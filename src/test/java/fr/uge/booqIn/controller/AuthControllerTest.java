package fr.uge.booqIn.controller;

import fr.uge.booqIn.controller.api.AuthController;
import fr.uge.booqIn.dto.auth.JwtDto;
import fr.uge.booqIn.dto.auth.SignInDto;
import fr.uge.booqIn.dto.auth.SignUpDto;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

  @Mock
  private AuthService authService;

  @InjectMocks
  private AuthController authController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testSignIn() {
    SignInDto signInDto = new SignInDto("user@example.com", "password123");
    when(authService.signIn(signInDto)).thenReturn("mocked-jwt-token");

    ResponseEntity<JwtDto> response = authController.signIn(signInDto);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("mocked-jwt-token", Objects.requireNonNull(response.getBody()).token());
  }

  @Test
  void testSignUp() {
    SignUpDto signUpDto = new SignUpDto("John", "Doe", "john.doe@example.com", "password", null, null);
    User mockUser = new User("John", "Doe", "john.doe@example.com", "hashed_password", null, null);
    when(authService.signUp(signUpDto)).thenReturn(mockUser);

    ResponseEntity<?> response = authController.signUp(signUpDto);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    assertEquals(mockUser, response.getBody());
  }

  @Test
  void testPublicRoute() {
    assertEquals("Hello World", authController.test());
  }

  @Test
  void testAdminRoute() {
    assertEquals("Hello Admin", authController.testAdmin());
  }
}
