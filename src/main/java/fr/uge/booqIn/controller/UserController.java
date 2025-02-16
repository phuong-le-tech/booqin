package fr.uge.booqIn.controller;

import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.service.BookCollectionService;
import fr.uge.booqIn.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private static final Logger logger = LoggerFactory.getLogger(UserController.class);
  private final UserService userService;
  private final BookCollectionService bookCollectionService;
  
  public UserController(UserService userService, BookCollectionService bookCollectionService) {
    this.userService = userService;
    this.bookCollectionService = bookCollectionService;
    logger.info("✅ UserController chargé !");
  }
  
  @PostMapping("/register")
  public ResponseEntity<?> registerUser(@RequestBody User user) {
    logger.info("📩 Requête POST reçue : registerUser");
    if (userService.findByEmail(user.getEmail()).isPresent()) {
      return ResponseEntity.badRequest().body("⚠️ Email déjà utilisé !");
    }
    User savedUser = userService.saveUser(user);
    return ResponseEntity.ok(savedUser);
  }
  
  @GetMapping("/{id}")
  public ResponseEntity<?> getUserById(@PathVariable Long id) {
    logger.info("🔍 Recherche utilisateur avec ID: " + id);
    Optional<User> user = userService.getUserById(id);
    return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }
  
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteUser(@PathVariable Long id) {
    logger.info("🗑️ Suppression de l'utilisateur avec id: " + id);
    if (userService.getUserById(id).isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    userService.deleteUser(id);
    return ResponseEntity.ok("🗑️ Utilisateur supprimé !");
  }
  
  @PutMapping
  public ResponseEntity<?> updateUser(@RequestBody User user) {
    logger.info("🔄 Mise à jour de l'utilisateur avec id: " + user.getId());
    if (userService.getUserById(user.getId()).isEmpty()) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(userService.updateUser(user));
  }
}
