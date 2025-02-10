package fr.uge.booqIn.controller;
import fr.uge.booqIn.service.BookCollectionService;
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
  private final BookCollectionService bookCollectionService;
  
  public UserController(UserService userService, BookCollectionService bookCollectionService) {
    this.userService = userService;
    this.bookCollectionService = bookCollectionService;
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

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
      logger.info("🗑️ Suppression de l'utilisateur avec id: " + id);
      userService.deleteUser(id);
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
      logger.info("🔄 Mise à jour de l'utilisateur avec id: " + user.getId());
      return userService.updateUser(user);
    }

    @PostMapping("/{id}/collections/{collectionId}")
    public void addCollectionToUser(@PathVariable Long id, @RequestBody Long collectionId) {
      logger.info("📩 Requête POST reçue : addCollectionToUser");
      Optional<User> user = userService.getUserById(id);
      if (user.isPresent()) {
      var collection = bookCollectionService.getCollectionById(collectionId);
          collection.ifPresent(bookCollection -> userService.addCollectionToUser(user.get(), bookCollection));
      }
    }
    @DeleteMapping("/{id}/collections/{collectionId}")
    public void removeCollectionFromUser(@PathVariable Long id, @PathVariable Long collectionId) {
      logger.info("🗑️ Suppression de la collection avec id: " + collectionId + " de l'utilisateur avec id: " + id);
      Optional<User> user = userService.getUserById(id);
      if (user.isPresent()) {
        var collection = bookCollectionService.getCollectionById(collectionId);
        if (collection.isPresent()) {
          userService.removeCollectionFromUser(user.get(), collection.get());
          userService.updateUser(user.get());
        }
      }
    }
}