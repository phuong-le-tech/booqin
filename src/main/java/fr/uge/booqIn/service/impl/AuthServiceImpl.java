package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.dto.auth.AuthResponse;
import fr.uge.booqIn.dto.auth.LoginDto;
import fr.uge.booqIn.dto.auth.RegisterDto;
import fr.uge.booqIn.dto.auth.UserMeDto;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.mapper.AuthMapper;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.interfaces.AuthServiceInterface;
import fr.uge.booqIn.service.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthServiceInterface {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final AuthMapper authMapper;

  public AuthServiceImpl(
          UserRepository userRepository,
          PasswordEncoder passwordEncoder,
          AuthenticationManager authenticationManager,
          JwtService jwtService,
          AuthMapper authMapper) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.authMapper = authMapper;
  }

  @Override
  @Transactional
  public User register(RegisterDto registerDto) {
    userRepository.findByEmail(registerDto.email()).ifPresent(u -> {
      throw new IllegalArgumentException("Email already taken");
    });
    Cart cart = new Cart();
    User user = new User(
            registerDto.firstName(),
            registerDto.lastName(),
            registerDto.email(),
            passwordEncoder.encode(registerDto.password()),
            registerDto.address(),
            UserRole.USER,
            cart
    );

    return userRepository.save(user);
  }

  @Override
  @Transactional
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

  @Override
  @Transactional(readOnly = true)
  public UserMeDto getCurrentUser(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new IllegalStateException("User is not authenticated");
    }

    User user = (User) authentication.getPrincipal();

    return authMapper.toUserMeDto(user);
  }
}