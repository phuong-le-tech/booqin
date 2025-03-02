package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.dto.user.UserUpdate;
import fr.uge.booqIn.entity.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;

@Validated
public interface UserServiceInterface {

  /**
   * Find users based on search criteria
   *
   * @param firstName Optional first name filter
   * @param lastName  Optional last name filter
   * @param email     Optional email filter
   * @param pageable  Pagination information
   * @return Page of UserIndex DTOs
   */
  Page<UserIndex> findUsers(String firstName, String lastName, String email, Pageable pageable);

  /**
   * Find user by ID
   *
   * @param id User ID
   * @return UserShow DTO
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if user not found
   */
  UserShow findById(@NotNull Long id);

  /**
   * Create new user
   *
   * @param user User entity to create
   * @return Created User entity
   * @throws fr.uge.booqIn.exception.DuplicateResourceException if email already exists
   * @throws jakarta.validation.ValidationException             if validation fails
   */
  User createUser(@NotNull @Valid User user);

  /**
   * Update user (all fields)
   *
   * @param user User entity with updated values
   * @return Updated User entity
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException  if user not found
   * @throws fr.uge.booqIn.exception.DuplicateResourceException if email already exists
   * @throws jakarta.validation.ValidationException             if validation fails
   */
  User updateUser(@NotNull @Valid User user);

  /**
   * Partially update user with provided non-null fields
   *
   * @param id         User ID to update
   * @param userUpdate DTO containing fields to update
   * @return Updated User entity
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException  if user not found
   * @throws fr.uge.booqIn.exception.DuplicateResourceException if email already exists
   * @throws jakarta.validation.ValidationException             if validation fails
   */
  User patchUser(@NotNull Long id, @NotNull @Valid UserUpdate userUpdate);

  /**
   * Delete user by ID
   *
   * @param id User ID to delete
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if user not found
   */
  void deleteUser(@NotNull Long id);


  UserIndex findUserByIdToIndex(@NotNull Long id);
}