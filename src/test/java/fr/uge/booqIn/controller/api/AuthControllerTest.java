package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.dto.auth.AuthResponse;
import fr.uge.booqIn.dto.auth.LoginDto;
import fr.uge.booqIn.dto.auth.RegisterDto;
import fr.uge.booqIn.dto.auth.UserMeDto;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.JwtService;
import fr.uge.booqIn.service.interfaces.AuthServiceInterface;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

  @Mock
  private AuthServiceInterface authService;

  @Mock
  private JwtService jwtService;

  @InjectMocks
  private AuthController authController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testSignUp() {
    RegisterDto signUpDto = new RegisterDto(
            "John",
            "Doe",
            "john.doe@example.com",
            "password",
            null,
            null
    );
    User mockUser = new User(
            "John",
            "Doe",
            "john.doe@example.com",
            "hashed_password",
            null,
            null
    );

    when(authService.register(signUpDto)).thenReturn(mockUser);

    ResponseEntity<ApiResponse<User>> response = authController.signUp(signUpDto);

    assertEquals(HttpStatus.CREATED, response.getStatusCode(),
            "Le statut doit être 201 CREATED.");

    ApiResponse<User> body = response.getBody();
    assertNotNull(body, "Le corps de la réponse ne doit pas être nul.");
    assertTrue(body.success(), "Le champ success doit être true.");
    assertEquals("User registered successfully", body.message());
    assertNotNull(body.data(), "Le champ data doit contenir l’utilisateur créé.");
    assertEquals(mockUser, body.data(),
            "L’utilisateur renvoyé doit correspondre à celui du service.");

    verify(authService, times(1)).register(signUpDto);
  }

  @Test
  void testLogin() {
    LoginDto loginDto = new LoginDto("john.doe@example.com", "password123");
    AuthResponse tokens = new AuthResponse("access-token", "refresh-token");

    when(authService.login(loginDto)).thenReturn(tokens);

    HttpServletResponse mockResponse = mock(HttpServletResponse.class);

    ResponseEntity<ApiResponse<AuthResponse>> response = authController.login(loginDto, mockResponse);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    ApiResponse<AuthResponse> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.success());
    assertEquals("Connexion réussie !", body.message());
    assertEquals(tokens, body.data());

    verify(authService, times(1)).login(loginDto);
    verify(mockResponse, atLeastOnce()).addCookie(any());
  }

  @Test
  void testLogout() {
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);

    ResponseEntity<ApiResponse<Void>> response = authController.logout(mockResponse);
    assertEquals(HttpStatus.OK, response.getStatusCode());

    ApiResponse<Void> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.success());
    assertEquals("Logged out successfully", body.message());
    assertNull(body.data());

    verify(mockResponse, times(2)).addCookie(any());
  }

  @Test
  void testRefreshToken_Success() {
    HttpServletRequest mockRequest = mock(HttpServletRequest.class);
    HttpServletResponse mockResponse = mock(HttpServletResponse.class);

    when(mockRequest.getCookies()).thenReturn(
            new jakarta.servlet.http.Cookie[] {
                    new jakarta.servlet.http.Cookie("jwt-refresh", "refresh-token-value")
            }
    );

    when(jwtService.extractUsername("refresh-token-value")).thenReturn("john.doe@example.com");
    when(jwtService.generateAccessToken("john.doe@example.com")).thenReturn("new-access-token");

    ResponseEntity<ApiResponse<String>> response = authController.refreshToken(mockRequest, mockResponse);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<String> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.success());
    assertEquals("Access Token refreshed successfully", body.message());
    assertEquals("new-access-token", body.data());
  }

  @Test
  void testMe() {
    Authentication authMock = mock(Authentication.class);
    UserMeDto meDto = new UserMeDto(1L, "John", "Doe", "john.doe@example.com");

    when(authService.getCurrentUser(authMock)).thenReturn(meDto);

    ResponseEntity<ApiResponse<UserMeDto>> response = authController.me(authMock);
    assertEquals(HttpStatus.OK, response.getStatusCode());

    ApiResponse<UserMeDto> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.success());
    assertEquals("Current user data", body.message());
    assertEquals(meDto, body.data());
  }
}
