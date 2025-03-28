package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.dto.user.UserUpdate;
import fr.uge.booqIn.entity.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

/**
 * Service interface for managing users in the BooqIn application.
 * Provides methods to create, retrieve, update, and delete users.
 */
@Validated
public interface UserServiceInterface {

  /**
   * Find users based on search criteria
   *
   * @param firstName Optional first name filter
   * @param lastName  Optional last name filter
   * @param email     Optional email filter
   * @param pageable  Pagination information (required)
   * @return Page of UserIndex DTOs
   * @throws IllegalArgumentException if pageable is null
   */
  Page<UserIndex> findUsers(String firstName, String lastName, String email, @NonNull Pageable pageable);

  /**
   * Find user by ID
   *
   * @param id User ID (required)
   * @return UserShow DTO
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if user not found
   * @throws IllegalArgumentException if id is null
   */
  UserShow findById(@NonNull Long id);

  /**
   * Find user by username (email)
   *
   * @param username The user's email address (required)
   * @return User entity
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if user not found
   * @throws IllegalArgumentException if username is null
   */
  User findByUsername(@NonNull String username);

  /**
   * Create new user
   *
   * @param user User entity to create (required)
   * @return Created User entity
   * @throws fr.uge.booqIn.exception.DuplicateResourceException if email already exists
   * @throws jakarta.validation.ValidationException if validation fails
   * @throws IllegalArgumentException if user is null
   */
  User createUser(@NonNull @Valid User user);

  /**
   * Update user (all fields)
   *
   * @param user User entity with updated values (required)
   * @return Updated User entity
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if user not found
   * @throws fr.uge.booqIn.exception.DuplicateResourceException if email already exists
   * @throws jakarta.validation.ValidationException if validation fails
   * @throws IllegalArgumentException if user is null
   */
  User updateUser(@NonNull @Valid User user);

  /**
   * Partially update user with provided non-null fields
   *
   * @param id User ID to update (required)
   * @param userUpdate DTO containing fields to update (required)
   * @return Updated UserUpdate DTO
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if user not found
   * @throws fr.uge.booqIn.exception.DuplicateResourceException if email already exists
   * @throws jakarta.validation.ValidationException if validation fails
   * @throws IllegalArgumentException if id or userUpdate is null
   */
  UserUpdate patchUser(@NonNull Long id, @NonNull @Valid UserUpdate userUpdate);

  /**
   * Delete user by ID
   *
   * @param id User ID to delete (required)
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if user not found
   * @throws IllegalArgumentException if id is null
   */
  void deleteUser(@NonNull Long id);

  /**
   * Find user by ID and return as UserIndex DTO
   *
   * @param id User ID (required)
   * @return UserIndex DTO
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if user not found
   * @throws IllegalArgumentException if id is null
   */
  UserIndex findUserByIdToIndex(@NonNull Long id);

  /**
   * Find user by ID and return as User entity with collections loaded
   *
   * @param id User ID (required)
   * @return User entity with collections
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if user not found
   * @throws IllegalArgumentException if id is null
   */
  User findUserById(@NonNull Long id);
}