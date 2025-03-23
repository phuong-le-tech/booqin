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
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional(readOnly = true)
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

  private final ConcurrentHashMap<String, Date> revokedTokens = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Boolean> usedRefreshTokens = new ConcurrentHashMap<>();

  private final Object tokenReuseLock = new Object();

  /**
   * Generates an access token for the specified username
   */
  public String generateAccessToken(String username) {
    return createToken(new HashMap<>(), username, ACCESS_TOKEN_EXPIRATION);
  }

  /**
   * Generates a refresh token for the specified username
   */
  public String generateRefreshToken(String username) {
    return createToken(new HashMap<>(), username, REFRESH_TOKEN_EXPIRATION);
  }

  /**
   * Creates a JWT token with the specified claims, subject, and expiration
   */
  private String createToken(Map<String, Object> claims, String subject, long expirationMillis) {
    return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
            .signWith(getSignKey(), SignatureAlgorithm.HS256)
            .compact();
  }

  /**
   * Gets the signing key for JWT tokens
   */
  private Key getSignKey() {
    byte[] keyBytes = Decoders.BASE64.decode(SECRET);
    return Keys.hmacShaKeyFor(keyBytes);
  }

  /**
   * Extracts the username from a JWT token
   */
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Extracts the expiration date from a JWT token
   */
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  /**
   * Extracts a claim from a JWT token using the provided claims resolver function
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  /**
   * Extracts all claims from a JWT token
   */
  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(getSignKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
  }

  /**
   * Checks if a token is expired
   */
  public boolean isTokenExpired(String token) {
    try {
      return extractExpiration(token).before(new Date());
    } catch (ExpiredJwtException e) {
      return true;
    }
  }

  /**
   * Validates a token for the specified user details
   * Checks if the token is not expired, not revoked, and belongs to the user
   */
  public boolean validateToken(String token, UserDetails userDetails) {
    try {
      final String username = extractUsername(token);
      return (username.equals(userDetails.getUsername())
              && !isTokenExpired(token)
              && !isTokenRevoked(token));
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Checks if an access token is valid (not expired, not revoked)
   */
  public boolean isAccessTokenValid(String token) {
    if (token == null || token.isEmpty()) {
      return false;
    }

    try {
      return !isTokenExpired(token) && !isTokenRevoked(token);
    } catch (ExpiredJwtException e) {
      return false;
    }
  }

  /**
   * Revokes a token by adding it to the blacklist
   */
  @Transactional
  public synchronized void revokeToken(String token) {
    if (token != null && !token.isEmpty()) {
      try {
        // Add to blacklist with expiration time
        Date expiry = extractExpiration(token);
        revokedTokens.put(token, expiry);
        logger.debug("Token revoked, expires at: {}", expiry);
      } catch (ExpiredJwtException e) {
        // Token already expired, no need to blacklist
        logger.debug("Attempted to revoke already expired token");
      }
    }
  }

  /**
   * Checks if a token has been revoked
   */
  public boolean isTokenRevoked(String token) {
    return revokedTokens.containsKey(token);
  }

  /**
   * Records that a refresh token has been used
   * This prevents refresh token reuse attacks
   */
  @Transactional
  public boolean recordRefreshTokenUsage(String token) {
    synchronized (tokenReuseLock) {
      if (usedRefreshTokens.containsKey(token)) {
        logger.warn("Attempted reuse of refresh token detected");
        return false;
      }

      usedRefreshTokens.put(token, Boolean.TRUE);
      return true;
    }
  }

  /**
   * Attempts to refresh a token
   *
   * @param refreshToken The refresh token to use
   * @param response     The HTTP response to add cookies to
   * @return true if token refresh was successful, false otherwise
   */
  @Transactional
  public boolean attemptTokenRefresh(String refreshToken, HttpServletResponse response) {
    logger.debug("Attempting token refresh");

    if (isTokenExpired(refreshToken) || isTokenRevoked(refreshToken)) {
      logger.debug("Refresh token expired or revoked");
      return false;
    }

    try {
      // Prevent refresh token reuse
/*      if (!recordRefreshTokenUsage(refreshToken)) {
        revokeToken(refreshToken);
        return false;
      }*/
      var username = extractUsername(refreshToken);
      addCookie(response,
              ACCESS_TOKEN_COOKIE_NAME,
              createToken(new HashMap<>(), username, ACCESS_TOKEN_EXPIRATION),
              ACCESS_TOKEN_EXPIRATION / 1000
      );
      addCookie(response,
              REFRESH_TOKEN_COOKIE_NAME,
              createToken(new HashMap<>(), username, REFRESH_TOKEN_EXPIRATION),
              REFRESH_TOKEN_EXPIRATION / 1000
      );

      logger.debug("Token refresh successful");
      return true;
    } catch (Exception e) {
      logger.error("Error during token refresh", e);
      return false;
    }
  }

  /**
   * Gets a token from cookies in the request
   */
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

  /**
   * Adds a cookie to the response
   */
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

  /**
   * Determines if the current environment is production
   * This method should be implemented to check the environment
   */
  private boolean isProdEnvironment() {
    // Implement environment check logic here
    // For example, check Spring profiles or environment variables
    return false; // Default to false for development
  }

  /**
   * Cleans up expired tokens from the blacklist and used token cache
   * Should be called periodically (e.g., via a scheduled task)
   */
  @Transactional
  public synchronized void cleanupExpiredTokens() {
    Date now = new Date();
    int revokedBefore = revokedTokens.size();
    int usedBefore = usedRefreshTokens.size();

    revokedTokens.entrySet().removeIf(entry -> entry.getValue().before(now));

    // For used refresh tokens, we need to extract expiry from the token itself
    usedRefreshTokens.entrySet().removeIf(entry -> {
      try {
        Date expiry = extractExpiration(entry.getKey());
        return expiry.before(now);
      } catch (ExpiredJwtException e) {
        return true;
      }
    });

    logger.debug("Cleaned up tokens: removed {} revoked tokens and {} used refresh tokens",
            revokedBefore - revokedTokens.size(),
            usedBefore - usedRefreshTokens.size());
  }
}