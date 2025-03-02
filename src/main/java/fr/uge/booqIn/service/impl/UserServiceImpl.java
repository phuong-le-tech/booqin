package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.dto.user.UserUpdate;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.exception.DuplicateResourceException;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.mapper.UserMapper;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import fr.uge.booqIn.specification.UserSpecification;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Service
public class UserServiceImpl implements UserServiceInterface {
  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final PasswordEncoder passwordEncoder;

  public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.userMapper = userMapper;
    this.passwordEncoder = passwordEncoder;
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

    user.setPassword(passwordEncoder.encode(user.getPassword()));

    return userRepository.save(user);
  }

  @Override
  @Transactional
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
  public User patchUser(@NotNull Long id, @NotNull @Valid UserUpdate userUpdate) {
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

    return userRepository.save(user);
  }


  @Override
  @Transactional
  public void deleteUser(@NotNull Long id) {
    if (!userRepository.existsById(id)) {
      throw new ResourceNotFoundException("User", "id", id);
    }
    userRepository.deleteById(id);
  }

  public User findUserById(Long id) {
    return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
  }

  @Transactional
  public UserIndex findUserByIdtoIndex(Long userId) {
    User user = userRepository.findUserWithBooks(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    User userWithCollections = userRepository.findUserWithCollections(userId)
            .orElse(user);
    
    return new UserIndex(userWithCollections.getId(), userWithCollections.getFirstName(),
            userWithCollections.getLastName(), userWithCollections.getEmail());
  }

  
}