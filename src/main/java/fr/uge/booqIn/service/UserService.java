package fr.uge.booqIn.service;

import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class UserService {
  private final UserRepository userRepository;

  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
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
