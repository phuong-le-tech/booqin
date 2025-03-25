package fr.uge.booqIn.filter;

import fr.uge.booqIn.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
  private static final String ACCESS_TOKEN_COOKIE_NAME = "jwt";
  private static final String REFRESH_TOKEN_COOKIE_NAME = "jwt-refresh";

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;

  public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
    this.jwtService = jwtService;
    this.userDetailsService = userDetailsService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain
  ) throws ServletException, IOException {
    // Try to authenticate using access token first
    Optional<String> accessTokenOpt = jwtService.getTokenFromCookies(request, ACCESS_TOKEN_COOKIE_NAME);
    boolean authenticated = processAccessToken(accessTokenOpt, request);

    // If access token authentication failed, try refresh token
    if (!authenticated) {
      Optional<String> refreshTokenOpt = jwtService.getTokenFromCookies(request, REFRESH_TOKEN_COOKIE_NAME);

      if (refreshTokenOpt.isPresent()) {
        String refreshToken = refreshTokenOpt.get();
        boolean refreshed = processRefreshToken(refreshToken, response);

        if (refreshed) {
          // we know it's valid, so authenticate the user
          String username = jwtService.extractUsername(refreshToken);
          if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            authenticateUser(username, request);
            authenticated = true;
            logger.info("User authenticated via token refresh: {}", username);
          }
        } else {
          // If refresh failed (expired, invalid, or reused), clear cookies
          clearAuthCookies(response);
        }
      }
    }
    filterChain.doFilter(request, response);
  }

  private boolean processAccessToken(Optional<String> accessTokenOpt, HttpServletRequest request) {
    if (accessTokenOpt.isEmpty() || !jwtService.isAccessTokenValid(accessTokenOpt.get())) {
      return false;
    }

    String accessToken = accessTokenOpt.get();
    try {
      String username = jwtService.extractUsername(accessToken);
      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        authenticateUser(username, request);
        return true;
      }
    } catch (Exception e) {
      logger.warn("Error processing access token", e);
    }

    return false;
  }

  private boolean processRefreshToken(String refreshToken, HttpServletResponse response) {
    try {
      // This will handle token rotation and reuse detection
      boolean refreshed = jwtService.attemptTokenRefresh(refreshToken, response);

      if (refreshed) {
        String username = jwtService.extractUsername(refreshToken);
        logger.info("Successfully refreshed tokens for user: {}", username);
      }

      return refreshed;
    } catch (Exception e) {
      logger.warn("Error during token refresh", e);
      return false;
    }
  }

  private void authenticateUser(String username, HttpServletRequest request) {
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            userDetails,
            null,
            userDetails.getAuthorities()
    );

    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authToken);
    logger.debug("User authenticated: {}", username);
  }

  private void clearAuthCookies(HttpServletResponse response) {
    Cookie accessCookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, "");
    accessCookie.setMaxAge(0);
    accessCookie.setPath("/");
    accessCookie.setHttpOnly(true);
    response.addCookie(accessCookie);

    Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "");
    refreshCookie.setMaxAge(0);
    refreshCookie.setPath("/");
    refreshCookie.setHttpOnly(true);
    response.addCookie(refreshCookie);

    logger.debug("Cleared authentication cookies");
  }
}