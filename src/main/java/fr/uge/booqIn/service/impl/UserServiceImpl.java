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
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ConcurrentModificationException;
import java.util.Optional;


@Service
public class UserServiceImpl implements UserServiceInterface {
  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;
  private final CartRepository cartRepository;

  public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder,
  CartRepository cartRepository) {
    this.userRepository = userRepository;
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
    this.cartRepository = cartRepository;
  }

  @Override
  @Transactional
  public Page<UserIndex> findUsers(String firstName, String lastName, String email, Pageable pageable) {
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
  @Transactional
  public UserShow findById(@NotNull Long id) {
    User user = findUserById(id);
    return userMapper.toUserShow(user);
  }

  @Override
  @Transactional
  public User createUser(@NotNull @Valid User user) {
    if (userRepository.findByEmail(user.getEmail()).isPresent()) {
      throw new DuplicateResourceException("User", "email", user.getEmail());
    }

    Cart cart = new Cart();
    cartRepository.save(cart);

    user.setPassword(passwordEncoder.encode(user.getPassword()));

    return userRepository.save(user);
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public User updateUser(@NotNull @Valid User user) {
      Optional<User> existingUserWithEmail = userRepository.findByEmail(user.getEmail());
      if (existingUserWithEmail.isPresent() && !existingUserWithEmail.get().getId().equals(user.getId())) {
        throw new DuplicateResourceException("User", "email", user.getEmail());
      }

      if (user.getPassword() != null && !user.getPassword().isEmpty() &&
              !passwordEncoder.matches(user.getPassword(), existingUserWithEmail.get().getPassword())) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
      }
      return userRepository.save(user);
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public UserUpdate patchUser(@NotNull Long id, @NotNull @Valid UserUpdate userUpdate) {
      User user = findUserById(id);

      userUpdate.email().ifPresent(newEmail -> {
        if (!newEmail.equals(user.getEmail())) {
          if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new DuplicateResourceException("User", "email", newEmail);
          }
          user.setEmail(newEmail);
        }
      });

      userUpdate.firstName().ifPresent(user::setFirstName);
      userUpdate.lastName().ifPresent(user::setLastName);
      userUpdate.address().ifPresent(user::setAddress);

      userUpdate.password().ifPresent(newPassword -> {
        if (!newPassword.isEmpty()) {
          user.setPassword(passwordEncoder.encode(newPassword));
        }
      });
      userRepository.save(user);
      return userUpdate;
  }


  @Override
  @Transactional
  public void deleteUser(@NotNull Long id) {
    if (!userRepository.existsById(id)) {
      throw new ResourceNotFoundException("User", "id", id);
    }
    userRepository.deleteById(id);
  }

  @Override
  @Transactional
  public UserIndex findUserByIdToIndex(Long userId) {
    User userWithCollections = userRepository.findByIdWithCollections(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    return new UserIndex(userWithCollections.getId(), userWithCollections.getFirstName(),
            userWithCollections.getLastName(), userWithCollections.getEmail(),
            userWithCollections.getAddress(), userWithCollections.getRole());
  }
  @Override
  public User findUserById(Long id) {
    return userRepository.findByIdWithCollections(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
  }

  @Recover
  public UserUpdate recoverFromOptimisticLockingFailure(ObjectOptimisticLockingFailureException ex, Long id, UserUpdate userUpdate) {
    throw new ConcurrentModificationException("The user has been modified by someone else. Please try again.", ex);
  }
}