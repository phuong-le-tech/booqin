package fr.uge.booqIn.web.controller;

import fr.uge.booqIn.controller.web.AdminWebController;
import fr.uge.booqIn.controller.web.HomeWebController;
import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.bookCollection.BookCollectionIndex;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.factory.fixture.AddressFactory;
import fr.uge.booqIn.model.BookCollection;
import fr.uge.booqIn.service.interfaces.BookCollectionServiceInterface;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("AdminWebController Tests")
class AdminWebControllerTest {

  @Mock
  private UserServiceInterface userService;

  @Mock
  private BookServiceInterface bookService;

  @Mock
  private BookCollectionServiceInterface bookCollectionService;

  @Mock
  private HomeWebController homeWebController;

  @Mock
  private Model model;

  @Mock
  private Principal principal;

  @Mock
  private RedirectAttributes redirectAttributes;

  @InjectMocks
  private AdminWebController adminWebController;

  private User testUser;
  private UserShow testUserShow;
  private UserIndex testUserIndex;
  private BookIndex testBookIndex;
  private BookCollectionIndex testCollectionIndex;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    // Setup test data
    testUser = new User();
    testUser.setId(1L);
    testUser.setFirstName("John");
    testUser.setLastName("Doe");
    testUser.setEmail("john.doe@example.com");
    testUser.setPassword("Password123");
    testUser.setAddress(AddressFactory.createAddress());
    testUser.setRole(UserRole.USER);

    testUserShow = new UserShow(
            testUser.getId(),
            testUser.getFirstName(),
            testUser.getLastName(),
            testUser.getEmail(),
            testUser.getAddress(),
            Collections.emptyList(),
            testUser.getRole()
    );

    testUserIndex = new UserIndex(
            testUser.getId(),
            testUser.getFirstName(),
            testUser.getLastName(),
            testUser.getEmail(),
            testUser.getAddress(),
            testUser.getRole()
    );

    testBookIndex = new BookIndex(
            1L,
            "Test Book",
            Collections.singleton("Test Author"),
            new Date()
    );

    testCollectionIndex = new BookCollectionIndex(
            1L,
            "Test Collection",
            "Test Description",
            "John Doe",
            1L,
            0,
            true,
            BookCollection.CollectionType.NORMAL
    );

    doNothing().when(homeWebController).prepareModel(any(Model.class), any(Principal.class), anyString());
  }

  @Test
  @DisplayName("adminDashboard - Should show users tab by default")
  void adminDashboard_Default_ShouldShowUsersTab() {
    // Given
    String activeTab = "users";
    int page = 0;
    int size = 10;
    String sortField = "id";
    String sortDirection = "asc";

    List<UserIndex> userList = Collections.singletonList(testUserIndex);
    Page<UserIndex> userPage = new PageImpl<>(userList);

    when(userService.findUsers(eq(null), eq(null), eq(null), any(Pageable.class))).thenReturn(userPage);

    // When
    String viewName = adminWebController.adminDashboard(
            model, principal, activeTab, page, size, sortField, sortDirection,
            null, null, null, null, null, null, null, false, null
    );

    // Then
    assertEquals("layout", viewName);

    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("admin"));
    verify(model).addAttribute(eq("activeTab"), eq(activeTab));
    verify(userService).findUsers(eq(null), eq(null), eq(null), any(Pageable.class));
    verify(model).addAttribute(eq("users"), eq(userPage));
    verify(model).addAttribute(eq("title"), eq("BooqIn - Administration"));
    verify(model).addAttribute(eq("content"), eq("pages/admin/admin-panel"));
  }

  @Test
  @DisplayName("adminDashboard - Should show books tab when selected")
  void adminDashboard_BooksTab_ShouldShowBooksTab() {
    // Given
    String activeTab = "books";
    int page = 0;
    int size = 10;
    String sortField = "id";
    String sortDirection = "asc";

    List<BookIndex> bookList = Collections.singletonList(testBookIndex);
    Page<BookIndex> bookPage = new PageImpl<>(bookList);

    when(bookService.findBooks(eq(null), eq(null), eq(null), any(Pageable.class))).thenReturn(bookPage);

    // When
    String viewName = adminWebController.adminDashboard(
            model, principal, activeTab, page, size, sortField, sortDirection,
            null, null, null, null, null, null, null, false, null
    );

    // Then
    assertEquals("layout", viewName);

    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("admin"));
    verify(model).addAttribute(eq("activeTab"), eq(activeTab));
    verify(bookService).findBooks(eq(null), eq(null), eq(null), any(Pageable.class));
    verify(model).addAttribute(eq("books"), eq(bookPage));
    verify(model).addAttribute(eq("title"), eq("BooqIn - Administration"));
    verify(model).addAttribute(eq("content"), eq("pages/admin/admin-panel"));
  }

  @Test
  @DisplayName("adminDashboard - Should show collections tab when selected")
  void adminDashboard_CollectionsTab_ShouldShowCollectionsTab() {
    // Given
    String activeTab = "collections";
    int page = 0;
    int size = 10;
    String sortField = "id";
    String sortDirection = "asc";

    List<BookCollectionIndex> collectionList = Collections.singletonList(testCollectionIndex);
    Page<BookCollectionIndex> collectionPage = new PageImpl<>(collectionList);

    when(bookCollectionService.findBookCollections(eq(null), eq(null), eq(false), any(Pageable.class))).thenReturn(collectionPage);

    // When
    String viewName = adminWebController.adminDashboard(
            model, principal, activeTab, page, size, sortField, sortDirection,
            null, null, null, null, null, null, null, false, null
    );

    // Then
    assertEquals("layout", viewName);

    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("admin"));
    verify(model).addAttribute(eq("activeTab"), eq(activeTab));
    verify(bookCollectionService).findBookCollections(eq(null), eq(null), eq(false), any(Pageable.class));
    verify(model).addAttribute(eq("collections"), eq(collectionPage));
    verify(model).addAttribute(eq("title"), eq("BooqIn - Administration"));
    verify(model).addAttribute(eq("content"), eq("pages/admin/admin-panel"));
  }

  @Test
  @DisplayName("adminDashboard - Should use search parameter for users tab")
  void adminDashboard_UsersTabWithSearch_ShouldUseSearchParameter() {
    // Given
    String activeTab = "users";
    int page = 0;
    int size = 10;
    String sortField = "id";
    String sortDirection = "asc";
    String search = "john";

    List<UserIndex> userList = Collections.singletonList(testUserIndex);
    Page<UserIndex> userPage = new PageImpl<>(userList);

    when(userService.findUsers(eq(search), eq(search), eq(search), any(Pageable.class))).thenReturn(userPage);

    // When
    String viewName = adminWebController.adminDashboard(
            model, principal, activeTab, page, size, sortField, sortDirection,
            null, null, null, null, null, null, null, false, search
    );

    // Then
    assertEquals("layout", viewName);

    verify(userService).findUsers(eq(search), eq(search), eq(search), any(Pageable.class));
    verify(model).addAttribute(eq("users"), eq(userPage));
  }

  @Test
  @DisplayName("showAddUserForm - Should show add user form")
  void showAddUserForm_ShouldShowAddUserForm() {
    // When
    String viewName = adminWebController.showAddUserForm(model, principal);

    // Then
    assertEquals("layout", viewName);

    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("admin"));
    verify(model).addAttribute(eq("user"), any(User.class));
    verify(model).addAttribute(eq("title"), eq("BooqIn - Add a user"));
    verify(model).addAttribute(eq("content"), eq("pages/admin/add-user"));
  }

  @Test
  @DisplayName("processAddUserForm - Should add user and redirect on success")
  void processAddUserForm_Success_ShouldRedirect() {
    // Given
    when(userService.createUser(any(User.class))).thenReturn(testUser);

    // When
    String viewName = adminWebController.processAddUserForm(testUser, UserRole.USER, model, principal, redirectAttributes);

    // Then
    assertEquals("redirect:/admin?tab=users", viewName);

    verify(userService).createUser(eq(testUser));
    verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
  }

  @Test
  @DisplayName("processAddUserForm - Should show error on failure")
  void processAddUserForm_Failure_ShouldShowError() {
    // Given
    when(userService.createUser(any(User.class))).thenThrow(new RuntimeException("Test error"));

    // When
    String viewName = adminWebController.processAddUserForm(testUser, UserRole.USER, model, principal, redirectAttributes);

    // Then
    assertEquals("layout", viewName);

    verify(userService).createUser(eq(testUser));
    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("admin"));
    verify(model).addAttribute(eq("error"), anyString());
    verify(model).addAttribute(eq("user"), eq(testUser));
    verify(model).addAttribute(eq("title"), eq("BooqIn - Add a user"));
    verify(model).addAttribute(eq("content"), eq("pages/admin/add-user"));
  }

  @Test
  @DisplayName("showEditUserForm - Should show edit user form")
  void showEditUserForm_Success_ShouldShowEditForm() {
    // Given
    Long userId = 1L;
    when(userService.findById(eq(userId))).thenReturn(testUserShow);

    // When
    String viewName = adminWebController.showEditUserForm(userId, model, principal);

    // Then
    assertEquals("layout", viewName);

    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("admin"));
    verify(userService).findById(eq(userId));
    verify(model).addAttribute(eq("user"), eq(testUserShow));
    verify(model).addAttribute(eq("role"), eq(testUserShow.role().getRole()));
    verify(model).addAttribute(eq("title"), eq("BooqIn - Modifier l'utilisateur"));
    verify(model).addAttribute(eq("content"), eq("pages/admin/edit-user"));
  }

  @Test
  @DisplayName("showEditUserForm - Should redirect on user not found")
  void showEditUserForm_UserNotFound_ShouldRedirect() {
    // Given
    Long userId = 999L;
    when(userService.findById(eq(userId))).thenThrow(new ResourceNotFoundException("User not found"));

    // When
    String viewName = adminWebController.showEditUserForm(userId, model, principal);

    // Then
    assertEquals("redirect:/admin?tab=users", viewName);

    verify(userService).findById(eq(userId));
  }

  @Test
  @DisplayName("processEditUserForm - Should update user and redirect on success")
  void processEditUserForm_Success_ShouldRedirect() {
    // Given
    Long userId = 1L;
    when(userService.updateUser(any(User.class))).thenReturn(testUser);

    // When
    String viewName = adminWebController.processEditUserForm(userId, testUser, UserRole.USER, model, principal, redirectAttributes);

    // Then
    assertEquals("redirect:/admin?tab=users", viewName);

    verify(userService).updateUser(eq(testUser));
    verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
  }

  @Test
  @DisplayName("processEditUserForm - Should show error on failure")
  void processEditUserForm_Failure_ShouldShowError() {
    // Given
    Long userId = 1L;
    when(userService.updateUser(any(User.class))).thenThrow(new RuntimeException("Test error"));

    // When
    String viewName = adminWebController.processEditUserForm(userId, testUser, UserRole.USER, model, principal, redirectAttributes);

    // Then
    assertEquals("layout", viewName);

    verify(userService).updateUser(eq(testUser));
    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("admin"));
    verify(model).addAttribute(eq("error"), anyString());
    verify(model).addAttribute(eq("user"), eq(testUser));
    verify(model).addAttribute(eq("userRoles"), eq(UserRole.USER.getRole()));
    verify(model).addAttribute(eq("title"), eq("BooqIn - Edit the user"));
    verify(model).addAttribute(eq("content"), eq("pages/admin/edit-user"));
  }

  @Test
  @DisplayName("deleteUser - Should delete user and redirect with success message")
  void deleteUser_Success_ShouldRedirectWithSuccessMessage() {
    // Given
    Long userId = 1L;
    doNothing().when(userService).deleteUser(eq(userId));

    // When
    String viewName = adminWebController.deleteUser(userId, redirectAttributes);

    // Then
    assertEquals("redirect:/admin?tab=users", viewName);

    verify(userService).deleteUser(eq(userId));
    verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
  }

  @Test
  @DisplayName("deleteUser - Should redirect with error message on failure")
  void deleteUser_Failure_ShouldRedirectWithErrorMessage() {
    // Given
    Long userId = 1L;
    doThrow(new RuntimeException("Test error")).when(userService).deleteUser(eq(userId));

    // When
    String viewName = adminWebController.deleteUser(userId, redirectAttributes);

    // Then
    assertEquals("redirect:/admin?tab=users", viewName);

    verify(userService).deleteUser(eq(userId));
    verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
  }
}