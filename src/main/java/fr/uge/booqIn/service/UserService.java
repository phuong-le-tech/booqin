package fr.uge.booqIn.service;

import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.dto.user.UserUpdate;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.specification.UserSpecification;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

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

    // Use the specification with the pageable object
    return userRepository.findAll(spec, pageable)
            .map(user -> new UserIndex(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail()
            ));
  }

  @Transactional
  public Optional<UserShow> findById(Long id) {
    return userRepository.findById(id)
            .map(user -> new UserShow(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail(),
                    user.getAddress()
            ));
  }

  @Transactional
  public Optional<User> findEntityById(Long id) {
    return userRepository.findById(id);
  }

  @Transactional
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }

  @Transactional
  public Boolean deleteUser(Long id) {
    userRepository.deleteById(id);
    return userRepository.existsById(id);
  }

  @Transactional
  public User updateUser(User user) {
    return userRepository.save(user);
  }

  @Transactional
  public User patchUser(Long id, UserUpdate user) {
    User existing = userRepository.findById(id).orElseThrow();

    if (user.firstName() != null) {
      existing.setFirstName(user.firstName());
    }

    if (user.lastName() != null) {
      existing.setLastName(user.lastName());
    }

    if (user.email() != null) {
      existing.setEmail(user.email());
    }

    if (user.address() != null) {
      existing.setAddress(user.address());
    }

    return userRepository.save(existing);

  }

  @Transactional
  public Boolean addCollectionToUser(User user, BookCollection collection) {
    if (user.getCollections() == null) {
      user.setCollections(new ArrayList<>());
    } else if (user.getCollections().contains(collection)) {
      return false;
    }
    user.addCollection(collection);
    return true;
  }

  @Transactional
  public void removeCollectionFromUser(User user, BookCollection collection) {
    user.removeCollection(collection);
  }
}
