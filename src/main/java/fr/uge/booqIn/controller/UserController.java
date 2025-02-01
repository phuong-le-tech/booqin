package fr.uge.booqIn.controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private static final Logger logger = LoggerFactory.getLogger(UserController.class);
  private final UserService userService;
  
  public UserController(UserService userService) {
    this.userService = userService;
    logger.info("✅ UserController chargé !");
  }
  
  @PostMapping("/register")
  public User registerUser(@RequestBody User user) {
    logger.info("📩 Requête POST reçue : registerUser");
    return userService.saveUser(user);
  }
  
  @GetMapping("/{id}")
  public Optional<User> getUserByEmail(@PathVariable Long id) {
    logger.info("🔍 Recherche utilisateur avec email: " + id);
    return userService.getUserById(id);
  }
}