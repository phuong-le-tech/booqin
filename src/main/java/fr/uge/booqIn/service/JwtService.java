package fr.uge.booqIn.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwtService {

  @Value("${security.jwt.token.secret-key}")
  private String SECRET;

  private final ConcurrentHashMap<String, Date> revokedTokens = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Boolean> usedRefreshTokens = new ConcurrentHashMap<>();

  private final Object tokenReuseLock = new Object();

  /**
   * Generates an access token for the specified username
   */
  public String generateAccessToken(String username) {
    return createToken(new HashMap<>(), username, 1000 * 60 * 30); // 30 minutes
  }

  /**
   * Generates a refresh token for the specified username
   */
  public String generateRefreshToken(String username) {
    return createToken(new HashMap<>(), username, 1000L * 60 * 60 * 24 * 7); // 7 days
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
   * Revokes a token by adding it to the blacklist
   */
  public synchronized void revokeToken(String token) {
    if (token != null && !token.isEmpty()) {
      try {
        // Add to blacklist with expiration time
        Date expiry = extractExpiration(token);
        revokedTokens.put(token, expiry);
      } catch (ExpiredJwtException e) {
        // Token already expired, no need to blacklist
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
   *
   * This method is synchronized on a dedicated lock to ensure that the check and set
   * operations are atomic across all threads, preventing race conditions.
   */
  public boolean recordRefreshTokenUsage(String token) {
    synchronized (tokenReuseLock) {
      if (usedRefreshTokens.containsKey(token)) {
        return false;
      }

      usedRefreshTokens.put(token, Boolean.TRUE);
      return true;
    }
  }

  /**
   * Cleans up expired tokens from the blacklist and used token cache
   * Should be called periodically (e.g., via a scheduled task)
   */
  public synchronized void cleanupExpiredTokens() {
    Date now = new Date();
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
  }
}