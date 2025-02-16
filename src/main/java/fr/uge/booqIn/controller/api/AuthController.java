package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.dto.auth.AuthResponse;
import fr.uge.booqIn.dto.auth.LoginDto;
import fr.uge.booqIn.dto.auth.RegisterDto;
import fr.uge.booqIn.dto.auth.UserMeDto;
import fr.uge.booqIn.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final AuthService authService;
  private final JwtService jwtService;
  public AuthController(AuthService authService, JwtService jwtService) {
    this.authService = authService;
    this.jwtService = jwtService;
  }

  @PostMapping("/login")
  public ResponseEntity<String> login(@RequestBody @Valid LoginDto data, HttpServletResponse response) {
    AuthResponse tokens = authService.login(data);
    addCookie(response, "jwt", tokens.accessToken(), 30 * 60);
    addCookie(response, "jwt-refresh", tokens.refreshToken(), 7 * 24 * 60 * 60);
    return ResponseEntity.ok().body("Connexion réussie !");
  }

  @PostMapping("/register")
  public ResponseEntity<?> signUp(@RequestBody @Valid RegisterDto registerDto) {
    User user = authService.register(registerDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(user);
  }

  @PostMapping("/logout")
  public ResponseEntity<String> logout(HttpServletResponse response) {
    addCookie(response, "jwt", null, 0);
    addCookie(response, "jwt-refresh", null, 0);
    return ResponseEntity.ok("Logged out successfully");
  }

  @PostMapping("/refresh")
  public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
    String refreshToken = getCookieValue(request, "jwt-refresh");

    if (refreshToken == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token is missing");
    }

    try {
      String username = jwtService.extractUsername(refreshToken);
      String newAccessToken = jwtService.generateAccessToken(username);
      addCookie(response, "jwt", newAccessToken, 30 * 60);

      return ResponseEntity.ok().body("Access Token refreshed successfully");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
    }
  }

  @GetMapping("/me")
  public ResponseEntity<?> me(Authentication authentication) {
    UserMeDto userMeDto = authService.getCurrentUser(authentication);
    return ResponseEntity.ok(userMeDto);
  }

  @GetMapping("/user/test")
  @PreAuthorize("hasAuthority('ROLE_USER')")
  public String test() {
    return "Hello World";
  }

  @GetMapping("/admin/test")
  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  public String testAdmin() {
    return "Hello Admin";
  }

  /**
   * Méthode utilitaire pour ajouter un cookie.
   */
  private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
    Cookie cookie = new Cookie(name, value);
    cookie.setHttpOnly(true);
    cookie.setSecure(false); // Mettre à true en production (HTTPS)
    cookie.setMaxAge(maxAge);
    response.addCookie(cookie);
  }

  /**
   * Méthode utilitaire pour récupérer la valeur d’un cookie donné.
   */
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
