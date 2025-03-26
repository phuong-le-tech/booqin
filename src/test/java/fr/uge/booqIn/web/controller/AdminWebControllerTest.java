package fr.uge.booqIn.web.controller;

import fr.uge.booqIn.controller.web.AdminWebController;
import fr.uge.booqIn.controller.web.HomeWebController;
import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.bookCollection.BookCollectionIndex;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.service.interfaces.BookCollectionServiceInterface;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AdminWebControllerTest {

  private MockMvc mockMvc;

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

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(adminWebController).build();
  }

  @Test
  void testAdminDashboardUsersTab() throws Exception {
    // Arrange
    List<UserIndex> userList = new ArrayList<>();
    userList.add(new UserIndex(1L, "John", "Doe", "john@example.com", null, UserRole.USER));
    Page<UserIndex> userPage = new PageImpl<>(userList);

    when(userService.findUsers(anyString(), anyString(), anyString(), any(Pageable.class)))
            .thenReturn(userPage);

    // Act & Assert
    mockMvc.perform(get("/admin")
                    .param("tab", "users")
                    .param("page", "0")
                    .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(view().name("layout"))
            .andExpect(model().attributeExists("users"))
            .andExpect(model().attribute("activeTab", "users"));

    verify(userService).findUsers(anyString(), anyString(), anyString(), any(Pageable.class));
  }

  @Test
  void testAdminDashboardBooksTab() throws Exception {
    // Arrange
    List<BookIndex> bookList = new ArrayList<>();
    Page<BookIndex> bookPage = new PageImpl<>(bookList);

    when(bookService.findBooks(anyString(), anyString(), anyString(), any(Pageable.class)))
            .thenReturn(bookPage);

    // Act & Assert
    mockMvc.perform(get("/admin")
                    .param("tab", "books")
                    .param("page", "0")
                    .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(view().name("layout"))
            .andExpect(model().attributeExists("books"))
            .andExpect(model().attribute("activeTab", "books"));

    verify(bookService).findBooks(anyString(), anyString(), anyString(), any(Pageable.class));
  }

  @Test
  void testAdminDashboardCollectionsTab() throws Exception {
    // Arrange
    List<BookCollectionIndex> collectionList = new ArrayList<>();
    Page<BookCollectionIndex> collectionPage = new PageImpl<>(collectionList);

    when(bookCollectionService.findBookCollections(anyString(), anyString(), anyBoolean(), any(Pageable.class)))
            .thenReturn(collectionPage);

    // Act & Assert
    mockMvc.perform(get("/admin")
                    .param("tab", "collections")
                    .param("page", "0")
                    .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(view().name("layout"))
            .andExpect(model().attributeExists("collections"))
            .andExpect(model().attribute("activeTab", "collections"));

    verify(bookCollectionService).findBookCollections(anyString(), anyString(), anyBoolean(), any(Pageable.class));
  }

  @Test
  void testShowAddUserForm() throws Exception {
    mockMvc.perform(get("/admin/users/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("layout"))
            .andExpect(model().attributeExists("user"))
            .andExpect(model().attribute("title", "BooqIn - Add a user"))
            .andExpect(model().attribute("content", "pages/admin/add-user"));

    verify(homeWebController).prepareModel(any(Model.class), any(Principal.class), eq("admin"));
  }

  @Test
  void testProcessAddUserForm_Success() {
    // Arrange
    User user = new User();
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setEmail("john@example.com");

    when(userService.createUser(any(User.class))).thenReturn(user);

    // Act
    String result = adminWebController.processAddUserForm(user, UserRole.USER, model, principal, redirectAttributes);

    // Assert
    assertEquals("redirect:/admin?tab=users", result);
    verify(redirectAttributes).addFlashAttribute("success", "User added with success");
    verify(userService).createUser(user);
  }

  @Test
  void testProcessAddUserForm_Error() {
    // Arrange
    User user = new User();
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setEmail("john@example.com");

    when(userService.createUser(any(User.class))).thenThrow(new RuntimeException("Email already exists"));

    // Act
    String result = adminWebController.processAddUserForm(user, UserRole.USER, model, principal, redirectAttributes);

    // Assert
    assertEquals("layout", result);
    verify(model).addAttribute("error", "Email already exists");
    verify(model).addAttribute("user", user);
    verify(model).addAttribute("title", "BooqIn - Add a user");
    verify(model).addAttribute("content", "pages/admin/add-user");
  }

  @Test
  void testShowEditUserForm_Success() throws Exception {
    // Arrange
    Long userId = 1L;
    UserShow userShow = new UserShow(userId, "John", "Doe", "john@example.com", null, new ArrayList<>(), UserRole.USER);

    when(userService.findById(userId)).thenReturn(userShow);

    // Act & Assert
    mockMvc.perform(get("/admin/users/{id}/edit", userId))
            .andExpect(status().isOk())
            .andExpect(view().name("layout"))
            .andExpect(model().attributeExists("user"))
            .andExpect(model().attribute("role", UserRole.USER.getRole()))
            .andExpect(model().attribute("title", "BooqIn - Modifier l'utilisateur"))
            .andExpect(model().attribute("content", "pages/admin/edit-user"));

    verify(userService).findById(userId);
  }

  @Test
  void testShowEditUserForm_Error() throws Exception {
    // Arrange
    Long userId = 1L;
    when(userService.findById(userId)).thenThrow(new RuntimeException("User not found"));

    // Act & Assert
    mockMvc.perform(get("/admin/users/{id}/edit", userId))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin?tab=users"));
  }

  @Test
  void testProcessEditUserForm_Success() {
    // Arrange
    Long userId = 1L;
    User user = new User();
    user.setId(userId);
    user.setFirstName("Updated");
    user.setLastName("User");
    user.setEmail("updated@example.com");

    when(userService.updateUser(any(User.class))).thenReturn(user);

    // Act
    String result = adminWebController.processEditUserForm(userId, user, UserRole.USER, model, principal, redirectAttributes);

    // Assert
    assertEquals("redirect:/admin?tab=users", result);
    verify(redirectAttributes).addFlashAttribute("success", "User removed with success");
    verify(userService).updateUser(user);
  }

  @Test
  void testProcessEditUserForm_Error() {
    // Arrange
    Long userId = 1L;
    User user = new User();
    user.setId(userId);
    user.setEmail("invalid-email");

    when(userService.updateUser(any(User.class))).thenThrow(new RuntimeException("Invalid email format"));

    // Act
    String result = adminWebController.processEditUserForm(userId, user, UserRole.USER, model, principal, redirectAttributes);

    // Assert
    assertEquals("layout", result);
    verify(model).addAttribute("error", "Invalid email format");
    verify(model).addAttribute("user", user);
    verify(model).addAttribute("userRoles", UserRole.USER.getRole());
    verify(model).addAttribute("title", "BooqIn - Edit the user");
    verify(model).addAttribute("content", "pages/admin/edit-user");
  }

  @Test
  void testDeleteUser_Success() {
    // Arrange
    Long userId = 1L;
    doNothing().when(userService).deleteUser(userId);

    // Act
    String result = adminWebController.deleteUser(userId, redirectAttributes);

    // Assert
    assertEquals("redirect:/admin?tab=users", result);
    verify(redirectAttributes).addFlashAttribute("success", "User removed with success");
    verify(userService).deleteUser(userId);
  }

  @Test
  void testDeleteUser_Error() {
    // Arrange
    Long userId = 1L;
    doThrow(new RuntimeException("Cannot delete user")).when(userService).deleteUser(userId);

    // Act
    String result = adminWebController.deleteUser(userId, redirectAttributes);

    // Assert
    assertEquals("redirect:/admin?tab=users", result);
    verify(redirectAttributes).addFlashAttribute(eq("error"), contains("Error while removing"));
    verify(userService).deleteUser(userId);
  }
}