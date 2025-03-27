package fr.uge.booqIn.api.controller;

import fr.uge.booqIn.controller.api.FollowController;
import fr.uge.booqIn.dto.follow.FollowDto;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.Follow;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.mapper.UserMapper;
import fr.uge.booqIn.model.Followable;
import fr.uge.booqIn.service.interfaces.BookCollectionServiceInterface;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import fr.uge.booqIn.service.interfaces.FollowServiceInterface;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("FollowController Tests")
class FollowControllerTest {

  private FollowServiceInterface followService;
  private UserServiceInterface userService;
  private BookServiceInterface bookService;
  private BookCollectionServiceInterface collectionService;
  private UserMapper userMapper;
  private FollowController followController;
  private User testUser;

  @BeforeEach
  void setUp() {
    followService = Mockito.mock(FollowServiceInterface.class);
    userService = Mockito.mock(UserServiceInterface.class);
    bookService = Mockito.mock(BookServiceInterface.class);
    collectionService = Mockito.mock(BookCollectionServiceInterface.class);
    userMapper = Mockito.mock(UserMapper.class);
    followController = new FollowController(followService, userService, bookService, collectionService, userMapper);
    testUser = createTestUser();
  }

  private User createTestUser() {
    User user = new User("John", "Doe", "john.doe@example.com", "password", null, UserRole.USER);
    user.setId(1L);
    return user;
  }

  @Test
  @DisplayName("POST /follows - Should follow user entity and return FollowDto")
  void followEntity_ShouldFollowUserAndReturnFollowDto() {
    // Given
    Long entityId = 2L;
    String entityType = "INVALID";
    Map<String, Object> request = Map.of("entityId", entityId, "entityType", entityType);

    // When
    ResponseEntity<ApiResponse<FollowDto>> response = followController.followEntity(testUser, request);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ApiResponse<FollowDto> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertFalse(apiResponse.success());
    assertEquals("Invalid entity type", apiResponse.message());
    assertNull(apiResponse.data());

    verify(userService, never()).findUserById(anyLong());
    verify(bookService, never()).findBookById(anyLong());
    verify(collectionService, never()).findCollectionById(anyLong());
    verify(followService, never()).followEntity(anyLong(), any(Followable.class));
  }

  @Test
  @DisplayName("POST /follows - Should return not found when entity doesn't exist")
  void followEntity_ShouldReturnNotFound_WhenEntityDoesNotExist() {
    // Given
    Long entityId = 999L;
    String entityType = "USER";
    Map<String, Object> request = Map.of("entityId", entityId, "entityType", entityType);

    when(userService.findUserById(entityId)).thenReturn(null);

    // When
    ResponseEntity<ApiResponse<FollowDto>> response = followController.followEntity(testUser, request);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

    verify(userService).findUserById(entityId);
    verify(followService, never()).followEntity(anyLong(), any(Followable.class));
  }

  @Test
  @DisplayName("DELETE /follows - Should unfollow entity and return success")
  void unfollowEntity_ShouldReturnSuccess() {
    // Given
    Long entityId = 2L;
    String entityType = "USER";

    when(followService.unfollowEntity(eq(testUser.getId()), eq(entityId), eq(entityType))).thenReturn(true);

    // When
    ResponseEntity<ApiResponse<Boolean>> response = followController.unfollowEntity(testUser, entityId, entityType);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<Boolean> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Successfully unfollowed entity", apiResponse.message());
    assertTrue(apiResponse.data());

    verify(followService).unfollowEntity(eq(testUser.getId()), eq(entityId), eq(entityType));
  }

  @Test
  @DisplayName("DELETE /follows - Should return bad request when unfollow fails")
  void unfollowEntity_ShouldReturnBadRequest_WhenUnfollowFails() {
    // Given
    Long entityId = 999L;
    String entityType = "USER";

    when(followService.unfollowEntity(eq(testUser.getId()), eq(entityId), eq(entityType))).thenReturn(false);

    // When
    ResponseEntity<ApiResponse<Boolean>> response = followController.unfollowEntity(testUser, entityId, entityType);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ApiResponse<Boolean> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertFalse(apiResponse.success());
    assertEquals("Failed to unfollow entity", apiResponse.message());
    assertFalse(apiResponse.data());

    verify(followService).unfollowEntity(eq(testUser.getId()), eq(entityId), eq(entityType));
  }

  @Test
  @DisplayName("GET /follows/check - Should return true when user follows entity")
  void checkIfFollowing_ShouldReturnTrue_WhenUserFollowsEntity() {
    // Given
    Long entityId = 2L;
    String entityType = "USER";

    when(followService.isFollowing(eq(testUser.getId()), eq(entityId), eq(entityType))).thenReturn(true);

    // When
    ResponseEntity<ApiResponse<Boolean>> response = followController.checkIfFollowing(testUser, entityId, entityType);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<Boolean> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertNull(apiResponse.message());
    assertTrue(apiResponse.data());

    verify(followService).isFollowing(eq(testUser.getId()), eq(entityId), eq(entityType));
  }

  @Test
  @DisplayName("GET /follows/followers - Should return list of followers for entity")
  void getFollowers_ShouldReturnFollowersList() {
    // Given
    Long entityId = 2L;
    String entityType = "BOOK";

    User follower1 = new User("Jane", "Doe", "jane.doe@example.com", "password", null, UserRole.USER);
    follower1.setId(3L);
    User follower2 = new User("Jim", "Smith", "jim.smith@example.com", "password", null, UserRole.USER);
    follower2.setId(4L);

    List<User> followers = List.of(follower1, follower2);

    UserIndex userIndex1 = new UserIndex(3L, "Jane", "Doe", "jane.doe@example.com", null, UserRole.USER);
    UserIndex userIndex2 = new UserIndex(4L, "Jim", "Smith", "jim.smith@example.com", null, UserRole.USER);

    List<UserIndex> followerDtos = List.of(userIndex1, userIndex2);

    when(followService.getFollowers(eq(entityId), eq(entityType))).thenReturn(followers);
    when(userMapper.toUserIndex(follower1)).thenReturn(userIndex1);
    when(userMapper.toUserIndex(follower2)).thenReturn(userIndex2);

    // When
    ResponseEntity<ApiResponse<List<UserIndex>>> response = followController.getFollowers(entityId, entityType);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<List<UserIndex>> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertNull(apiResponse.message());
    assertEquals(followerDtos, apiResponse.data());
    assertEquals(2, apiResponse.data().size());

    verify(followService).getFollowers(eq(entityId), eq(entityType));
    verify(userMapper, times(2)).toUserIndex(any(User.class));
  }

  @Test
  @DisplayName("GET /follows/count - Should return count of followers for entity")
  void getFollowersCount_ShouldReturnFollowersCount() {
    // Given
    Long entityId = 2L;
    String entityType = "BOOK";
    Long followersCount = 5L;

    when(followService.countFollowers(eq(entityId), eq(entityType))).thenReturn(followersCount);

    // When
    ResponseEntity<ApiResponse<Long>> response = followController.getFollowersCount(entityId, entityType);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<Long> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertNull(apiResponse.message());
    assertEquals(followersCount, apiResponse.data());

    verify(followService).countFollowers(eq(entityId), eq(entityType));
  }

  @Test
  @DisplayName("GET /follows/following/{entityType} - Should return list of followed entities")
  void getFollowedEntities_ShouldReturnFollowedEntitiesList() {
    // Given
    String entityType = "BOOK";
    List<Long> followedEntities = List.of(1L, 3L, 5L);

    when(followService.getFollowedEntities(eq(testUser.getId()), eq(entityType))).thenReturn(followedEntities);

    // When
    ResponseEntity<ApiResponse<List<Long>>> response = followController.getFollowedEntities(testUser, entityType);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<List<Long>> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertNull(apiResponse.message());
    assertEquals(followedEntities, apiResponse.data());
    assertEquals(3, apiResponse.data().size());

    verify(followService).getFollowedEntities(eq(testUser.getId()), eq(entityType));
  }
}