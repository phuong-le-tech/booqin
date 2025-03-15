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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;

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
    private HttpServletResponse httpResponse;
    private HttpServletRequest httpRequest;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        authService = Mockito.mock(AuthServiceInterface.class);
        jwtService = Mockito.mock(JwtService.class);
        authController = new AuthController(authService, jwtService);
        httpResponse = Mockito.mock(HttpServletResponse.class);
        httpRequest = Mockito.mock(HttpServletRequest.class);
        authentication = Mockito.mock(Authentication.class);
    }

    // Méthode d'aide pour créer une adresse de test
    private Address createTestAddress() {
        return new Address("123", "Test St", "Test City", "12345");
    }

    @Test
    @DisplayName("POST /auth/login - Retourne tokens et configure les cookies")
    void login_ShouldReturnTokensAndSetCookies() {
        // Given
        LoginDto loginDto = new LoginDto("test@example.com", "password");
        AuthResponse authResponse = new AuthResponse("access-token", "refresh-token");
        when(authService.login(any(LoginDto.class))).thenReturn(authResponse);

        // When
        ResponseEntity<ApiResponse<AuthResponse>> response = authController.login(loginDto, httpResponse);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<AuthResponse> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Login successful", apiResponse.message());
        assertEquals(authResponse, apiResponse.data());

        // Vérification que deux cookies ont été ajoutés
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(httpResponse, times(2)).addCookie(cookieCaptor.capture());
        verify(authService).login(eq(loginDto));
    }

    @Test
    @DisplayName("POST /auth/register - Crée et retourne un utilisateur")
    void register_ShouldReturnCreatedUser() {
        // Given
        Address address = createTestAddress();
        RegisterDto registerDto = new RegisterDto("John", "Doe", "john.doe@example.com", "Password123", address, UserRole.USER);
        User createdUser = new User("John", "Doe", "john.doe@example.com", "encoded-password", address, UserRole.USER);
        createdUser.setId(1L);
        when(authService.register(any(RegisterDto.class))).thenReturn(createdUser);

        // When
        ResponseEntity<ApiResponse<User>> response = authController.signUp(registerDto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ApiResponse<User> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("User registered successfully", apiResponse.message());
        assertEquals(createdUser, apiResponse.data());
        verify(authService).register(eq(registerDto));
    }

    @Test
    @DisplayName("POST /auth/logout - Révoque les tokens et efface les cookies (avec authentication)")
    void logout_ShouldRevokeTokensAndClearCookies() {
        // Given
        Long userId = 1L;
        Cookie[] cookies = {
                new Cookie("jwt", "access-token"),
                new Cookie("jwt-refresh", "refresh-token")
        };
        User mockUser = Mockito.mock(User.class);
        when(mockUser.getId()).thenReturn(userId);
        when(httpRequest.getCookies()).thenReturn(cookies);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        // When
        ResponseEntity<ApiResponse<Void>> response = authController.logout(httpRequest, httpResponse, authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<Void> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Logged out successfully", apiResponse.message());
        assertNull(apiResponse.data());

        // Vérification que les cookies ont été effacés
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(httpResponse, times(2)).addCookie(cookieCaptor.capture());
        // Vérification de la révocation des tokens et de l'invalidation de la session utilisateur
        verify(jwtService).revokeToken("access-token");
        verify(jwtService).revokeToken("refresh-token");
        verify(authService).invalidateUserSessions(eq(userId));
    }

    @Test
    @DisplayName("POST /auth/logout - Fonctionne sans authentication")
    void logout_WithoutAuthentication_ShouldStillWork() {
        // Given
        Cookie[] cookies = {
                new Cookie("jwt", "access-token"),
                new Cookie("jwt-refresh", "refresh-token")
        };
        when(httpRequest.getCookies()).thenReturn(cookies);

        // When
        ResponseEntity<ApiResponse<Void>> response = authController.logout(httpRequest, httpResponse, null);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Vérification que les cookies ont été effacés
        verify(httpResponse, times(2)).addCookie(any(Cookie.class));
        // Vérification de la révocation des tokens
        verify(jwtService).revokeToken("access-token");
        verify(jwtService).revokeToken("refresh-token");
        verify(authService, never()).invalidateUserSessions(anyLong());
    }

    @Test
    @DisplayName("POST /auth/refresh-token - Renouvelle le token et met à jour les cookies")
    void refreshToken_ShouldReturnNewTokensAndSetCookies() {
        // Given
        Cookie[] cookies = { new Cookie("jwt-refresh", "refresh-token") };
        AuthResponse authResponse = new AuthResponse("new-access-token", "new-refresh-token");
        when(httpRequest.getCookies()).thenReturn(cookies);
        when(jwtService.isTokenExpired("refresh-token")).thenReturn(false);
        when(jwtService.isTokenRevoked("refresh-token")).thenReturn(false);
        when(jwtService.recordRefreshTokenUsage("refresh-token")).thenReturn(true);
        when(authService.refreshToken("refresh-token")).thenReturn(authResponse);

        // When
        ResponseEntity<ApiResponse<String>> response = authController.refreshToken(httpRequest, httpResponse);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<String> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Access Token refreshed successfully", apiResponse.message());
        assertEquals("new-access-token", apiResponse.data());

        // Vérification que les cookies ont été mis à jour
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        verify(httpResponse, times(2)).addCookie(cookieCaptor.capture());
        verify(authService).refreshToken("refresh-token");
    }

    @Test
    @DisplayName("POST /auth/refresh-token - Avec token manquant, renvoie Unauthorized")
    void refreshToken_WithMissingToken_ShouldReturnUnauthorized() {
        // Given
        when(httpRequest.getCookies()).thenReturn(null);

        // When
        ResponseEntity<ApiResponse<String>> response = authController.refreshToken(httpRequest, httpResponse);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ApiResponse<String> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.success());
        assertEquals("Refresh token is missing", apiResponse.message());

        verify(authService, never()).refreshToken(anyString());
        verify(httpResponse, never()).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("POST /auth/refresh-token - Avec token expiré, renvoie Unauthorized")
    void refreshToken_WithExpiredToken_ShouldReturnUnauthorized() {
        // Given
        Cookie[] cookies = { new Cookie("jwt-refresh", "expired-token") };
        when(httpRequest.getCookies()).thenReturn(cookies);
        when(jwtService.isTokenExpired("expired-token")).thenReturn(true);

        // When
        ResponseEntity<ApiResponse<String>> response = authController.refreshToken(httpRequest, httpResponse);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ApiResponse<String> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.success());
        assertEquals("Refresh token has expired", apiResponse.message());

        verify(authService, never()).refreshToken(anyString());
        verify(httpResponse, never()).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("POST /auth/refresh-token - Avec token révoqué, renvoie Unauthorized")
    void refreshToken_WithRevokedToken_ShouldReturnUnauthorized() {
        // Given
        Cookie[] cookies = { new Cookie("jwt-refresh", "revoked-token") };
        when(httpRequest.getCookies()).thenReturn(cookies);
        when(jwtService.isTokenExpired("revoked-token")).thenReturn(false);
        when(jwtService.isTokenRevoked("revoked-token")).thenReturn(true);

        // When
        ResponseEntity<ApiResponse<String>> response = authController.refreshToken(httpRequest, httpResponse);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ApiResponse<String> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.success());
        assertEquals("Refresh token has been revoked", apiResponse.message());

        verify(authService, never()).refreshToken(anyString());
    }

    @Test
    @DisplayName("POST /auth/refresh-token - Avec token réutilisé, renvoie Unauthorized et révoque le token")
    void refreshToken_WithReusedToken_ShouldReturnUnauthorized() {
        // Given
        Cookie[] cookies = { new Cookie("jwt-refresh", "reused-token") };
        when(httpRequest.getCookies()).thenReturn(cookies);
        when(jwtService.isTokenExpired("reused-token")).thenReturn(false);
        when(jwtService.isTokenRevoked("reused-token")).thenReturn(false);
        when(jwtService.recordRefreshTokenUsage("reused-token")).thenReturn(false);

        // When
        ResponseEntity<ApiResponse<String>> response = authController.refreshToken(httpRequest, httpResponse);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        ApiResponse<String> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.success());
        assertEquals("Token reuse detected. Please login again.", apiResponse.message());

        verify(jwtService).revokeToken("reused-token");
        verify(authService, never()).refreshToken(anyString());
    }

    @Test
    @DisplayName("GET /auth/me - Retourne les données de l'utilisateur courant")
    void me_ShouldReturnCurrentUserData() {
        // Given
        UserMeDto userMeDto = new UserMeDto(1L, "John", "Doe", "john.doe@example.com", "ROLE_USER", createTestAddress());
        when(authService.getCurrentUser(any(Authentication.class))).thenReturn(userMeDto);

        // When
        ResponseEntity<ApiResponse<UserMeDto>> response = authController.me(authentication);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<UserMeDto> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Current user data", apiResponse.message());
        assertEquals(userMeDto, apiResponse.data());
        verify(authService).getCurrentUser(eq(authentication));
    }

    @Test
    @DisplayName("POST /auth/login - Gère l'exception BadCredentialsException")
    void loginWithException_ShouldHandleExceptions() {
        // Given
        LoginDto loginDto = new LoginDto("test@example.com", "password");
        when(authService.login(any(LoginDto.class))).thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () ->
                authController.login(loginDto, httpResponse)
        );
        assertEquals("Invalid credentials", exception.getMessage());
    }

    @Test
    @DisplayName("POST /auth/register - Gère l'exception pour email déjà existant")
    void registerWithDuplicateEmail_ShouldHandleException() {
        // Given
        Address address = createTestAddress();
        RegisterDto registerDto = new RegisterDto("John", "Doe", "existing@example.com", "Password123", address, UserRole.USER);
        when(authService.register(any(RegisterDto.class))).thenThrow(new IllegalArgumentException("Email already taken"));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                authController.signUp(registerDto)
        );
        assertEquals("Email already taken", exception.getMessage());
    }

    @Test
    @DisplayName("GET /auth/me - Gère l'exception d'authentification invalide")
    void meWithInvalidAuthentication_ShouldHandleException() {
        // Given
        when(authService.getCurrentUser(any(Authentication.class))).thenThrow(new IllegalStateException("User is not authenticated"));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                authController.me(authentication)
        );
        assertEquals("User is not authenticated", exception.getMessage());
    }
}
