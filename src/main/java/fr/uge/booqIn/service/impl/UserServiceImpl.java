package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.dto.user.UserUpdate;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.exception.DuplicateResourceException;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.mapper.UserMapper;
import fr.uge.booqIn.repository.CartRepository;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import fr.uge.booqIn.specification.UserSpecification;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ConcurrentModificationException;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserServiceInterface {
  private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final CartRepository cartRepository;

  public UserServiceImpl(
          @NonNull UserRepository userRepository,
          @NonNull UserMapper userMapper,
          @NonNull PasswordEncoder passwordEncoder,
          @NonNull CartRepository cartRepository) {
    this.userRepository = userRepository;
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
    this.cartRepository = cartRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<UserIndex> findUsers(String firstName, String lastName, String email, @NonNull Pageable pageable) {
    logger.debug("Finding users with filters: firstName={}, lastName={}, email={}", firstName, lastName, email);

    // Build the specification for filtering
    Specification<User> spec = Specification.where(null);

    if (firstName != null && !firstName.isEmpty()) {
      spec = spec.and(UserSpecification.hasFirstName(firstName));
    }

    if (lastName != null && !lastName.isEmpty()) {
      spec = spec.and(UserSpecification.hasLastName(lastName));
    }

    if (email != null && !email.isEmpty()) {
      spec = spec.and(UserSpecification.hasEmail(email));
    }

    // Use the specification with the pageable object and map results
    return userRepository.findAll(spec, pageable)
            .map(userMapper::toUserIndex);
  }

  @Override
  @Transactional(readOnly = true)
  public UserShow findById(@NonNull Long id) {
    logger.debug("Finding user by ID: {}", id);
    User user = findUserById(id);
    return userMapper.toUserShow(user);
  }

  @Override
  @Transactional(readOnly = true)
  public User findByUsername(@NonNull String username) {
    logger.debug("Finding user by username: {}", username);
    return userRepository.findByEmail(username)
            .orElseThrow(() -> {
              logger.warn("User not found with email: {}", username);
              return new ResourceNotFoundException("User", "email", username);
            });
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public User createUser(@NonNull @Valid User user) {
    logger.debug("Creating new user with email: {}", user.getEmail());

    if (userRepository.findByEmail(user.getEmail()).isPresent()) {
      logger.warn("Cannot create user: email already exists: {}", user.getEmail());
      throw new DuplicateResourceException("User", "email", user.getEmail());
    }

    Cart cart = new Cart();
    cartRepository.save(cart);
    user.setCart(cart);

    // Encode password
    user.setPassword(passwordEncoder.encode(user.getPassword()));

    User savedUser = userRepository.save(user);
    return savedUser;
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public User updateUser(@NonNull @Valid User user) {
    logger.debug("Updating user with ID: {}", user.getId());

    // Verify user exists
    if (!userRepository.existsById(user.getId())) {
      logger.warn("Cannot update user: user not found with ID: {}", user.getId());
      throw new ResourceNotFoundException("User", "id", user.getId());
    }

    // Check if email is unique (if changed)
    Optional<User> existingUserWithEmail = userRepository.findByEmail(user.getEmail());
    if (existingUserWithEmail.isPresent() && !existingUserWithEmail.get().getId().equals(user.getId())) {
      logger.warn("Cannot update user: email already exists: {}", user.getEmail());
      throw new DuplicateResourceException("User", "email", user.getEmail());
    }

    // Check if password needs encoding
    if (user.getPassword() != null && !user.getPassword().isEmpty() &&
            (existingUserWithEmail.isEmpty() ||
                    !passwordEncoder.matches(user.getPassword(), existingUserWithEmail.get().getPassword()))) {
      user.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    User updatedUser = userRepository.save(user);
    return updatedUser;
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public UserUpdate patchUser(@NonNull Long id, @NonNull @Valid UserUpdate userUpdate) {
    logger.debug("Partially updating user with ID: {}", id);

    User user = findUserById(id);

    // Update email if provided and changed
    userUpdate.email().ifPresent(newEmail -> {
      if (!newEmail.equals(user.getEmail())) {
        if (userRepository.findByEmail(newEmail).isPresent()) {
          logger.warn("Cannot update user: email already exists: {}", newEmail);
          throw new DuplicateResourceException("User", "email", newEmail);
        }
        user.setEmail(newEmail);
        logger.debug("Updated email for user ID {}: {}", id, newEmail);
      }
    });

    // Update other fields if provided
    userUpdate.firstName().ifPresent(firstName -> {
      user.setFirstName(firstName);
      logger.debug("Updated first name for user ID {}: {}", id, firstName);
    });

    userUpdate.lastName().ifPresent(lastName -> {
      user.setLastName(lastName);
      logger.debug("Updated last name for user ID {}: {}", id, lastName);
    });

    userUpdate.address().ifPresent(address -> {
      user.setAddress(address);
      logger.debug("Updated address for user ID {}", id);
    });

    // Update password if provided and not empty
    userUpdate.password().ifPresent(newPassword -> {
      if (!newPassword.isEmpty()) {
        user.setPassword(passwordEncoder.encode(newPassword));
        logger.debug("Updated password for user ID {}", id);
      }
    });

    userRepository.save(user);
    return userUpdate;
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public void deleteUser(@NonNull Long id) {
    logger.debug("Deleting user with ID: {}", id);

    if (!userRepository.existsById(id)) {
      logger.warn("Cannot delete user: user not found with ID: {}", id);
      throw new ResourceNotFoundException("User", "id", id);
    }

    userRepository.deleteById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public UserIndex findUserByIdToIndex(@NonNull Long id) {
    logger.debug("Finding user by ID for index: {}", id);

    User userWithCollections = userRepository.findByIdWithCollections(id)
            .orElseThrow(() -> {
              logger.warn("User not found with ID: {}", id);
              return new ResourceNotFoundException("User", "id", id);
            });

    return new UserIndex(
            userWithCollections.getId(),
            userWithCollections.getFirstName(),
            userWithCollections.getLastName(),
            userWithCollections.getEmail(),
            userWithCollections.getAddress(),
            userWithCollections.getRole()
    );
  }

  @Override
  @Transactional(readOnly = true)
  public User findUserById(@NonNull Long id) {
    logger.debug("Finding user by ID with collections: {}", id);

    return userRepository.findByIdWithCollections(id)
            .orElseThrow(() -> {
              logger.warn("User not found with ID: {}", id);
              return new ResourceNotFoundException("User", "id", id);
            });
  }

  @Recover
  public UserUpdate recoverFromOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex, Long id, UserUpdate userUpdate) {
    logger.error("Optimistic locking failure while updating user {}: {}", id, ex.getMessage());
    throw new ConcurrentModificationException("The user has been modified by someone else. Please try again.", ex);
  }

  @Recover
  public User recoverFromOptimisticLockingFailureOnCreate(ObjectOptimisticLockingFailureException ex, User user) {
    logger.error("Optimistic locking failure while creating user: {}", ex.getMessage());
    throw new ConcurrentModificationException("The user could not be created due to concurrent modifications. Please try again.", ex);
  }

  @Recover
  public User recoverFromOptimisticLockingFailureOnUpdate(ObjectOptimisticLockingFailureException ex, User user) {
    logger.error("Optimistic locking failure while updating user {}: {}", user.getId(), ex.getMessage());
    throw new ConcurrentModificationException("The user has been modified by someone else. Please try again.", ex);
  }

  @Recover
  public void recoverFromOptimisticLockingFailureOnDelete(ObjectOptimisticLockingFailureException ex, Long id) {
    logger.error("Optimistic locking failure while deleting user {}: {}", id, ex.getMessage());
    throw new ConcurrentModificationException("The user has been modified by someone else. Please try again.", ex);
  }
}