package fr.uge.booqIn.service;

import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.repository.UserRepository;
import jakarta.transaction.Transactional;
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
  public List<UserIndex> findAll() {
    return userRepository.findAll().stream()
            .map(user -> new UserIndex(
                    user.getId(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getEmail()
            ))
            .toList();
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
  public User patchUser(User user) {
    Optional<User> existingOpt = userRepository.findById(user.getId());
    if (existingOpt.isEmpty()) {
      return null;
    }
    User existing = existingOpt.get();
    if (user.getFirstName() != null) {
      existing.setFirstName(user.getFirstName());
    }
    if (user.getLastName() != null) {
      existing.setLastName(user.getLastName());
    }
    if (user.getEmail() != null) {
      existing.setEmail(user.getEmail());
    }
    if (user.getAddress() != null) {
      existing.setAddress(user.getAddress());
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
