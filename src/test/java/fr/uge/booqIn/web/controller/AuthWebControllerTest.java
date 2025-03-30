package fr.uge.booqIn.web.controller;

import fr.uge.booqIn.controller.web.AuthWebController;
import fr.uge.booqIn.controller.web.HomeWebController;
import fr.uge.booqIn.dto.auth.RegisterDto;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.service.interfaces.AuthServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("AuthWebController Tests")
class AuthWebControllerTest {

  private MockMvc mockMvc;

  @Mock
  private AuthServiceInterface authService;

  @Mock
  private HomeWebController homeWebController;

  @InjectMocks
  private AuthWebController authWebController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
    viewResolver.setPrefix("/WEB-INF/templates/");
    viewResolver.setSuffix(".html");

    mockMvc = MockMvcBuilders.standaloneSetup(authWebController)
            .setViewResolvers(viewResolver)
            .build();

    // Setup the behavior for homeWebController.prepareModel
    // We use isNull() because in the AuthWebController the principal will be null in most tests
    doNothing().when(homeWebController).prepareModel(any(Model.class), isNull(), anyString());
  }

  @Test
  @DisplayName("GET /auth/login - Should show login form")
  void showLoginForm_ShouldReturnLoginPage() throws Exception {
    // When & Then
    mockMvc.perform(get("/auth/login"))
            .andExpect(status().isOk())
            .andExpect(view().name("layout"))
            .andExpect(model().attributeExists("loginDto"));

    verify(homeWebController).prepareModel(any(Model.class), isNull(), eq("login"));
  }

  @Test
  @DisplayName("GET /auth/register - Should show registration form")
  void showSignUpForm_ShouldReturnRegisterPage() throws Exception {
    // When & Then
    mockMvc.perform(get("/auth/register"))
            .andExpect(status().isOk())
            .andExpect(view().name("layout"))
            .andExpect(model().attributeExists("registerDto"));

    verify(homeWebController).prepareModel(any(Model.class), isNull(), eq("register"));
  }

  @Test
  @DisplayName("POST /auth/register - Should redirect to home on successful registration")
  void processRegisterForm_ShouldRedirectToHome_OnSuccess() throws Exception {
    // Given
    when(authService.register(any(RegisterDto.class))).thenReturn(new UserIndex());

    // When & Then
    mockMvc.perform(post("/auth/register")
                    .param("firstName", "John")
                    .param("lastName", "Doe")
                    .param("email", "john.doe@example.com")
                    .param("password", "password123"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/"));
  }

  @Test
  @DisplayName("POST /auth/register - Should return to form with error on failure")
  void processRegisterForm_ShouldReturnForm_OnFailure() throws Exception {
    // Given
    when(authService.register(any(RegisterDto.class))).thenThrow(new RuntimeException("Registration failed"));

    // When & Then
    mockMvc.perform(post("/auth/register")
                    .param("firstName", "John")
                    .param("lastName", "Doe")
                    .param("email", "john.doe@example.com")
                    .param("password", "password123"))
            .andExpect(status().isOk())
            .andExpect(view().name("layout"))
            .andExpect(model().attributeExists("error"))
            .andExpect(model().attributeExists("registerDto"));

    verify(homeWebController).prepareModel(any(Model.class), isNull(), eq("register"));
  }
}