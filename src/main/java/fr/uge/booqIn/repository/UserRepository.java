package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

  /**
   * Finds a user by their ID with eager loading of book collections.
   *
   * @param id The unique identifier of the user
   * @return An optional containing the user with their collections loaded
   */
  @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.collections WHERE u.id = :id")
  Optional<User> findByIdWithCollections(@NonNull Long id);

  /**
   * Finds a user by their email address.
   *
   * @param email The email address of the user
   * @return An optional containing the user if found
   */
  Optional<User> findByEmail(@NonNull String email);

  /**
   * Finds a user by their cart ID.
   *
   * @param cartId The unique identifier of the user's cart
   * @return An optional containing the user if found
   */
  Optional<User> findByCartId(@NonNull Long cartId);

  /**
   * Finds a user by their role.
   *
   * @param role The role of the user
   * @return An optional containing the user if found
   */
  Optional<User> findByRole(@NonNull UserRole role);
}