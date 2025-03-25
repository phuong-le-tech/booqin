package fr.uge.booqIn.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Service
public class JwtService {

  private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
  private static final String ACCESS_TOKEN_COOKIE_NAME = "jwt";
  private static final String REFRESH_TOKEN_COOKIE_NAME = "jwt-refresh";

  @Value("${security.jwt.token.secret-key}")
  private String SECRET;

  @Value("${security.jwt.token.access-expiration}")
  private long ACCESS_TOKEN_EXPIRATION;

  @Value("${security.jwt.token.refresh-expiration}")
  private long REFRESH_TOKEN_EXPIRATION;

  private final Map<String, Boolean> usedRefreshTokens = new ConcurrentHashMap<>();  // Track used refresh tokens
  private final Map<String, String> tokenFamilies = new ConcurrentHashMap<>();       // Track parent-child relationships
  private final Map<String, Set<String>> familyMembers = new ConcurrentHashMap<>();  // Track all tokens in a family

  private final Object tokenLock = new Object();

  public String generateAccessToken(String username) {
    return createToken(new HashMap<>(), username, ACCESS_TOKEN_EXPIRATION);
  }

  public String generateRefreshToken(String username) {
    return createToken(new HashMap<>(), username, REFRESH_TOKEN_EXPIRATION);
  }

  private String createToken(Map<String, Object> claims, String subject, long expirationMillis) {
    String jti = UUID.randomUUID().toString();
    claims.put("jti", jti);

    return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
            .signWith(getSignKey(), SignatureAlgorithm.HS256)
            .compact();
  }

  private Key getSignKey() {
    byte[] keyBytes = Decoders.BASE64.decode(SECRET);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(getSignKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
  }

  public boolean isTokenExpired(String token) {
    try {
      return extractExpiration(token).before(new Date());
    } catch (ExpiredJwtException e) {
      return true;
    }
  }

  public boolean validateToken(String token, UserDetails userDetails) {
    try {
      final String username = extractUsername(token);
      return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    } catch (Exception e) {
      return false;
    }
  }

  public boolean isAccessTokenValid(String token) {
    if (token == null || token.isEmpty()) {
      return false;
    }

    try {
      return !isTokenExpired(token);
    } catch (ExpiredJwtException e) {
      return false;
    }
  }

  public void revokeToken(String token) {
    if (token == null || token.isEmpty()) {
      return;
    }

    synchronized (tokenLock) {
      try {
        // Mark the token as used to prevent reuse
        usedRefreshTokens.put(token, Boolean.TRUE);

        // Find and revoke the token family if it exists
        String rootToken = findFamilyRoot(token);
        if (rootToken != null) {
          invalidateTokenFamily(rootToken);
        }

        logger.debug("Token revoked: {}", token);
      } catch (Exception e) {
        logger.error("Error revoking token", e);
      }
    }
  }

  private void invalidateTokenFamily(String familyRootToken) {
    synchronized (tokenLock) {
      Set<String> tokens = familyMembers.getOrDefault(familyRootToken, new HashSet<>());
      logger.warn("Invalidating entire token family with {} members", tokens.size() + 1);

      usedRefreshTokens.put(familyRootToken, Boolean.TRUE);
      for (String token : tokens) {
        usedRefreshTokens.put(token, Boolean.TRUE);
      }

      familyMembers.remove(familyRootToken);
    }
  }

  public boolean recordRefreshTokenUsage(String token) {
    synchronized (tokenLock) {
      if (usedRefreshTokens.containsKey(token)) {
        logger.warn("SECURITY ALERT: Refresh token reuse detected!");
        String rootToken = findFamilyRoot(token);
        if (rootToken != null) {
          invalidateTokenFamily(rootToken);
        } else {
          usedRefreshTokens.put(token, Boolean.TRUE);
        }
        return false;
      }

      usedRefreshTokens.put(token, Boolean.TRUE);
      return true;
    }
  }

  private String findFamilyRoot(String token) {
    synchronized (tokenLock) {
      String current = token;
      String parent = tokenFamilies.get(current);
      while (parent != null) {
        current = parent;
        parent = tokenFamilies.get(current);
      }
      return current;
    }
  }

  private void recordTokenFamily(String parentToken, String newToken) {
    synchronized (tokenLock) {
      String root = findFamilyRoot(parentToken);
      if (root == null) {
        root = parentToken;
      }

      tokenFamilies.put(newToken, parentToken);

      Set<String> members = familyMembers.computeIfAbsent(root, k -> new HashSet<>());
      members.add(newToken);

      logger.debug("Added token to family. Root={}, Parent={}, New={}", root, parentToken, newToken);
    }
  }

  public boolean attemptTokenRefresh(String refreshToken, HttpServletResponse response) {
    logger.debug("Attempting token refresh");

    if (isTokenExpired(refreshToken)) {
      logger.debug("Refresh token expired");
      return false;
    }

    try {
      if (!recordRefreshTokenUsage(refreshToken)) {
        logger.warn("Token reuse detected during refresh attempt");
        return false;
      }

      String username = extractUsername(refreshToken);
      String newAccessToken = generateAccessToken(username);
      String newRefreshToken = generateRefreshToken(username);

      recordTokenFamily(refreshToken, newRefreshToken);

      addCookie(response,
              ACCESS_TOKEN_COOKIE_NAME,
              newAccessToken,
              ACCESS_TOKEN_EXPIRATION / 1000
      );
      addCookie(response,
              REFRESH_TOKEN_COOKIE_NAME,
              newRefreshToken,
              REFRESH_TOKEN_EXPIRATION / 1000
      );

      logger.debug("Token refresh successful");
      return true;
    } catch (Exception e) {
      logger.error("Error during token refresh", e);
      return false;
    }
  }

  public Optional<String> getTokenFromCookies(HttpServletRequest request, String cookieName) {
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if (cookieName.equals(cookie.getName())) {
          return Optional.of(cookie.getValue());
        }
      }
    }
    return Optional.empty();
  }

  private void addCookie(HttpServletResponse response, String name, String value, long maxAgeSeconds) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);

    // Enable this for production environments (HTTPS)
    boolean isProduction = isProdEnvironment();
    cookie.setSecure(isProduction);

    cookie.setPath("/");
    cookie.setMaxAge((int) maxAgeSeconds);
    response.addCookie(cookie);
  }

  private boolean isProdEnvironment() {
    // TODO : with env
    return false;
  }


  public void cleanupExpiredTokens() {
    Date now = new Date();
    int usedBefore = usedRefreshTokens.size();
    int familiesBefore = familyMembers.size();

    synchronized (tokenLock) {
      Set<String> expiredTokens = new HashSet<>();
      for (String token : usedRefreshTokens.keySet()) {
        try {
          Date expiry = extractExpiration(token);
          if (expiry.before(now)) {
            expiredTokens.add(token);
          }
        } catch (ExpiredJwtException e) {
          expiredTokens.add(token);
        }
      }

      for (String token : expiredTokens) {
        usedRefreshTokens.remove(token);
        tokenFamilies.remove(token);
      }

      for (Map.Entry<String, Set<String>> entry : new HashMap<>(familyMembers).entrySet()) {
        entry.getValue().removeIf(expiredTokens::contains);
        if (entry.getValue().isEmpty() || expiredTokens.contains(entry.getKey())) {
          familyMembers.remove(entry.getKey());
        }
      }
    }

    logger.debug("Cleaned up tokens: removed {} used refresh tokens and {} token families",
            usedBefore - usedRefreshTokens.size(),
            familiesBefore - familyMembers.size());
  }
}