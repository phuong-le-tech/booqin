package fr.uge.booqIn.filter;

import fr.uge.booqIn.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
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
    // Skip token refresh for refresh endpoint to avoid infinite loop
    if (request.getRequestURI().equals("/api/v1/auth/refresh")) {
      filterChain.doFilter(request, response);
      return;
    }

    // Try getting the access token first
    Optional<String> accessTokenOpt = jwtService.getTokenFromCookies(request, "jwt");

    String username = null;
    if (accessTokenOpt.isPresent() && jwtService.isAccessTokenValid(accessTokenOpt.get())) {
      logger.debug("Valid access token found");
      try {
        username = jwtService.extractUsername(accessTokenOpt.get());
      } catch (Exception e) {
        logger.warn("Error extracting username from access token", e);
        // Token might be malformed or signed incorrectly
      }
    }

    // If access token is invalid or expired, try refresh token
    if (username == null) {
      Optional<String> refreshTokenOpt = jwtService.getTokenFromCookies(request, "jwt-refresh");

      if (refreshTokenOpt.isEmpty()) {
        logger.info("No refresh token found");
        filterChain.doFilter(request, response);
        return;
      }

      String refreshToken = refreshTokenOpt.get();
      if (!jwtService.attemptTokenRefresh(refreshToken, response)) {
        logger.info("Token refresh attempt failed");
        filterChain.doFilter(request, response);
        return;
      }

      try {
        username = jwtService.extractUsername(refreshToken);
        logger.info("Successfully refreshed token for user: {}", username);
      } catch (Exception e) {
        logger.warn("Error extracting username from refresh token", e);
        filterChain.doFilter(request, response);
        return;
      }
    }

    // If we have a valid username, authenticate the user
    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = userDetailsService.loadUserByUsername(username);

      UsernamePasswordAuthenticationToken authToken =
              new UsernamePasswordAuthenticationToken(
                      userDetails,
                      null,
                      userDetails.getAuthorities()
              );
      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authToken);
      logger.debug("User authenticated: {}", username);
    }

    filterChain.doFilter(request, response);
  }
}