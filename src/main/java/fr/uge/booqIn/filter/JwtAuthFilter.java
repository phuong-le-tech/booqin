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
    if (SecurityContextHolder.getContext().getAuthentication() != null) {
      filterChain.doFilter(request, response);
      return;
    }

    boolean authenticated = authenticateWithAccessToken(request);

    if (!authenticated) {
      authenticated = authenticateWithRefreshToken(request, response);
    }

    logger.debug("Request authenticated: {}", authenticated);
    filterChain.doFilter(request, response);
  }

  private boolean authenticateWithAccessToken(HttpServletRequest request) {
    Optional<String> accessTokenOpt = jwtService.getTokenFromCookies(request, ACCESS_TOKEN_COOKIE_NAME);

    if (accessTokenOpt.isEmpty() || accessTokenOpt.get().isEmpty()) {
      return false;
    }

    return processAccessToken(accessTokenOpt.get(), request);
  }

  private boolean authenticateWithRefreshToken(HttpServletRequest request, HttpServletResponse response) {
    Optional<String> refreshTokenOpt = jwtService.getTokenFromCookies(request, REFRESH_TOKEN_COOKIE_NAME);

    if (refreshTokenOpt.isEmpty() || refreshTokenOpt.get().isEmpty()) {
      return false;
    }

    String refreshToken = refreshTokenOpt.get();

    if (jwtService.isTokenExpired(refreshToken)) {
      logger.info("Refresh token expired - clearing auth cookies");
      clearAuthCookies(response);
      return false;
    }

    try {
      boolean refreshed = jwtService.attemptTokenRefresh(refreshToken, response);

      if (!refreshed) {
        logger.warn("Failed to refresh token - clearing auth cookies");
        clearAuthCookies(response);
        return false;
      }

      String username = jwtService.extractUsername(refreshToken);
      if (username == null) {
        return false;
      }

      authenticateUser(username, request);
      logger.info("User authenticated via token refresh: {}", username);
      return true;

    } catch (Exception e) {
      logger.warn("Error during token refresh - clearing cookies", e);
      clearAuthCookies(response);
      return false;
    }
  }

  private boolean processAccessToken(String accessToken, HttpServletRequest request) {
    try {
      if (!jwtService.isAccessTokenValid(accessToken)) {
        return false;
      }

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

  private void authenticateUser(String username, HttpServletRequest request) {
    try {
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities()
      );

      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authToken);
      logger.debug("User authenticated: {}", username);
    } catch (Exception e) {
      logger.error("Failed to authenticate user: {}", username, e);
    }
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