package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.dto.auth.JwtDto;
import fr.uge.booqIn.dto.auth.SignInDto;
import fr.uge.booqIn.dto.auth.SignUpDto;
import jakarta.servlet.http.Cookie;
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

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/signin")
  public  ResponseEntity<String> signIn(@RequestBody @Valid SignInDto data, HttpServletResponse response) {
    String token = authService.signIn(data);

    Cookie jwtCookie = new Cookie("jwt", token);
    jwtCookie.setHttpOnly(true);  // Empêche l'accès via JavaScript (sécurité contre XSS)
    jwtCookie.setSecure(false);     // Active le cookie uniquement en HTTPS
    jwtCookie.setPath("/");        // Disponible sur toute l'application
    jwtCookie.setMaxAge(24 * 60 * 60); // Expiration en 1 jour

    response.addCookie(jwtCookie);

    return ResponseEntity.ok().body("Connexion réussie !");
  }

  @PostMapping("/signup")
  public ResponseEntity<?> signUp(@RequestBody @Valid SignUpDto signUpDto) {
    User user;
    try {
      user = authService.signUp(signUpDto);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
    return ResponseEntity.status(HttpStatus.CREATED).body(user);
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
