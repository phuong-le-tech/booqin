package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.dto.user.UserUpdate;
import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class UserControllerTest {

  @Mock
  private UserServiceInterface userService;

  @InjectMocks
  private UserController userController;

  @Mock
  private SecurityContext securityContext;

  private User testUser;
  private User adminUser;
  private UserShow testUserShow;
  private UserIndex testUserIndex;

  @BeforeEach
  void setUp() {
    Address address = new Address("123", "Rue de Test", "TestVille", "75000");
    testUser = new User("Test", "User", "test.user@example.com", "Password123", address, UserRole.USER);
    testUser.setId(1L);

    adminUser = new User("Admin", "User", "admin@example.com", "AdminPass123", address, UserRole.ADMIN);
    adminUser.setId(2L);
    // TODO: à corriger
    /*testUserShow = new UserShow(
            1L,
            "Test",
            "User",
            "test.user@example.com",
            address,
            Collections.emptyList()
    );
    testUserIndex = new UserIndex(1L, "Test", "User", "test.user@example.com");

    SecurityContextHolder.setContext(securityContext);

    when(userService.findById(1L)).thenReturn(testUserShow);*/
  }

  @Test
  void testGetUsersAsAdminReturnsUsersList() {
    Authentication authentication = new UsernamePasswordAuthenticationToken(
            adminUser, null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
    );
    when(securityContext.getAuthentication()).thenReturn(authentication);

    List<UserIndex> userIndices = Collections.singletonList(testUserIndex);
    Page<UserIndex> userPage = new PageImpl<>(userIndices);
    when(userService.findUsers(any(), any(), any(), any(Pageable.class))).thenReturn(userPage);

    ResponseEntity<ApiResponse<Page<UserIndex>>> response = userController.getUsers(
            1, 10, "id", "asc", null, null, null
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Users found", response.getBody().message());
    assertEquals(userPage, response.getBody().data());
    verify(userService, times(1)).findUsers(any(), any(), any(), any(Pageable.class));
  }

  @Test
  void testGetUserByIdReturnsUser() {
    ResponseEntity<ApiResponse<UserShow>> response = userController.getUserById(1L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("User found", response.getBody().message());
    assertEquals(testUserShow, response.getBody().data());
    verify(userService, times(1)).findById(1L);
  }

  @Test
  void testCreateUserAsAdminCreatesUser() {
    Authentication authentication = new UsernamePasswordAuthenticationToken(
            adminUser, null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
    );
    when(securityContext.getAuthentication()).thenReturn(authentication);

    when(userService.createUser(any(User.class))).thenReturn(testUser);

    ResponseEntity<ApiResponse<User>> response = userController.createUser(testUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("User created successfully", response.getBody().message());
    assertEquals(testUser, response.getBody().data());
    verify(userService, times(1)).createUser(any(User.class));
  }

  @Test
  void testUpdateUserAsAdminUpdatesUser() {
    Authentication authentication = new UsernamePasswordAuthenticationToken(
            adminUser, null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
    );
    when(securityContext.getAuthentication()).thenReturn(authentication);

    when(userService.updateUser(any(User.class))).thenReturn(testUser);

    ResponseEntity<ApiResponse<User>> response = userController.updateUser(testUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("User updated successfully", response.getBody().message());
    assertEquals(testUser, response.getBody().data());
    verify(userService, times(1)).updateUser(any(User.class));
  }

  /*@Test
  void testPatchUserAsAdminPatchesUser() {
    Authentication authentication = new UsernamePasswordAuthenticationToken(
            adminUser, null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
    );
    when(securityContext.getAuthentication()).thenReturn(authentication);

    UserUpdate userUpdate = new UserUpdate(
            Optional.of("NewFirstName"),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
    );

    when(userService.patchUser(eq(1L), any(UserUpdate.class))).thenReturn(testUser);

    ResponseEntity<ApiResponse<User>> response = userController.patchUser(1L, userUpdate);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("User updated successfully", response.getBody().message());
    assertEquals(testUser, response.getBody().data());
    verify(userService, times(1)).patchUser(eq(1L), any(UserUpdate.class));
  }*/

  /*@Test
  void testPatchUserAsSameUserPatchesUser() {
    Authentication authentication = new UsernamePasswordAuthenticationToken(
            testUser, null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
    );
    when(securityContext.getAuthentication()).thenReturn(authentication);

    UserUpdate userUpdate = new UserUpdate(
            Optional.of("NewFirstName"),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
    );

    when(userService.patchUser(eq(1L), any(UserUpdate.class))).thenReturn(testUser);

    ResponseEntity<ApiResponse<User>> response = userController.patchUser(1L, userUpdate);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("User updated successfully", response.getBody().message());
    assertEquals(testUser, response.getBody().data());
    verify(userService, times(1)).patchUser(eq(1L), any(UserUpdate.class));
  }*/

  @Test
  void testDeleteUserDeletesUser() {
    ResponseEntity<ApiResponse<Void>> response = userController.deleteUser(1L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("User deleted successfully", response.getBody().message());
    verify(userService, times(1)).deleteUser(1L);
  }
}