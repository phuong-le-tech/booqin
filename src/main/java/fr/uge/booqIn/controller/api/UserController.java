package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.BookCollectionService;
import fr.uge.booqIn.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  private final UserService userService;
  private final BookCollectionService bookCollectionService;

  public UserController(UserService userService, BookCollectionService bookCollectionService) {
    this.userService = userService;
    this.bookCollectionService = bookCollectionService;
    logger.info("✅ UserController chargé !");
  }

  @GetMapping()
  public ResponseEntity<ApiResponse<Page<UserIndex>>> getUsers(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size,
          @RequestParam(defaultValue = "id,asc") String[] sort,
          @RequestParam(defaultValue = "asc") String direction,
          @RequestParam(required = false) String firstName,
          @RequestParam(required = false) String lastName,
          @RequestParam(required = false) String email ){

    logger.info("🔍 Recherche paginée des utilisateurs (page={}, size={})", page, size);

    List<Sort.Order> orders = new ArrayList<>();
    System.out.println(Arrays.toString(sort));
    for (String sortOrder : sort) {
      String[] _sort = sortOrder.split(",");
      orders.add(new Sort.Order(_sort[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, _sort[0]));
    }
    Page<UserIndex> users = userService.findUsers(
            firstName,
            lastName,
            email,
            PageRequest.of(page, size, Sort.by(orders))
    );

    return ResponseEntity.ok(ApiResponse.success("Users found", users));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<UserShow>> getUserById(@PathVariable Long id) {
    logger.info("🔍 Recherche utilisateur avec id: {}", id);

    Optional<UserShow> userDtoOpt = userService.findById(id);
    return userDtoOpt.map(userShow ->
                    ResponseEntity.ok(ApiResponse.success("User found", userShow)))
            .orElseGet(() -> ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("User not found")));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
    logger.info("🗑️ Suppression de l'utilisateur avec id: {}", id);

    Optional<User> userOpt = userService.findEntityById(id);
    if (userOpt.isEmpty()) {
      return ResponseEntity
              .status(HttpStatus.NOT_FOUND)
              .body(ApiResponse.error("User not found"));
    }

    var isDelete = userService.deleteUser(id);
    if (!isDelete) {
      return ResponseEntity
              .status(HttpStatus.BAD_REQUEST)
              .body(ApiResponse.error("User not deleted"));
    }
    return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
  }

  @PutMapping
  public ResponseEntity<ApiResponse<UserShow>> updateUser(@RequestBody User user) {
    logger.info("🔄 Mise à jour de l'utilisateur avec id: {}", user.getId());

    Optional<User> existingOpt = userService.findEntityById(user.getId());
    if (existingOpt.isEmpty()) {
      return ResponseEntity
              .status(HttpStatus.NOT_FOUND)
              .body(ApiResponse.error("User not found"));
    }

    User updatedUser = userService.updateUser(user);
    var updatedUserShow = new UserShow(
            updatedUser.getId(),
            updatedUser.getFirstName(),
            updatedUser.getLastName(),
            updatedUser.getEmail(),
            updatedUser.getAddress()
    );

    return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUserShow));
  }

  @PostMapping("/{id}/collections/{collectionId}")
  public ResponseEntity<ApiResponse<Void>> addCollectionToUser(
          @PathVariable Long id,
          @PathVariable Long collectionId
  ) {
    logger.info("📩 addCollectionToUser (user={}, collection={})", id, collectionId);

    Optional<User> userOpt = userService.findEntityById(id);
    if (userOpt.isEmpty()) {
      return ResponseEntity
              .status(HttpStatus.NOT_FOUND)
              .body(ApiResponse.error("User not found"));
    }

    Optional<BookCollection> collectionOpt = bookCollectionService.findCollectionById(collectionId);
    if (collectionOpt.isEmpty()) {
      return ResponseEntity
              .status(HttpStatus.NOT_FOUND)
              .body(ApiResponse.error("Collection not found"));
    }

    var isAdd = userService.addCollectionToUser(userOpt.get(), collectionOpt.get());
    if (!isAdd) {
      return ResponseEntity
              .status(HttpStatus.BAD_REQUEST)
              .body(ApiResponse.error("Collection not added to user"));
    }

    return ResponseEntity.ok(ApiResponse.success("Collection added to user successfully", null));
  }

  @DeleteMapping("/{id}/collections/{collectionId}")
  public ResponseEntity<ApiResponse<Void>> removeCollectionFromUser(
          @PathVariable Long id,
          @PathVariable Long collectionId
  ) {
    logger.info("🗑️ removeCollectionFromUser (user={}, collection={})", id, collectionId);

    Optional<User> userOpt = userService.findEntityById(id);
    if (userOpt.isEmpty()) {
      return ResponseEntity
              .status(HttpStatus.NOT_FOUND)
              .body(ApiResponse.error("User not found"));
    }

    Optional<BookCollection> collectionOpt = bookCollectionService.findCollectionById(collectionId);
    if (collectionOpt.isEmpty()) {
      return ResponseEntity
              .status(HttpStatus.NOT_FOUND)
              .body(ApiResponse.error("Collection not found"));
    }

    userService.removeCollectionFromUser(userOpt.get(), collectionOpt.get());
    userService.updateUser(userOpt.get());

    return ResponseEntity.ok(ApiResponse.success("Collection removed from user successfully", null));
  }
}
