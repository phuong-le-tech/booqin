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
            ApiResponse.success("Connexion réussie !", tokens));
  }

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<User>> signUp(@RequestBody @Valid RegisterDto registerDto) {
    User user = authService.register(registerDto);

    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success("User registered successfully", user));
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
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

    try {
      String username = jwtService.extractUsername(refreshToken);
      String newAccessToken = jwtService.generateAccessToken(username);

      addCookie(response, "jwt", newAccessToken, 30 * 60);

      return ResponseEntity.ok(
              ApiResponse.success("Access Token refreshed successfully", newAccessToken));
    } catch (Exception e) {
      return ResponseEntity
              .status(HttpStatus.UNAUTHORIZED)
              .body(ApiResponse.error("Invalid refresh token"));
    }
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<UserMeDto>> me(Authentication authentication) {
    UserMeDto userMeDto = authService.getCurrentUser(authentication);
    return ResponseEntity.ok(
            ApiResponse.success("Current user data", userMeDto));
  }

  /*
   * =======================
   * Méthodes utilitaires
   * =======================
   */

  private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(false); // Mettre true en production (HTTPS)
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
