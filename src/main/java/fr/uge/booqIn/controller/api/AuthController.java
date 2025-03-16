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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthServiceInterface authService;
  private final JwtService jwtService;

  public AuthController(AuthServiceInterface authService, JwtService jwtService) {
    this.authService = authService;
    this.jwtService = jwtService;
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(
          @RequestBody @Valid LoginDto data,
          HttpServletResponse response) {

    AuthResponse tokens = authService.login(data);

    addCookie(response, "jwt", tokens.accessToken(), 30 * 60);
    addCookie(response, "jwt-refresh", tokens.refreshToken(), 7 * 24 * 60 * 60);

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
          HttpServletRequest request,
          HttpServletResponse response,
          Authentication authentication) {
    String accessToken = getCookieValue(request, "jwt");
    String refreshToken = getCookieValue(request, "jwt-refresh");

    if (accessToken != null) {
      jwtService.revokeToken(accessToken);
    }

    if (refreshToken != null) {
      jwtService.revokeToken(refreshToken);
    }

    if (authentication != null && authentication.isAuthenticated()) {
      User user = (User) authentication.getPrincipal();
      authService.invalidateUserSessions(user.getId());
    }

    addCookie(response, "jwt", null, 0);
    addCookie(response, "jwt-refresh", null, 0);

    return ResponseEntity.ok(
            ApiResponse.success("Logged out successfully", null));

  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<String>> refreshToken(
          HttpServletRequest request,
          HttpServletResponse response) {
    String refreshToken = getCookieValue(request, "jwt-refresh");

    if (refreshToken == null) {
      return ResponseEntity
              .status(HttpStatus.UNAUTHORIZED)
              .body(ApiResponse.error("Refresh token is missing"));
    }

    if (jwtService.isTokenExpired(refreshToken)) {
      return ResponseEntity
              .status(HttpStatus.UNAUTHORIZED)
              .body(ApiResponse.error("Refresh token has expired"));
    }

    if (jwtService.isTokenRevoked(refreshToken)) {
      return ResponseEntity
              .status(HttpStatus.UNAUTHORIZED)
              .body(ApiResponse.error("Refresh token has been revoked"));
    }

    if (!jwtService.recordRefreshTokenUsage(refreshToken)) {
      jwtService.revokeToken(refreshToken);

      return ResponseEntity
              .status(HttpStatus.UNAUTHORIZED)
              .body(ApiResponse.error("Token reuse detected. Please login again."));
    }

    AuthResponse tokens = authService.refreshToken(refreshToken);

    addCookie(response, "jwt", tokens.accessToken(), 30 * 60);
    addCookie(response, "jwt-refresh", tokens.refreshToken(), 7 * 24 * 60 * 60);

    return ResponseEntity.ok(
            ApiResponse.success("Access Token refreshed successfully", tokens.accessToken()));
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserMeDto>> me(Authentication authentication) {
      UserMeDto userMeDto = authService.getCurrentUser(authentication);
      return ResponseEntity.ok(
              ApiResponse.success("Current user data", userMeDto));
  }

  /*
   * =======================
   * Utility methods
   * =======================
   */

  private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(false); // Set to true in production (HTTPS)
    cookie.setPath("/");
    cookie.setMaxAge(maxAge);
    response.addCookie(cookie);
  }

  private String getCookieValue(HttpServletRequest request, String cookieName) {
    if (request.getCookies() == null) {
      return null;
    }
    for (Cookie cookie : request.getCookies()) {
      if (cookieName.equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }
}