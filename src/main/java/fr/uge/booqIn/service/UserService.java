package fr.uge.booqIn.service;

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
  public Optional<User> findByEmail(String email) {
    return Optional.ofNullable(userRepository.findByPersonEmail(email));
  }
  @Transactional
  public Optional<User> getUserById(Long id) {
    return userRepository.findById(id);
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

}