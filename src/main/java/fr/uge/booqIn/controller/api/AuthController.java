package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.dto.auth.JwtDto;
import fr.uge.booqIn.dto.auth.SignInDto;
import fr.uge.booqIn.dto.auth.SignUpDto;
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
  public  ResponseEntity<JwtDto> signIn(@RequestBody @Valid SignInDto data) {
    String token = authService.signIn(data);
    return ResponseEntity.ok(new JwtDto(token));
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
