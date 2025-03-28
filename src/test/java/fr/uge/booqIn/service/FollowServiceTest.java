package fr.uge.booqIn.service;

import fr.uge.booqIn.entity.Follow;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.event.BooqInEventPublisher;
import fr.uge.booqIn.event.publishable.AddFollowedUserBooks;
import fr.uge.booqIn.event.publishable.RemoveFollowedUserBooks;
import fr.uge.booqIn.interfaces.Followable;
import fr.uge.booqIn.repository.FollowRepository;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.impl.FollowServiceImpl;
import fr.uge.booqIn.service.interfaces.FollowServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("FollowService Tests")
class FollowServiceTest {

  private FollowRepository followRepository;
  private UserRepository userRepository;
  private BooqInEventPublisher eventPublisher;
  private FollowServiceInterface followService;
  private User testUser;
  private User testFollowedUser;
  private Follow testFollow;
  private Followable testFollowable;

  @BeforeEach
  void setUp() {
    followRepository = Mockito.mock(FollowRepository.class);
    userRepository = Mockito.mock(UserRepository.class);
    eventPublisher = Mockito.mock(BooqInEventPublisher.class);

    followService = new FollowServiceImpl(followRepository, userRepository, eventPublisher);

    // Setup test data
    testUser = new User();
    testUser.setId(1L);
    testUser.setFirstName("John");
    testUser.setLastName("Doe");

    testFollowedUser = new User();
    testFollowedUser.setId(2L);
    testFollowedUser.setFirstName("Jane");
    testFollowedUser.setLastName("Smith");

    testFollowable = new Followable() {
      @Override
      public Long getId() {
        return 2L;
      }

      @Override
      public String getEntityType() {
        return "USER";
      }
    };

    testFollow = Follow.create(testUser, testFollowable);
  }

  @Test
  @DisplayName("followEntity - Should create follow relationship for entity")
  void followEntity_WithValidData_ShouldCreateFollow() {
    // Given
    Long followerId = testUser.getId();

    when(followRepository.existsByFollowerAndEntity(eq(followerId), eq(testFollowable.getId()), eq(testFollowable.getEntityType()))).thenReturn(false);
    when(userRepository.findById(eq(followerId))).thenReturn(Optional.of(testUser));
    when(userRepository.findById(eq(testFollowable.getId()))).thenReturn(Optional.of(testFollowedUser));
    when(followRepository.save(any(Follow.class))).thenReturn(testFollow);
    doNothing().when(eventPublisher).publishEvent(any(AddFollowedUserBooks.class));

    // When
    Follow result = followService.followEntity(followerId, testFollowable);

    // Then
    assertNotNull(result);

    verify(followRepository).existsByFollowerAndEntity(eq(followerId), eq(testFollowable.getId()), eq(testFollowable.getEntityType()));
    verify(userRepository).findById(eq(followerId));
    verify(userRepository).findById(eq(testFollowable.getId()));
    verify(followRepository).save(any(Follow.class));
    verify(eventPublisher).publishEvent(any(AddFollowedUserBooks.class));

    ArgumentCaptor<Follow> followCaptor = ArgumentCaptor.forClass(Follow.class);
    verify(followRepository).save(followCaptor.capture());
    Follow capturedFollow = followCaptor.getValue();
    assertEquals(testUser, capturedFollow.getFollower());
    assertEquals(testFollowable.getId(), capturedFollow.getEntityId());
    assertEquals(testFollowable.getEntityType(), capturedFollow.getEntityType());
  }

  @Test
  @DisplayName("followEntity - Should throw IllegalStateException when already following")
  void followEntity_WhenAlreadyFollowing_ShouldThrowException() {
    // Given
    Long followerId = testUser.getId();

    when(followRepository.existsByFollowerAndEntity(eq(followerId), eq(testFollowable.getId()), eq(testFollowable.getEntityType()))).thenReturn(true);

    // When & Then
    assertThrows(IllegalStateException.class, () ->
            followService.followEntity(followerId, testFollowable)
    );

    verify(followRepository).existsByFollowerAndEntity(eq(followerId), eq(testFollowable.getId()), eq(testFollowable.getEntityType()));
    verifyNoMoreInteractions(followRepository);
    verifyNoInteractions(eventPublisher);
  }

  @Test
  @DisplayName("followEntity - Should throw IllegalArgumentException when user not found")
  void followEntity_WhenUserNotFound_ShouldThrowException() {
    // Given
    Long followerId = testUser.getId();

    when(followRepository.existsByFollowerAndEntity(eq(followerId), eq(testFollowable.getId()), eq(testFollowable.getEntityType()))).thenReturn(false);
    when(userRepository.findById(eq(followerId))).thenReturn(Optional.empty());

    // When & Then
    assertThrows(IllegalArgumentException.class, () ->
            followService.followEntity(followerId, testFollowable)
    );

    verify(followRepository).existsByFollowerAndEntity(eq(followerId), eq(testFollowable.getId()), eq(testFollowable.getEntityType()));
    verify(userRepository).findById(eq(followerId));
    verifyNoMoreInteractions(followRepository);
    verifyNoInteractions(eventPublisher);
  }

  @Test
  @DisplayName("unfollowEntity - Should unfollow entity and publish event for USER type")
  void unfollowEntity_ForUserType_ShouldUnfollowAndPublishEvent() {
    // Given
    Long followerId = testUser.getId();
    Long entityId = testFollowable.getId();
    String entityType = testFollowable.getEntityType();

    when(userRepository.findById(eq(followerId))).thenReturn(Optional.of(testUser));
    when(userRepository.findById(eq(entityId))).thenReturn(Optional.of(testFollowedUser));
    doNothing().when(eventPublisher).publishEvent(any(RemoveFollowedUserBooks.class));
    doNothing().when(followRepository).deleteByFollowerIdAndEntityIdAndEntityType(eq(followerId), eq(entityId), eq(entityType));

    // When
    boolean result = followService.unfollowEntity(followerId, entityId, entityType);

    // Then
    assertTrue(result);

    verify(userRepository).findById(eq(followerId));
    verify(userRepository).findById(eq(entityId));
    verify(eventPublisher).publishEvent(any(RemoveFollowedUserBooks.class));
    verify(followRepository).deleteByFollowerIdAndEntityIdAndEntityType(eq(followerId), eq(entityId), eq(entityType));
  }

  @Test
  @DisplayName("isFollowing - Should return true when following entity")
  void isFollowing_WhenFollowing_ShouldReturnTrue() {
    // Given
    Long followerId = testUser.getId();
    Long entityId = testFollowable.getId();
    String entityType = testFollowable.getEntityType();

    when(followRepository.existsByFollowerAndEntity(eq(followerId), eq(entityId), eq(entityType))).thenReturn(true);

    // When
    boolean result = followService.isFollowing(followerId, entityId, entityType);

    // Then
    assertTrue(result);

    verify(followRepository).existsByFollowerAndEntity(eq(followerId), eq(entityId), eq(entityType));
  }

  @Test
  @DisplayName("isFollowing - Should return false when not following entity")
  void isFollowing_WhenNotFollowing_ShouldReturnFalse() {
    // Given
    Long followerId = testUser.getId();
    Long entityId = testFollowable.getId();
    String entityType = testFollowable.getEntityType();

    when(followRepository.existsByFollowerAndEntity(eq(followerId), eq(entityId), eq(entityType))).thenReturn(false);

    // When
    boolean result = followService.isFollowing(followerId, entityId, entityType);

    // Then
    assertFalse(result);

    verify(followRepository).existsByFollowerAndEntity(eq(followerId), eq(entityId), eq(entityType));
  }

  @Test
  @DisplayName("getFollowedEntities - Should return list of followed entity IDs")
  void getFollowedEntities_ShouldReturnFollowedEntityIds() {
    // Given
    Long followerId = testUser.getId();
    String entityType = testFollowable.getEntityType();
    List<Follow> follows = Arrays.asList(testFollow);
    List<Long> expectedIds = Arrays.asList(testFollowable.getId());

    when(followRepository.findByFollowerIdAndEntityType(eq(followerId), eq(entityType))).thenReturn(follows);

    // When
    List<Long> result = followService.getFollowedEntities(followerId, entityType);

    // Then
    assertEquals(expectedIds, result);

    verify(followRepository).findByFollowerIdAndEntityType(eq(followerId), eq(entityType));
  }

  @Test
  @DisplayName("getFollowers - Should return list of follower users")
  void getFollowers_ShouldReturnFollowerUsers() {
    // Given
    Long entityId = testFollowable.getId();
    String entityType = testFollowable.getEntityType();
    List<Follow> follows = Arrays.asList(testFollow);
    List<User> expectedUsers = Arrays.asList(testUser);

    when(followRepository.findByEntityIdAndEntityType(eq(entityId), eq(entityType))).thenReturn(follows);

    // When
    List<User> result = followService.getFollowers(entityId, entityType);

    // Then
    assertEquals(expectedUsers, result);

    verify(followRepository).findByEntityIdAndEntityType(eq(entityId), eq(entityType));
  }

  @Test
  @DisplayName("countFollowers - Should return count of followers")
  void countFollowers_ShouldReturnFollowerCount() {
    // Given
    Long entityId = testFollowable.getId();
    String entityType = testFollowable.getEntityType();
    long expectedCount = 5L;

    when(followRepository.countByEntityIdAndEntityType(eq(entityId), eq(entityType))).thenReturn(expectedCount);

    // When
    Long result = followService.countFollowers(entityId, entityType);

    // Then
    assertEquals(expectedCount, result);

    verify(followRepository).countByEntityIdAndEntityType(eq(entityId), eq(entityType));
  }
}