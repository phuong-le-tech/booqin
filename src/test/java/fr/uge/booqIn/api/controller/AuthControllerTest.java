package fr.uge.booqIn.api.controller;

import fr.uge.booqIn.controller.api.AuthController;
import fr.uge.booqIn.dto.auth.AuthResponse;
import fr.uge.booqIn.dto.auth.LoginDto;
import fr.uge.booqIn.dto.auth.RegisterDto;
import fr.uge.booqIn.dto.auth.UserMeDto;
import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.factory.fixture.AddressFactory;
import fr.uge.booqIn.service.JwtService;
import fr.uge.booqIn.service.interfaces.AuthServiceInterface;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("AuthController Tests")
class AuthControllerTest {

  private AuthServiceInterface authService;
  private JwtService jwtService;
  private AuthController authController;
  private HttpServletResponse response;
  private HttpServletRequest request;
  private Authentication authentication;

  @BeforeEach
  void setUp() {
    authService = Mockito.mock(AuthServiceInterface.class);
    jwtService = Mockito.mock(JwtService.class);
    authController = new AuthController(authService, jwtService);

    // Set token expiration values via reflection
    ReflectionTestUtils.setField(authController, "ACCESS_TOKEN_EXPIRATION", 900000L); // 15 minutes
    ReflectionTestUtils.setField(authController, "REFRESH_TOKEN_EXPIRATION", 604800000L); // 7 days

    response = Mockito.mock(HttpServletResponse.class);
    request = Mockito.mock(HttpServletRequest.class);
    authentication = Mockito.mock(Authentication.class);
  }

  @Test
  @DisplayName("POST /login - Should authenticate user and return tokens")
  void login_ShouldAuthenticateAndReturnTokens() {
    // Given
    LoginDto loginDto = new LoginDto("john.doe@example.com", "password123");
    AuthResponse authResponse = new AuthResponse("access_token_value", "refresh_token_value");

    when(authService.login(any(LoginDto.class))).thenReturn(authResponse);

    // When
    ResponseEntity<ApiResponse<AuthResponse>> responseEntity = authController.login(loginDto, response);

    // Then
    assertNotNull(responseEntity);
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    ApiResponse<AuthResponse> apiResponse = responseEntity.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Login successful", apiResponse.message());
    assertEquals(authResponse, apiResponse.data());

    // Verify cookies were added
    ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
    verify(response, times(2)).addCookie(cookieCaptor.capture());

    // Verify service was called
    verify(authService).login(eq(loginDto));
  }

  @Test
  @DisplayName("POST /register - Should register user and return created user")
  void register_ShouldCreateAndReturnUser() {
    // Given
    RegisterDto registerDto = new RegisterDto("John", "Doe", "john.doe@example.com", "Password123", null, UserRole.USER);
    User createdUser = new User("John", "Doe", "john.doe@example.com", "Password123", null, UserRole.USER);
    createdUser.setId(1L);

    when(authService.register(any(RegisterDto.class))).thenReturn(createdUser);

    // When
    ResponseEntity<ApiResponse<User>> responseEntity = authController.signUp(registerDto);

    // Then
    assertNotNull(responseEntity);
    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    ApiResponse<User> apiResponse = responseEntity.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("User registered successfully", apiResponse.message());
    assertEquals(createdUser, apiResponse.data());

    // Verify service was called
    verify(authService).register(eq(registerDto));
  }

  @Test
  @DisplayName("POST /logout - Should logout user and clear cookies")
  void logout_ShouldLogoutAndClearCookies() {
    // Given
    User user = new User("John", "Doe", "john.doe@example.com", "Password123", null, UserRole.USER);
    user.setId(1L);

    Cookie refreshCookie = new Cookie("jwt-refresh", "refresh_token_value");
    Cookie[] cookies = {refreshCookie};

    when(request.getCookies()).thenReturn(cookies);
    when(authentication.getPrincipal()).thenReturn(user);
    when(authentication.isAuthenticated()).thenReturn(true);

    // When
    ResponseEntity<ApiResponse<Void>> responseEntity = authController.logout(request, response, authentication);

    // Then
    assertNotNull(responseEntity);
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    ApiResponse<Void> apiResponse = responseEntity.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Logged out successfully", apiResponse.message());

    // Verify refresh token was revoked
    verify(jwtService).revokeToken(eq("refresh_token_value"));

    // Verify user sessions were invalidated
    verify(authService).invalidateUserSessions(eq(1L));

    // Verify cookies were cleared
    ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
    verify(response, times(2)).addCookie(cookieCaptor.capture());
  }

  @Test
  @DisplayName("GET /me - Should return current user data")
  void me_ShouldReturnCurrentUserData() {
    // Given
    Address address = AddressFactory.createAddress();
    UserMeDto userMeDto = new UserMeDto(1L, "John", "Doe", "john.doe@example.com", UserRole.USER.getRole(), address);
    when(authService.getCurrentUser(any(Authentication.class))).thenReturn(userMeDto);

    // When
    ResponseEntity<ApiResponse<UserMeDto>> responseEntity = authController.me(authentication);

    // Then
    assertNotNull(responseEntity);
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    ApiResponse<UserMeDto> apiResponse = responseEntity.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Current user data", apiResponse.message());
    assertEquals(userMeDto, apiResponse.data());

    // Verify service was called
    verify(authService).getCurrentUser(eq(authentication));
  }

  @Test
  @DisplayName("POST /refresh - Should refresh tokens when valid refresh token is provided")
  void refreshToken_WithValidToken_ShouldRefreshTokens() {
    // Given
    Cookie refreshCookie = new Cookie("jwt-refresh", "valid_refresh_token");
    Cookie[] cookies = {refreshCookie};

    when(request.getCookies()).thenReturn(cookies);
    when(jwtService.attemptTokenRefresh(eq("valid_refresh_token"), any(HttpServletResponse.class))).thenReturn(true);

    // When
    ResponseEntity<ApiResponse<Void>> responseEntity = authController.refreshToken(request, response);

    // Then
    assertNotNull(responseEntity);
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    ApiResponse<Void> apiResponse = responseEntity.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Tokens refreshed successfully", apiResponse.message());

    // Verify token refresh was attempted
    verify(jwtService).attemptTokenRefresh(eq("valid_refresh_token"), eq(response));
  }

  @Test
  @DisplayName("POST /refresh - Should return error when no refresh token is provided")
  void refreshToken_WithNoToken_ShouldReturnError() {
    // Given
    when(request.getCookies()).thenReturn(null);

    // When
    ResponseEntity<ApiResponse<Void>> responseEntity = authController.refreshToken(request, response);

    // Then
    assertNotNull(responseEntity);
    assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    ApiResponse<Void> apiResponse = responseEntity.getBody();
    assertNotNull(apiResponse);
    assertFalse(apiResponse.success());
    assertEquals("No refresh token provided", apiResponse.message());

    // Verify token refresh was not attempted
    verify(jwtService, never()).attemptTokenRefresh(anyString(), any(HttpServletResponse.class));
  }

  @Test
  @DisplayName("POST /refresh - Should return error when refresh token is invalid")
  void refreshToken_WithInvalidToken_ShouldReturnError() {
    // Given
    Cookie refreshCookie = new Cookie("jwt-refresh", "invalid_refresh_token");
    Cookie[] cookies = {refreshCookie};

    when(request.getCookies()).thenReturn(cookies);
    when(jwtService.attemptTokenRefresh(eq("invalid_refresh_token"), any(HttpServletResponse.class))).thenReturn(false);

    // When
    ResponseEntity<ApiResponse<Void>> responseEntity = authController.refreshToken(request, response);

    // Then
    assertNotNull(responseEntity);
    assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    ApiResponse<Void> apiResponse = responseEntity.getBody();
    assertNotNull(apiResponse);
    assertFalse(apiResponse.success());
    assertEquals("Token refresh failed, please login again", apiResponse.message());

    // Verify cookies were cleared
    ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
    verify(response, times(2)).addCookie(cookieCaptor.capture());
  }
}