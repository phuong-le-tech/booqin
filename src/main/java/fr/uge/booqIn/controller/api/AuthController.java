package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.dto.auth.AuthResponse;
import fr.uge.booqIn.dto.auth.LoginDto;
import fr.uge.booqIn.dto.auth.RegisterDto;
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

    Cookie jwtCookie = new Cookie("jwt", tokens.accessToken());
    jwtCookie.setHttpOnly(true);
    jwtCookie.setSecure(false); // set true if you use HTTPS
    jwtCookie.setPath("/");
    jwtCookie.setMaxAge(30 * 60); // 30 minutes

    Cookie refreshCookie = new Cookie("jwt-refresh", tokens.refreshToken());
    refreshCookie.setHttpOnly(true);
    refreshCookie.setSecure(false); // set true if you use HTTPS
    refreshCookie.setPath("/");
    refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days

    response.addCookie(jwtCookie);
    response.addCookie(refreshCookie);

    return ResponseEntity.ok().body("Connexion réussie !");
  }

  @PostMapping("/register")
  public ResponseEntity<?> signUp(@RequestBody @Valid RegisterDto registerDto) {
    User user;
    try {
      user = authService.register(registerDto);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
    return ResponseEntity.status(HttpStatus.CREATED).body(user);
  }

  @PostMapping("/logout")
  public ResponseEntity<String> logout(HttpServletResponse response) {
    // 1. Overwrite the jwt cookie with an empty value and set maxAge to 0
    Cookie jwtCookie = new Cookie("jwt", null);
    jwtCookie.setHttpOnly(true);
    jwtCookie.setSecure(false);
    jwtCookie.setPath("/");
    jwtCookie.setMaxAge(0); // remove cookie immediately

    // 2. Overwrite the refresh cookie likewise
    Cookie refreshCookie = new Cookie("jwt-refresh", null);
    refreshCookie.setHttpOnly(true);
    refreshCookie.setSecure(false);
    refreshCookie.setPath("/");
    refreshCookie.setMaxAge(0);

    response.addCookie(jwtCookie);
    response.addCookie(refreshCookie);

    // 3. (Optional) If you store refresh tokens in DB, remove / invalidate them here
    // refreshTokenRepository.invalidate(...);

    return ResponseEntity.ok("Logged out successfully");
  }

  @PostMapping("/refresh")
  public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
    String refreshToken = null;
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("jwt-refresh".equals(cookie.getName())) {
          refreshToken = cookie.getValue();
        }
      }
    }

    if (refreshToken == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token is missing");
    }

    try {
      String username = jwtService.extractUsername(refreshToken);

      // Usually you would check whether this refresh token
      // is still valid in DB (if you stored it).
      // For example: refreshTokenRepository.findByToken(refreshToken)

      // If valid, generate new access token
      String newAccessToken = jwtService.generateAccessToken(username);

      // Optionally, you can rotate the refresh token as well:
      // String newRefreshToken = jwtService.generateRefreshToken(username);
      // and store it again in the cookie.

      Cookie jwtCookie = new Cookie("jwt", newAccessToken);
      jwtCookie.setHttpOnly(true);
      jwtCookie.setSecure(false);
      jwtCookie.setPath("/");
      jwtCookie.setMaxAge(30 * 60); // 30 minutes

      response.addCookie(jwtCookie);

      return ResponseEntity.ok().body("Access Token refreshed successfully");
    } catch (Exception e) {
      // e.g. token might be expired or invalid
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
    }
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
}
