package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.dto.auth.AuthResponse;
import fr.uge.booqIn.dto.auth.LoginDto;
import fr.uge.booqIn.dto.auth.RegisterDto;
import fr.uge.booqIn.dto.auth.UserMeDto;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.exception.DuplicateResourceException;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.mapper.AuthMapper;
import fr.uge.booqIn.repository.CartRepository;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.interfaces.AuthServiceInterface;
import fr.uge.booqIn.service.JwtService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class AuthServiceImpl implements AuthServiceInterface {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final AuthMapper authMapper;
  private final CartRepository cartRepository;

  private final Object refreshLock = new Object();

  public AuthServiceImpl(
          UserRepository userRepository,
          PasswordEncoder passwordEncoder,
          AuthenticationManager authenticationManager,
          JwtService jwtService,
          AuthMapper authMapper,
          CartRepository cartRepository) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.authMapper = authMapper;
    this.cartRepository = cartRepository;
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public User register(RegisterDto registerDto) {
    userRepository.findByEmail(registerDto.email()).ifPresent(u -> {
      throw new DuplicateResourceException("User", "email", registerDto.email());
    });


    Cart cart = new Cart();
    cartRepository.save(cart);

    User user = new User(
            registerDto.firstName(),
            registerDto.lastName(),
            registerDto.email(),
            passwordEncoder.encode(registerDto.password()),
            registerDto.address(),
            UserRole.USER,
            cart
    );

    user.setCreatedAt(new Date());
    user.setUpdatedAt(new Date());

    return userRepository.save(user);
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public AuthResponse login(LoginDto signInDto) {
    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            signInDto.login(),
            signInDto.password());

    var authResult = authenticationManager.authenticate(authToken);
    var user = (User) authResult.getPrincipal();

    String accessToken = jwtService.generateAccessToken(user.getUsername());
    String refreshToken = jwtService.generateRefreshToken(user.getUsername());

    user.setUpdatedAt(new Date());
    userRepository.save(user);

    return new AuthResponse(accessToken, refreshToken);
  }

  @Override
  @Transactional
  public UserMeDto getCurrentUser(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new IllegalStateException("User is not authenticated");
    }

    User user = (User) authentication.getPrincipal();

    User refreshedUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new IllegalStateException("User not found"));

    return authMapper.toUserMeDto(refreshedUser);
  }

  @Transactional
  public AuthResponse refreshToken(String refreshToken) {
    if (jwtService.isTokenExpired(refreshToken)) {
      throw new IllegalStateException("Refresh token has expired");
    }

    boolean isFirstUse = jwtService.recordRefreshTokenUsage(refreshToken);
    if (!isFirstUse) {
      throw new IllegalStateException("Token reuse detected");
    }

    String username = jwtService.extractUsername(refreshToken);

    synchronized (refreshLock) {
      User user = userRepository.findByEmail(username)
              .orElseThrow(() -> new ResourceNotFoundException("User not found"));

      String newAccessToken = jwtService.generateAccessToken(username);
      String newRefreshToken = jwtService.generateRefreshToken(username);

      user.setUpdatedAt(new Date());
      userRepository.save(user);

      return new AuthResponse(newAccessToken, newRefreshToken);
    }
  }

  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public void invalidateUserSessions(Long userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found"));

    user.setUpdatedAt(new Date());
    userRepository.save(user);
  }

  @Recover
  public User recoverFromDuplicateException(DuplicateResourceException ex, RegisterDto registerDto) {
    // Simply rethrow - we don't want to retry this
    throw ex;
  }

  @Recover
  public AuthResponse recoverFromOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex, LoginDto loginDto) {
    throw new ConcurrentModificationException("Concurrent user modification detected. Please try again.", ex);
  }

  @Recover
  public void recoverFromOptimisticLockingFailureInvalidate(ObjectOptimisticLockingFailureException ex, Long userId) {
    throw new ConcurrentModificationException("Concurrent user modification detected. Please try again.", ex);
  }
}