package fr.uge.booqIn.api.controller;

import fr.uge.booqIn.controller.api.UserController;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.dto.user.UserUpdate;
import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("UserController Tests")
class UserControllerTest {

    private UserServiceInterface userService;
    private UserController userController;

    @BeforeEach
    void setUp() {
        userService = Mockito.mock(UserServiceInterface.class);
        userController = new UserController(userService);
    }

    // Méthode d'aide pour créer une adresse de test
    private Address createTestAddress() {
        return new Address("123", "Main St", "New York", "10001");
    }

    @Test
    @DisplayName("GET /users - Retourne une page d'utilisateurs avec tri ascendant")
    void getUsers_ShouldReturnPageOfUsers() {
        // Given
        int page = 0;
        int size = 10;
        String sortField = "id";
        String sortDirection = "asc";
        String firstName = "John";
        String lastName = "Doe";
        String email = "john.doe@example.com";

        List<UserIndex> userList = List.of(
                new UserIndex(1L, "John", "Doe", "john.doe@example.com", null, UserRole.USER),
                new UserIndex(2L, "Jane", "Doe", "jane.doe@example.com", null, UserRole.USER)
        );
        Page<UserIndex> userPage = new PageImpl<>(userList);

        when(userService.findUsers(eq(firstName), eq(lastName), eq(email), any(PageRequest.class)))
                .thenReturn(userPage);

        // When
        ResponseEntity<ApiResponse<Page<UserIndex>>> response = userController.getUsers(
                page, size, sortField, sortDirection, firstName, lastName, email);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<Page<UserIndex>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Users found", apiResponse.message());
        assertEquals(userPage, apiResponse.data());

        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(userService).findUsers(eq(firstName), eq(lastName), eq(email), pageRequestCaptor.capture());
        PageRequest capturedPageRequest = pageRequestCaptor.getValue();
        assertEquals(page, capturedPageRequest.getPageNumber());
        assertEquals(size, capturedPageRequest.getPageSize());
        assertEquals(Sort.by(sortField).ascending(), capturedPageRequest.getSort());
    }

    @Test
    @DisplayName("GET /users - Avec tri descendant, retourne une page d'utilisateurs")
    void getUsers_WithDescendingSort_ShouldReturnPageOfUsers() {
        // Given
        int page = 0;
        int size = 10;
        String sortField = "id";
        String sortDirection = "desc";
        String firstName = null;
        String lastName = null;
        String email = null;

        List<UserIndex> userList = List.of(
                new UserIndex(2L, "Jane", "Doe", "jane.doe@example.com", null, UserRole.USER),
                new UserIndex(1L, "John", "Doe", "john.doe@example.com", null, UserRole.USER)
        );
        Page<UserIndex> userPage = new PageImpl<>(userList);

        when(userService.findUsers(eq(firstName), eq(lastName), eq(email), any(PageRequest.class)))
                .thenReturn(userPage);

        // When
        ResponseEntity<ApiResponse<Page<UserIndex>>> response = userController.getUsers(
                page, size, sortField, sortDirection, firstName, lastName, email);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<Page<UserIndex>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());

        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(userService).findUsers(eq(firstName), eq(lastName), eq(email), pageRequestCaptor.capture());
        PageRequest capturedPageRequest = pageRequestCaptor.getValue();
        assertEquals(Sort.by(sortField).descending(), capturedPageRequest.getSort());
    }

    @Test
    @DisplayName("GET /users/{id} - Retourne un utilisateur lorsqu'il existe")
    void getUserById_ShouldReturnUser() {
        // Given
        Long userId = 1L;
        Address address = createTestAddress();
        UserShow userShow = new UserShow(
                userId, "John", "Doe", "john.doe@example.com", address,
                new ArrayList<>(), UserRole.USER
        );
        when(userService.findById(eq(userId))).thenReturn(userShow);

        // When
        ResponseEntity<ApiResponse<UserShow>> response = userController.getUserById(userId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<UserShow> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("User found", apiResponse.message());
        assertEquals(userShow, apiResponse.data());
        assertEquals(UserRole.USER, apiResponse.data().role());
        verify(userService).findById(eq(userId));
    }

    @Test
    @DisplayName("POST /users - Crée et retourne un nouvel utilisateur")
    void createUser_ShouldReturnCreatedUser() {
        // Given
        Address address = createTestAddress();
        User user = new User("John", "Doe", "john.doe@example.com", "Password123", address, UserRole.USER);
        User createdUser = new User("John", "Doe", "john.doe@example.com", "Password123", address, UserRole.USER);
        createdUser.setId(1L);
        when(userService.createUser(any(User.class))).thenReturn(createdUser);

        // When
        ResponseEntity<ApiResponse<User>> response = userController.createUser(user);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<User> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("User created successfully", apiResponse.message());
        assertEquals(createdUser, apiResponse.data());
        assertEquals(UserRole.USER, apiResponse.data().getRole());
        verify(userService).createUser(eq(user));
    }

    @Test
    @DisplayName("PUT /users - Met à jour et retourne l'utilisateur modifié")
    void updateUser_ShouldReturnUpdatedUser() {
        // Given
        Address address = createTestAddress();
        User user = new User("John", "Doe", "john.doe@example.com", "Password123", address, UserRole.USER);
        user.setId(1L);
        User updatedUser = new User("John", "Smith", "john.smith@example.com", "Password123", address, UserRole.USER);
        updatedUser.setId(1L);
        when(userService.updateUser(any(User.class))).thenReturn(updatedUser);

        // When
        ResponseEntity<ApiResponse<User>> response = userController.updateUser(user);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<User> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("User updated successfully", apiResponse.message());
        assertEquals(updatedUser, apiResponse.data());
        assertEquals(UserRole.USER, apiResponse.data().getRole());
        verify(userService).updateUser(eq(user));
    }

    @Test
    @DisplayName("PATCH /users/{id} - Met à jour partiellement et retourne l'utilisateur modifié")
    void patchUser_ShouldReturnPatchedUser() {
        // Given
        Long userId = 1L;
        UserUpdate userUpdate = new UserUpdate(
                Optional.of("Updated"),
                Optional.of("User"),
                Optional.of("updated.user@example.com"),
                Optional.of(createTestAddress()),
                Optional.of("NewPassword123")
        );
        when(userService.patchUser(eq(userId), any(UserUpdate.class))).thenReturn(userUpdate);

        // When
        ResponseEntity<ApiResponse<UserUpdate>> response = userController.patchUser(userId, userUpdate);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<UserUpdate> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("User updated successfully", apiResponse.message());
        assertEquals(userUpdate, apiResponse.data());
        verify(userService).patchUser(eq(userId), eq(userUpdate));
    }

    @Test
    @DisplayName("DELETE /users/{id} - Supprime l'utilisateur et retourne un succès")
    void deleteUser_ShouldReturnSuccessResponse() {
        // Given
        Long userId = 1L;
        doNothing().when(userService).deleteUser(eq(userId));

        // When
        ResponseEntity<ApiResponse<Void>> response = userController.deleteUser(userId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<Void> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("User deleted successfully", apiResponse.message());
        assertNull(apiResponse.data());
        verify(userService).deleteUser(eq(userId));
    }

    @Test
    @DisplayName("POST /users - Crée et retourne un utilisateur avec rôle ADMIN")
    void createUserWithAdminRole_ShouldReturnCreatedUserWithAdminRole() {
        // Given
        Address address = createTestAddress();
        User adminUser = new User("Admin", "User", "admin@example.com", "AdminPass123", address, UserRole.ADMIN);
        User createdAdminUser = new User("Admin", "User", "admin@example.com", "AdminPass123", address, UserRole.ADMIN);
        createdAdminUser.setId(1L);
        when(userService.createUser(any(User.class))).thenReturn(createdAdminUser);

        // When
        ResponseEntity<ApiResponse<User>> response = userController.createUser(adminUser);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<User> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("User created successfully", apiResponse.message());
        assertEquals(createdAdminUser, apiResponse.data());
        assertEquals(UserRole.ADMIN, apiResponse.data().getRole());
        verify(userService).createUser(eq(adminUser));
    }
}
