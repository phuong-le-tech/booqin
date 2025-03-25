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
          HttpServletRequest request,
          HttpServletResponse response,
          Authentication authentication) {
    var accessToken = getCookieValue(request, ACCESS_TOKEN_COOKIE_NAME);
    var refreshToken = getCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);

    accessToken.ifPresent(jwtService::revokeToken);
    refreshToken.ifPresent(jwtService::revokeToken);

    if (authentication != null && authentication.isAuthenticated()) {
      User user = (User) authentication.getPrincipal();
      authService.invalidateUserSessions(user.getId());
    }

    addCookie(response, ACCESS_TOKEN_COOKIE_NAME, null, 0);
    addCookie(response, REFRESH_TOKEN_COOKIE_NAME, null, 0);

    return ResponseEntity.ok(
            ApiResponse.success("Logged out successfully", null));

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

  private void addCookie(HttpServletResponse response, String name, String value, long maxAgeMillis) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(false); // Set to true in production (HTTPS)
    cookie.setPath("/");
    cookie.setMaxAge((int)(maxAgeMillis / 1000));
    response.addCookie(cookie);
  }

  private Optional<String> getCookieValue(HttpServletRequest request, String cookieName) {
    if (request.getCookies() == null) {
      return Optional.empty();
    }
    for (Cookie cookie : request.getCookies()) {
      if (cookieName.equals(cookie.getName())) {
        return Optional.of(cookie.getValue());
      }
    }
    return Optional.empty();
  }
}