package fr.uge.booqIn.service;

import fr.uge.booqIn.dto.auth.AuthResponse;
import fr.uge.booqIn.dto.auth.LoginDto;
import fr.uge.booqIn.dto.auth.RegisterDto;
import fr.uge.booqIn.dto.auth.UserMeDto;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.exception.DuplicateResourceException;
import fr.uge.booqIn.factory.fixture.AddressFactory;
import fr.uge.booqIn.mapper.AuthMapper;
import fr.uge.booqIn.mapper.UserMapper;
import fr.uge.booqIn.repository.CartRepository;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.impl.AuthServiceImpl;
import fr.uge.booqIn.service.interfaces.AuthServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("AuthService Tests")
class AuthServiceTest {

  private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;
  private AuthenticationManager authenticationManager;
  private JwtService jwtService;
  private AuthMapper authMapper;
  private CartRepository cartRepository;
  private AuthServiceInterface authService;
  private User testUser;
  private Cart testCart;
  private Address testAddress;
  private UserMapper userMapper;

  @BeforeEach
  void setUp() {
    userRepository = Mockito.mock(UserRepository.class);
    passwordEncoder = Mockito.mock(PasswordEncoder.class);
    authenticationManager = Mockito.mock(AuthenticationManager.class);
    jwtService = Mockito.mock(JwtService.class);
    authMapper = Mockito.mock(AuthMapper.class);
    cartRepository = Mockito.mock(CartRepository.class);
    userMapper = Mockito.mock(UserMapper.class);
    authService = new AuthServiceImpl(
            userRepository,
            passwordEncoder,
            authenticationManager,
            jwtService,
            authMapper,
            cartRepository,
            userMapper
    );

    // Setup test data
    testAddress = AddressFactory.createAddress();

    testCart = new Cart();
    testCart.setId(1L);

    testUser = new User();
    testUser.setId(1L);
    testUser.setFirstName("John");
    testUser.setLastName("Doe");
    testUser.setEmail("john.doe@example.com");
    testUser.setPassword("Password123");
    testUser.setAddress(testAddress);
    testUser.setRole(UserRole.USER);
    testUser.setCart(testCart);
    testUser.setCreatedAt(new Date());
    testUser.setUpdatedAt(new Date());
  }

  @Test
  @DisplayName("register - Should create and return new user")
  void register_WithValidData_ShouldCreateAndReturnUser() {
    // Given
    RegisterDto registerDto = new RegisterDto(
            "John",
            "Doe",
            "john.doe@example.com",
            "Password123",
            testAddress,
            UserRole.USER
    );

    when(userRepository.findByEmail(eq(registerDto.email()))).thenReturn(Optional.empty());
    when(cartRepository.save(any(Cart.class))).thenReturn(testCart);
    when(passwordEncoder.encode(eq(registerDto.password()))).thenReturn("encodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // When
    UserIndex result = authService.register(registerDto);

    // Then
    assertNotNull(result);
    assertEquals(testUser, result);

    verify(userRepository).findByEmail(eq(registerDto.email()));
    verify(cartRepository).save(any(Cart.class));
    verify(passwordEncoder).encode(eq(registerDto.password()));
    verify(userRepository).save(any(User.class));

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    User capturedUser = userCaptor.getValue();
    assertEquals(registerDto.firstName(), capturedUser.getFirstName());
    assertEquals(registerDto.lastName(), capturedUser.getLastName());
    assertEquals(registerDto.email(), capturedUser.getEmail());
    assertEquals("encodedPassword", capturedUser.getPassword());
    assertEquals(registerDto.address(), capturedUser.getAddress());
    assertEquals(registerDto.role(), capturedUser.getRole());
    assertNotNull(capturedUser.getCart());
    assertNotNull(capturedUser.getCreatedAt());
    assertNotNull(capturedUser.getUpdatedAt());
  }

  @Test
  @DisplayName("register - Should throw DuplicateResourceException when email already exists")
  void register_WithExistingEmail_ShouldThrowException() {
    // Given
    RegisterDto registerDto = new RegisterDto(
            "John",
            "Doe",
            "john.doe@example.com",
            "Password123",
            testAddress,
            UserRole.USER
    );

    when(userRepository.findByEmail(eq(registerDto.email()))).thenReturn(Optional.of(testUser));

    // When & Then
    assertThrows(DuplicateResourceException.class, () ->
            authService.register(registerDto)
    );

    verify(userRepository).findByEmail(eq(registerDto.email()));
    verifyNoInteractions(cartRepository);
    verifyNoInteractions(passwordEncoder);
  }

  @Test
  @DisplayName("login - Should authenticate user and return tokens")
  void login_WithValidCredentials_ShouldReturnTokens() {
    // Given
    LoginDto loginDto = new LoginDto("john.doe@example.com", "Password123");
    String accessToken = "access-token";
    String refreshToken = "refresh-token";

    Authentication authentication = Mockito.mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(testUser);

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
    when(jwtService.generateAccessToken(eq(testUser.getUsername()))).thenReturn(accessToken);
    when(jwtService.generateRefreshToken(eq(testUser.getUsername()))).thenReturn(refreshToken);
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // When
    AuthResponse result = authService.login(loginDto);

    // Then
    assertNotNull(result);
    assertEquals(accessToken, result.accessToken());
    assertEquals(refreshToken, result.refreshToken());

    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(jwtService).generateAccessToken(eq(testUser.getUsername()));
    verify(jwtService).generateRefreshToken(eq(testUser.getUsername()));
    verify(userRepository).save(eq(testUser));

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    User capturedUser = userCaptor.getValue();
    assertNotNull(capturedUser.getUpdatedAt());
  }

  @Test
  @DisplayName("getCurrentUser - Should return current user data")
  void getCurrentUser_WithValidAuthentication_ShouldReturnUserData() {
    // Given
    Authentication authentication = Mockito.mock(Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getPrincipal()).thenReturn(testUser);

    UserMeDto userMeDto = new UserMeDto(
            testUser.getId(),
            testUser.getFirstName(),
            testUser.getLastName(),
            testUser.getEmail(),
            testUser.getRole().toString(),
            testUser.getAddress()
    );

    when(userRepository.findById(eq(testUser.getId()))).thenReturn(Optional.of(testUser));
    when(authMapper.toUserMeDto(any(User.class))).thenReturn(userMeDto);

    // When
    UserMeDto result = authService.getCurrentUser(authentication);

    // Then
    assertNotNull(result);
    assertEquals(userMeDto, result);

    verify(authentication).isAuthenticated();
    verify(authentication).getPrincipal();
    verify(userRepository).findById(eq(testUser.getId()));
    verify(authMapper).toUserMeDto(eq(testUser));
  }

  @Test
  @DisplayName("getCurrentUser - Should throw IllegalStateException when not authenticated")
  void getCurrentUser_WhenNotAuthenticated_ShouldThrowException() {
    // Given
    Authentication authentication = Mockito.mock(Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(false);

    // When & Then
    assertThrows(IllegalStateException.class, () ->
            authService.getCurrentUser(authentication)
    );

    verify(authentication).isAuthenticated();
    verifyNoInteractions(userRepository);
    verifyNoInteractions(authMapper);
  }

  @Test
  @DisplayName("getCurrentUser - Should throw IllegalStateException when authentication is null")
  void getCurrentUser_WithNullAuthentication_ShouldThrowException() {
    // When & Then
    assertThrows(NullPointerException.class, () ->
            authService.getCurrentUser(null)
    );

    verifyNoInteractions(userRepository);
    verifyNoInteractions(authMapper);
  }

  @Test
  @DisplayName("invalidateUserSessions - Should update user to invalidate sessions")
  void invalidateUserSessions_ShouldUpdateUser() {
    // Given
    Long userId = testUser.getId();

    when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser));
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // When
    authService.invalidateUserSessions(userId);

    // Then
    verify(userRepository).findById(eq(userId));
    verify(userRepository).save(eq(testUser));

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    User capturedUser = userCaptor.getValue();
    assertNotNull(capturedUser.getUpdatedAt());
  }

  @Test
  @DisplayName("invalidateUserSessions - Should throw IllegalStateException when user not found")
  void invalidateUserSessions_WhenUserNotFound_ShouldThrowException() {
    // Given
    Long userId = 999L;

    when(userRepository.findById(eq(userId))).thenReturn(Optional.empty());

    // When & Then
    assertThrows(IllegalStateException.class, () ->
            authService.invalidateUserSessions(userId)
    );

    verify(userRepository).findById(eq(userId));
    verifyNoMoreInteractions(userRepository);
  }
}