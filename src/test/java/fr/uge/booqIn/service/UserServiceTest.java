package fr.uge.booqIn.service;

import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.dto.user.UserUpdate;
import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.exception.DuplicateResourceException;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.factory.fixture.AddressFactory;
import fr.uge.booqIn.mapper.UserMapper;
import fr.uge.booqIn.repository.CartRepository;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.impl.UserServiceImpl;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("UserService Tests")
class UserServiceTest {

  private UserRepository userRepository;
  private UserMapper userMapper;
  private PasswordEncoder passwordEncoder;
  private CartRepository cartRepository;
  private UserServiceInterface userService;
  private User testUser;
  private Cart testCart;
  private Address testAddress;

  @BeforeEach
  void setUp() {
    userRepository = Mockito.mock(UserRepository.class);
    userMapper = Mockito.mock(UserMapper.class);
    passwordEncoder = Mockito.mock(PasswordEncoder.class);
    cartRepository = Mockito.mock(CartRepository.class);

    userService = new UserServiceImpl(userRepository, userMapper, passwordEncoder, cartRepository);

    testAddress = AddressFactory.createAddress();

    testCart = new Cart();
    testCart.setId(1L);

    testUser = new User();
    testUser.setId(1L);
    testUser.setFirstName("John");
    testUser.setLastName("Doe");
    testUser.setEmail("john.doe@example.com");
    testUser.setPassword("encodedPassword");
    testUser.setAddress(testAddress);
    testUser.setRole(UserRole.USER);
    testUser.setCart(testCart);
    testUser.setCreatedAt(new Date());
    testUser.setUpdatedAt(new Date());
  }

  @Test
  @DisplayName("findUsers - Should return page of users when filters are applied")
  void findUsers_WithFilters_ShouldReturnFilteredUsers() {
    // Given
    String firstName = "John";
    String lastName = "Doe";
    String email = "john.doe@example.com";
    Pageable pageable = Pageable.ofSize(10);

    List<User> userList = Collections.singletonList(testUser);
    Page<User> userPage = new PageImpl<>(userList);

    UserIndex userIndex = new UserIndex(
            testUser.getId(),
            testUser.getFirstName(),
            testUser.getLastName(),
            testUser.getEmail(),
            testUser.getAddress(),
            testUser.getRole()
    );

    when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
    when(userMapper.toUserIndex(any(User.class))).thenReturn(userIndex);

    // When
    Page<UserIndex> result = userService.findUsers(firstName, lastName, email, pageable);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(userIndex, result.getContent().get(0));

    verify(userRepository).findAll(any(Specification.class), eq(pageable));
    verify(userMapper).toUserIndex(eq(testUser));
  }

  @Test
  @DisplayName("findById - Should return user when it exists")
  void findById_WithExistingId_ShouldReturnUser() {
    // Given
    Long userId = testUser.getId();
    UserShow userShow = new UserShow(
            userId,
            testUser.getFirstName(),
            testUser.getLastName(),
            testUser.getEmail(),
            testUser.getAddress(),
            Collections.emptyList(),
            testUser.getRole()
    );

    when(userRepository.findByIdWithCollections(eq(userId))).thenReturn(Optional.of(testUser));
    when(userMapper.toUserShow(any(User.class))).thenReturn(userShow);

    // When
    UserShow result = userService.findById(userId);

    // Then
    assertNotNull(result);
    assertEquals(userId, result.id());
    assertEquals(testUser.getFirstName(), result.firstName());
    assertEquals(testUser.getEmail(), result.email());

    verify(userRepository).findByIdWithCollections(eq(userId));
    verify(userMapper).toUserShow(eq(testUser));
  }

  @Test
  @DisplayName("findById - Should throw ResourceNotFoundException when user doesn't exist")
  void findById_WithNonExistingId_ShouldThrowException() {
    // Given
    Long userId = 999L;

    when(userRepository.findByIdWithCollections(eq(userId))).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class, () ->
            userService.findById(userId)
    );

    verify(userRepository).findByIdWithCollections(eq(userId));
    verifyNoInteractions(userMapper);
  }

  @Test
  @DisplayName("createUser - Should throw DuplicateResourceException when email already exists")
  void createUser_WithExistingEmail_ShouldThrowException() {
    // Given
    User newUser = new User();
    newUser.setFirstName("Jane");
    newUser.setLastName("Smith");
    newUser.setEmail("john.doe@example.com"); // Use existing email
    newUser.setPassword("Password123");
    newUser.setAddress(testAddress);
    newUser.setRole(UserRole.USER);

    when(userRepository.findByEmail(eq(newUser.getEmail()))).thenReturn(Optional.of(testUser));

    // When & Then
    assertThrows(DuplicateResourceException.class, () ->
            userService.createUser(newUser)
    );

    verify(userRepository).findByEmail(eq(newUser.getEmail()));
    verifyNoInteractions(cartRepository);
    verifyNoInteractions(passwordEncoder);
  }

  @Test
  @DisplayName("updateUser - Should throw DuplicateResourceException when email already used by another user")
  void updateUser_WithEmailUsedByAnotherUser_ShouldThrowException() {
    // Given
    User anotherUser = new User();
    anotherUser.setId(2L);
    anotherUser.setEmail("another@example.com");

    User userToUpdate = new User();
    userToUpdate.setId(testUser.getId());
    userToUpdate.setEmail("another@example.com"); // Already used by another user

    when(userRepository.findByEmail(eq(userToUpdate.getEmail()))).thenReturn(Optional.of(anotherUser));

    // When & Then
    assertThrows(DuplicateResourceException.class, () ->
            userService.updateUser(userToUpdate)
    );

    verify(userRepository).findByEmail(eq(userToUpdate.getEmail()));
    verifyNoMoreInteractions(userRepository);
  }

  @Test
  @DisplayName("patchUser - Should partially update and return updated user")
  void patchUser_WithValidData_ShouldPatchAndReturnUser() {
    // Given
    Long userId = testUser.getId();
    String newFirstName = "Updated";
    String newPassword = "NewPassword123";

    UserUpdate userUpdate = new UserUpdate(
            Optional.of(newFirstName),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.of(newPassword)
    );

    when(userRepository.findByIdWithCollections(eq(userId))).thenReturn(Optional.of(testUser));
    when(passwordEncoder.encode(eq(newPassword))).thenReturn("newEncodedPassword");
    when(userRepository.save(any(User.class))).thenReturn(testUser);

    // When
    UserUpdate result = userService.patchUser(userId, userUpdate);

    // Then
    assertNotNull(result);
    assertEquals(userUpdate, result);

    verify(userRepository).findByIdWithCollections(eq(userId));
    verify(passwordEncoder).encode(eq(newPassword));
    verify(userRepository).save(eq(testUser));

    assertEquals(newFirstName, testUser.getFirstName());
    assertEquals("newEncodedPassword", testUser.getPassword());
  }

  @Test
  @DisplayName("patchUser - Should throw ResourceNotFoundException when user doesn't exist")
  void patchUser_WithNonExistingId_ShouldThrowException() {
    // Given
    Long userId = 999L;
    UserUpdate userUpdate = new UserUpdate(
            Optional.of("Updated"),
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
    );

    when(userRepository.findByIdWithCollections(eq(userId))).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class, () ->
            userService.patchUser(userId, userUpdate)
    );

    verify(userRepository).findByIdWithCollections(eq(userId));
    verifyNoMoreInteractions(userRepository);
  }

  @Test
  @DisplayName("patchUser - Should throw DuplicateResourceException when email already used by another user")
  void patchUser_WithEmailUsedByAnotherUser_ShouldThrowException() {
    // Given
    Long userId = testUser.getId();
    String newEmail = "another@example.com";

    User anotherUser = new User();
    anotherUser.setId(2L);
    anotherUser.setEmail(newEmail);

    UserUpdate userUpdate = new UserUpdate(
            Optional.empty(),
            Optional.empty(),
            Optional.of(newEmail),
            Optional.empty(),
            Optional.empty()
    );

    when(userRepository.findByIdWithCollections(eq(userId))).thenReturn(Optional.of(testUser));
    when(userRepository.findByEmail(eq(newEmail))).thenReturn(Optional.of(anotherUser));

    // When & Then
    assertThrows(DuplicateResourceException.class, () ->
            userService.patchUser(userId, userUpdate)
    );

    verify(userRepository).findByIdWithCollections(eq(userId));
    verify(userRepository).findByEmail(eq(newEmail));
    verifyNoMoreInteractions(userRepository);
  }

  @Test
  @DisplayName("deleteUser - Should delete existing user")
  void deleteUser_WithValidId_ShouldDeleteUser() {
    // Given
    Long userId = testUser.getId();

    when(userRepository.existsById(eq(userId))).thenReturn(true);
    doNothing().when(userRepository).deleteById(eq(userId));

    // When
    userService.deleteUser(userId);

    // Then
    verify(userRepository).existsById(eq(userId));
    verify(userRepository).deleteById(eq(userId));
  }

  @Test
  @DisplayName("deleteUser - Should throw ResourceNotFoundException when user doesn't exist")
  void deleteUser_WithNonExistingId_ShouldThrowException() {
    // Given
    Long userId = 999L;

    when(userRepository.existsById(eq(userId))).thenReturn(false);

    // When & Then
    assertThrows(ResourceNotFoundException.class, () ->
            userService.deleteUser(userId)
    );

    verify(userRepository).existsById(eq(userId));
    verify(userRepository, never()).deleteById(anyLong());
  }

  @Test
  @DisplayName("findUserByIdToIndex - Should return user index when user exists")
  void findUserByIdToIndex_WithExistingId_ShouldReturnUserIndex() {
    // Given
    Long userId = testUser.getId();

    when(userRepository.findByIdWithCollections(eq(userId))).thenReturn(Optional.of(testUser));

    // When
    UserIndex result = userService.findUserByIdToIndex(userId);

    // Then
    assertNotNull(result);
    assertEquals(userId, result.id());
    assertEquals(testUser.getFirstName(), result.firstName());
    assertEquals(testUser.getLastName(), result.lastName());
    assertEquals(testUser.getEmail(), result.email());
    assertEquals(testUser.getAddress(), result.address());
    assertEquals(testUser.getRole(), result.role());

    verify(userRepository).findByIdWithCollections(eq(userId));
  }

  @Test
  @DisplayName("findUserByIdToIndex - Should throw ResourceNotFoundException when user doesn't exist")
  void findUserByIdToIndex_WithNonExistingId_ShouldThrowException() {
    // Given
    Long userId = 999L;

    when(userRepository.findByIdWithCollections(eq(userId))).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class, () ->
            userService.findUserByIdToIndex(userId)
    );

    verify(userRepository).findByIdWithCollections(eq(userId));
  }

  @Test
  @DisplayName("findUserById - Should return user when it exists")
  void findUserById_WithExistingId_ShouldReturnUser() {
    // Given
    Long userId = testUser.getId();

    when(userRepository.findByIdWithCollections(eq(userId))).thenReturn(Optional.of(testUser));

    // When
    User result = userService.findUserById(userId);

    // Then
    assertNotNull(result);
    assertEquals(testUser, result);

    verify(userRepository).findByIdWithCollections(eq(userId));
  }

  @Test
  @DisplayName("findUserById - Should throw ResourceNotFoundException when user doesn't exist")
  void findUserById_WithNonExistingId_ShouldThrowException() {
    // Given
    Long userId = 999L;

    when(userRepository.findByIdWithCollections(eq(userId))).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class, () ->
            userService.findUserById(userId)
    );

    verify(userRepository).findByIdWithCollections(eq(userId));
  }
}