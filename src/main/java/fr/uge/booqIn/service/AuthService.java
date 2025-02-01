package fr.uge.booqIn.service;

import fr.uge.booqIn.dto.auth.SignInDto;
import fr.uge.booqIn.dto.auth.SignUpDto;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
          JwtService jwtService
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
  }

  public User signUp(SignUpDto signUpDto) {
    userRepository.findByEmail(signUpDto.email()).ifPresent(u -> {
      throw new IllegalArgumentException("Email already taken");
    });

    User user = new User(
            signUpDto.firstName(),
            signUpDto.lastName(),
            signUpDto.email(),
            passwordEncoder.encode(signUpDto.password()),
            signUpDto.address(),
            signUpDto.role()
    );
    return userRepository.save(user);
  }

  public String signIn(SignInDto signInDto) {
    UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
                    signInDto.login(),
                    signInDto.password()
            );

    var authResult = authenticationManager.authenticate(authToken);
    var user = (User) authResult.getPrincipal();
    return jwtService.generateToken(user.getUsername());
  }
}
