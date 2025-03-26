package fr.uge.booqIn.web.controller;

import fr.uge.booqIn.controller.web.HomeWebController;
import fr.uge.booqIn.controller.web.ProfileWebController;
import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.dto.user.UserUpdate;
import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.factory.fixture.AddressFactory;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("ProfileWebController Tests")
class ProfileWebControllerTest {

  @Mock
  private UserServiceInterface userService;

  @Mock
  private HomeWebController homeWebController;

  @Mock
  private Model model;

  @Mock
  private Authentication principal; // Utilisez Authentication directement comme Principal

  @Mock
  private User authenticatedUser;

  @Mock
  private RedirectAttributes redirectAttributes;

  @InjectMocks
  private ProfileWebController profileWebController;

  private UserShow testUserShow;
  private UserUpdate testUserUpdate;
  private Address testAddress;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    // Setup test data
    testAddress = AddressFactory.createAddress();

    testUserShow = new UserShow(
            1L,
            "John",
            "Doe",
            "john.doe@example.com",
            testAddress,
            Collections.emptyList(),
            UserRole.USER
    );

    testUserUpdate = new UserUpdate(
            Optional.of("John"),
            Optional.of("Doe"),
            Optional.of("john.doe@example.com"),
            Optional.of(testAddress),
            Optional.empty()
    );

    // Common mock setup
    when(principal.getName()).thenReturn("john.doe@example.com");
    when(principal.getPrincipal()).thenReturn(authenticatedUser);
    when(authenticatedUser.getId()).thenReturn(1L);

    doNothing().when(homeWebController).prepareModel(any(Model.class), any(Principal.class), anyString());
  }

  @Test
  @DisplayName("showProfile - Should display profile for authenticated user")
  void showProfile_AuthenticatedUser_ShouldDisplayProfile() {
    // Given
    when(userService.findById(eq(1L))).thenReturn(testUserShow);

    // When
    String viewName = profileWebController.showProfile(model, principal);

    // Then
    assertEquals("layout", viewName);

    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("profile"));
    verify(userService).findById(eq(1L));
    verify(model).addAttribute(eq("user"), eq(testUserShow));
    verify(model).addAttribute(eq("title"), eq("BooqIn - My Profile"));
    verify(model).addAttribute(eq("content"), eq("pages/user/profile"));
  }

  @Test
  @DisplayName("showProfile - Should handle unauthenticated user")
  void showProfile_UnauthenticatedUser_ShouldHandleGracefully() {
    // Given
    Principal nullPrincipal = null;

    // When
    String viewName = profileWebController.showProfile(model, nullPrincipal);

    // Then
    assertEquals("layout", viewName);

    verify(homeWebController).prepareModel(eq(model), eq(nullPrincipal), eq("profile"));
    verify(userService, never()).findById(anyLong());
    verify(model).addAttribute(eq("title"), eq("BooqIn - My Profile"));
    verify(model).addAttribute(eq("content"), eq("pages/user/profile"));
  }

  @Test
  @DisplayName("showEditProfileForm - Should display edit form for authenticated user")
  void showEditProfileForm_AuthenticatedUser_ShouldDisplayEditForm() {
    // Given
    when(userService.findById(eq(1L))).thenReturn(testUserShow);

    // When
    String viewName = profileWebController.showEditProfileForm(model, principal);

    // Then
    assertEquals("layout", viewName);

    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("profile"));
    verify(userService).findById(eq(1L));
    verify(model).addAttribute(eq("user"), eq(testUserShow));
    verify(model).addAttribute(eq("userUpdate"), any(UserUpdate.class));
    verify(model).addAttribute(eq("firstName"), eq("John"));
    verify(model).addAttribute(eq("lastName"), eq("Doe"));
    verify(model).addAttribute(eq("email"), eq("john.doe@example.com"));
    verify(model).addAttribute(eq("title"), eq("BooqIn - Edit my profile"));
    verify(model).addAttribute(eq("content"), eq("pages/user/edit-profile"));
  }

  @Test
  @DisplayName("showEditProfileForm - Should handle unauthenticated user")
  void showEditProfileForm_UnauthenticatedUser_ShouldHandleGracefully() {
    // Given
    Principal nullPrincipal = null;

    // When
    String viewName = profileWebController.showEditProfileForm(model, nullPrincipal);

    // Then
    assertEquals("layout", viewName);

    verify(homeWebController).prepareModel(eq(model), eq(nullPrincipal), eq("profile"));
    verify(userService, never()).findById(anyLong());
    verify(model).addAttribute(eq("title"), eq("BooqIn - Edit my profile"));
    verify(model).addAttribute(eq("content"), eq("pages/user/edit-profile"));
  }

  @Test
  @DisplayName("processEditProfileForm - Should update profile and redirect on success")
  void processEditProfileForm_Success_ShouldUpdateAndRedirect() {
    // Given
    when(userService.patchUser(eq(1L), eq(testUserUpdate))).thenReturn(testUserUpdate);

    // When
    String viewName = profileWebController.processEditProfileForm(testUserUpdate, model, principal, redirectAttributes);

    // Then
    assertEquals("redirect:/profile", viewName);

    verify(userService).patchUser(eq(1L), eq(testUserUpdate));
    verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
  }

  @Test
  @DisplayName("processEditProfileForm - Should handle exception and show error")
  void processEditProfileForm_WithException_ShouldShowError() {
    // Given
    when(userService.patchUser(eq(1L), eq(testUserUpdate)))
            .thenThrow(new RuntimeException("Error updating profile"));

    // When
    String viewName = profileWebController.processEditProfileForm(testUserUpdate, model, principal, redirectAttributes);

    // Then
    assertEquals("layout", viewName);

    verify(userService).patchUser(eq(1L), eq(testUserUpdate));
    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("profile"));
    verify(model).addAttribute(eq("error"), anyString());
    verify(model).addAttribute(eq("userUpdate"), eq(testUserUpdate));
    verify(model).addAttribute(eq("isEditing"), eq(true));
    verify(model).addAttribute(eq("title"), eq("BooqIn - Edit my profile"));
    verify(model).addAttribute(eq("content"), eq("pages/user/edit-profile"));
  }

  @Test
  @DisplayName("processEditProfileForm - Should handle unauthenticated user")
  void processEditProfileForm_UnauthenticatedUser_ShouldRedirect() {
    // Given
    Principal nullPrincipal = null;

    // When
    String viewName = profileWebController.processEditProfileForm(testUserUpdate, model, nullPrincipal, redirectAttributes);

    // Then
    assertEquals("redirect:/profile", viewName);

    verify(userService, never()).patchUser(anyLong(), any(UserUpdate.class));
  }
}