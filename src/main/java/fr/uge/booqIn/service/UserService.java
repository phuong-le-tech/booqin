package fr.uge.booqIn.service;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.hibernate.Hibernate;
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
  public Optional<User> findByEmail(String email) {
    return userRepository.findByEmail(email);
  }
  @Transactional
  public Optional<User> getUserById(Long id) {
    Optional<User> user = userRepository.findById(id);
    user.ifPresent(u -> {
      Hibernate.initialize(u.getBooksToLend());
      Hibernate.initialize(u.getCollections());
    });
    return user;
  }

  

  
  public void deleteUser(Long id) {
    userRepository.deleteById(id);
  }
  
  public User saveUser(User user) {

    if (user.getBooksToLend() == null) {
      user.setBooksToLend(new ArrayList<>());
    }
    if (user.getCollections() == null) {
      user.setCollections(new ArrayList<>());
    }
    
    return userRepository.save(user);
  }

  public User updateUser(User user) {
    return userRepository.save(user);
  }
  
  public void addCollectionToUser(User user, BookCollection collection) {
    if (user.getCollections() == null) {
      user.setCollections(new ArrayList<>());
    } else if (user.getCollections().contains(collection)) {
      return;
    }
    user.addCollection(collection);
  }
  
  public void removeCollectionFromUser(User user, BookCollection collection) {
    user.removeCollection(collection);
  }
}