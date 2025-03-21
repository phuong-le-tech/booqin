package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.dto.follow.FollowDto;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.entity.Follow;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.mapper.UserMapper;
import fr.uge.booqIn.model.Followable;
import fr.uge.booqIn.service.interfaces.BookCollectionServiceInterface;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import fr.uge.booqIn.service.interfaces.FollowServiceInterface;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/follows")
public class FollowController {

  private final FollowServiceInterface followService;
  private final UserServiceInterface userService;
  private final BookServiceInterface bookService;
  private final BookCollectionServiceInterface collectionService;
  private final UserMapper userMapper;

  public FollowController(
          FollowServiceInterface followService,
          UserServiceInterface userService,
          BookServiceInterface bookService,
          BookCollectionServiceInterface collectionService,
          UserMapper userMapper) {
    this.followService = followService;
    this.userService = userService;
    this.bookService = bookService;
    this.collectionService = collectionService;
    this.userMapper = userMapper;
  }

  /**
   * Follow an entity (user, book, collection)
   */
  @PostMapping
  public ResponseEntity<ApiResponse<FollowDto>> followEntity(
          @AuthenticationPrincipal User currentUser,
          @RequestBody Map<String, Object> request) {

    Long entityId = Long.valueOf(request.get("entityId").toString());
    String entityType = request.get("entityType").toString();

    Followable entityToFollow = null;

    // Get the entity to follow based on type
    switch (entityType) {
      case "USER":
        entityToFollow = userService.findUserById(entityId);
        break;
      case "BOOK":
        entityToFollow = bookService.findBookById(entityId);
        break;
      case "COLLECTION":
        entityToFollow = collectionService.findCollectionById(entityId);
        break;
      default:
        return ResponseEntity.badRequest().body(
                new ApiResponse<>(false, "Invalid entity type", null)
        );
    }

    if (entityToFollow == null) {
      return ResponseEntity.notFound().build();
    }

    Follow follow = followService.followEntity(currentUser.getId(), entityToFollow);

    FollowDto followDto = new FollowDto(
            follow.getId(),
            follow.getFollower().getId(),
            follow.getEntityId(),
            follow.getEntityType(),
            follow.getCreatedAt()
    );

    return ResponseEntity.ok(
            new ApiResponse<>(true, "Successfully followed entity", followDto)
    );
  }

  /**
   * Unfollow an entity
   */
  @DeleteMapping
  public ResponseEntity<ApiResponse<Boolean>> unfollowEntity(
          @AuthenticationPrincipal User currentUser,
          @RequestParam Long entityId,
          @RequestParam String entityType) {

    boolean success = followService.unfollowEntity(currentUser.getId(), entityId, entityType);

    if (success) {
      return ResponseEntity.ok(
              new ApiResponse<>(true, "Successfully unfollowed entity", true)
      );
    } else {
      return ResponseEntity.badRequest().body(
              new ApiResponse<>(false, "Failed to unfollow entity", false)
      );
    }
  }

  /**
   * Check if current user follows an entity
   */
  @GetMapping("/check")
  public ResponseEntity<ApiResponse<Boolean>> checkIfFollowing(
          @AuthenticationPrincipal User currentUser,
          @RequestParam Long entityId,
          @RequestParam String entityType) {

    boolean isFollowing = followService.isFollowing(currentUser.getId(), entityId, entityType);

    return ResponseEntity.ok(
            new ApiResponse<>(true, null, isFollowing)
    );
  }

  /**
   * Get all followers of an entity
   */
  @GetMapping("/followers")
  public ResponseEntity<ApiResponse<List<UserIndex>>> getFollowers(
          @RequestParam Long entityId,
          @RequestParam String entityType) {

    List<User> followers = followService.getFollowers(entityId, entityType);
    List<UserIndex> followerDtos = followers.stream()
            .map(userMapper::toUserIndex)
            .collect(Collectors.toList());

    return ResponseEntity.ok(
            new ApiResponse<>(true, null, followerDtos)
    );
  }

  /**
   * Get count of followers of an entity
   */
  @GetMapping("/count")
  public ResponseEntity<ApiResponse<Long>> getFollowersCount(
          @RequestParam Long entityId,
          @RequestParam String entityType) {

    Long count = followService.countFollowers(entityId, entityType);

    return ResponseEntity.ok(
            new ApiResponse<>(true, null, count)
    );
  }

  /**
   * Get all entities of a specific type that the current user follows
   */
  @GetMapping("/following/{entityType}")
  public ResponseEntity<ApiResponse<List<Long>>> getFollowedEntities(
          @AuthenticationPrincipal User currentUser,
          @PathVariable String entityType) {

    List<Long> followedEntities = followService.getFollowedEntities(currentUser.getId(), entityType);

    return ResponseEntity.ok(
            new ApiResponse<>(true, null, followedEntities)
    );
  }
}