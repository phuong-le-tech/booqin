package fr.uge.booqIn.service;

import fr.uge.booqIn.dto.auth.AuthResponse;
import fr.uge.booqIn.dto.auth.LoginDto;
import fr.uge.booqIn.dto.auth.RegisterDto;
import fr.uge.booqIn.dto.auth.UserMeDto;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;

  public AuthService(
          UserRepository userRepository,
          PasswordEncoder passwordEncoder,
          AuthenticationManager authenticationManager,
          JwtService jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
  }

  public User register(RegisterDto signUpDto) {
    userRepository.findByEmail(signUpDto.email()).ifPresent(u -> {
      throw new IllegalArgumentException("Email already taken");
    });

    User user = new User(
            signUpDto.firstName(),
            signUpDto.lastName(),
            signUpDto.email(),
            passwordEncoder.encode(signUpDto.password()),
            signUpDto.address(),
            /* signUpDto.role() */ UserRole.USER);
    return userRepository.save(user);
  }

  public AuthResponse login(LoginDto signInDto) {
    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            signInDto.login(),
            signInDto.password());

    var authResult = authenticationManager.authenticate(authToken);
    var user = (User) authResult.getPrincipal();

    String accessToken = jwtService.generateAccessToken(user.getUsername());
    String refreshToken = jwtService.generateRefreshToken(user.getUsername());

    return new AuthResponse(accessToken, refreshToken);
  }

  public UserMeDto getCurrentUser(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      System.out.println("User is not authenticated");
      throw new IllegalStateException("User is not authenticated");
    }
    User user = (User) authentication.getPrincipal();

    return new UserMeDto(
            user.getId(),
            user.getFirstName(),
            user.getLastName(),
            user.getEmail());
  }
}
