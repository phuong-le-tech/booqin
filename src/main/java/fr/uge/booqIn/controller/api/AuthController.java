package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.dto.auth.AuthResponse;
import fr.uge.booqIn.dto.auth.LoginDto;
import fr.uge.booqIn.dto.auth.RegisterDto;
import fr.uge.booqIn.dto.auth.UserMeDto;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.JwtService;
import fr.uge.booqIn.service.interfaces.AuthServiceInterface;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthServiceInterface authService;
  private final JwtService jwtService;

  private static final String ACCESS_TOKEN_COOKIE_NAME = "jwt";
  private static final String REFRESH_TOKEN_COOKIE_NAME = "jwt-refresh";

  @Value("${security.jwt.token.access-expiration}")
  private long ACCESS_TOKEN_EXPIRATION;
  @Value("${security.jwt.token.refresh-expiration}")
  private long REFRESH_TOKEN_EXPIRATION;

  public AuthController(AuthServiceInterface authService, JwtService jwtService) {
    this.authService = authService;
    this.jwtService = jwtService;
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(
          @RequestBody @Valid LoginDto data,
          HttpServletResponse response) {

    AuthResponse tokens = authService.login(data);

    addCookie(response, ACCESS_TOKEN_COOKIE_NAME, tokens.accessToken(), ACCESS_TOKEN_EXPIRATION);
    addCookie(response, REFRESH_TOKEN_COOKIE_NAME, tokens.refreshToken(), REFRESH_TOKEN_EXPIRATION);

    return ResponseEntity.ok(
            ApiResponse.success("Login successful", tokens));
  }

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<User>> signUp(@RequestBody @Valid RegisterDto registerDto) {
    User user = authService.register(registerDto);

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("User registered successfully", user));
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
          @NonNull HttpServletRequest request,
          @NonNull HttpServletResponse response,
          @NonNull Authentication authentication) {

    // Get tokens from cookies
    var refreshToken = getCookieValue(request);

    // Revoke refresh token if present (which invalidates its entire family)
    refreshToken.ifPresent(jwtService::revokeToken);

    // Invalidate user sessions if authenticated
    if (authentication.isAuthenticated()) {
      User user = (User) authentication.getPrincipal();
      authService.invalidateUserSessions(user.getId());
    }

    // Clear cookies
    clearCookies(response);

    return ResponseEntity.ok(
            ApiResponse.success("Logged out successfully", null));
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserMeDto>> me(@NonNull Authentication authentication) {
    UserMeDto userMeDto = authService.getCurrentUser(authentication);
    return ResponseEntity.ok(
            ApiResponse.success("Current user data", userMeDto));
  }

  /**
   * Endpoint to manually refresh tokens
   */
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<Void>> refreshToken(
          @NonNull HttpServletRequest request,
          @NonNull HttpServletResponse response) {

    Optional<String> refreshToken = getCookieValue(request);

    if (refreshToken.isEmpty()) {
      return ResponseEntity
              .status(HttpStatus.UNAUTHORIZED)
              .body(ApiResponse.error("No refresh token provided"));
    }

    boolean refreshed = jwtService.attemptTokenRefresh(refreshToken.get(), response);

    if (!refreshed) {
      // Clear cookies on failure
      clearCookies(response);

      return ResponseEntity
              .status(HttpStatus.UNAUTHORIZED)
              .body(ApiResponse.error("Token refresh failed, please login again"));
    }

    return ResponseEntity.ok(
            ApiResponse.success("Tokens refreshed successfully", null));
  }

  /*
   * =======================
   * Utility methods
   * =======================
   */

  private void addCookie(HttpServletResponse response, String name, String value, long maxAgeMillis) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(false); // Set to true in production (HTTPS)
    cookie.setPath("/");
    cookie.setMaxAge((int)(maxAgeMillis / 1000));
    response.addCookie(cookie);
  }

  private Optional<String> getCookieValue(HttpServletRequest request) {
    if (request.getCookies() == null) {
      return Optional.empty();
    }
    for (Cookie cookie : request.getCookies()) {
      if (AuthController.REFRESH_TOKEN_COOKIE_NAME.equals(cookie.getName())) {
        return Optional.of(cookie.getValue());
      }
    }
    return Optional.empty();
  }

  private void clearCookies(HttpServletResponse response) {
    Cookie accessCookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, null);
    accessCookie.setHttpOnly(true);
    accessCookie.setPath("/");
    accessCookie.setMaxAge(0);
    response.addCookie(accessCookie);

    Cookie refreshCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);
    refreshCookie.setHttpOnly(true);
    refreshCookie.setPath("/");
    refreshCookie.setMaxAge(0);
    response.addCookie(refreshCookie);
  }
}