package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.dto.user.UserUpdate;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  private final UserServiceInterface userService;

  public UserController(UserServiceInterface userService) {
    this.userService = userService;
  }

  @GetMapping("")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Page<UserIndex>>> getUsers(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "10") int size,
          @RequestParam(defaultValue = "id") String sortField,
          @RequestParam(defaultValue = "asc") String sortDirection,
          @RequestParam(required = false) String firstName,
          @RequestParam(required = false) String lastName,
          @RequestParam(required = false) String email) {
    Sort sort = sortDirection.equalsIgnoreCase("desc") ?
            Sort.by(sortField).descending() :
            Sort.by(sortField).ascending();

    Pageable pageable = PageRequest.of(page, size, sort);

    Page<UserIndex> users = userService.findUsers(firstName, lastName, email, pageable);

    return ResponseEntity.ok(ApiResponse.success("Users found", users));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<UserShow>> getUserById(@PathVariable Long id) {
    UserShow userShow = userService.findById(id);
    return ResponseEntity.ok(ApiResponse.success("User found", userShow));
  }

  @PostMapping("")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<User>> createUser(@RequestBody User user) {
    User createdUser = userService.createUser(user);
    return ResponseEntity.ok(ApiResponse.success("User created successfully", createdUser));
  }

  @PutMapping("")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<User>> updateUser(@RequestBody User user) {
    User updatedUser = userService.updateUser(user);
    return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUser));
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #id" )
  public ResponseEntity<ApiResponse<UserUpdate>> patchUser(@PathVariable Long id, @RequestBody UserUpdate user) {
    UserUpdate updatedUser = userService.patchUser(id, user);
    return ResponseEntity.ok(ApiResponse.success("User updated successfully", updatedUser));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
    userService.deleteUser(id);
    return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
  }
}
