/*
package fr.uge.booqIn.service;

import fr.uge.booqIn.dto.vote.VoteCount;
import fr.uge.booqIn.dto.vote.VoteCreate;
import fr.uge.booqIn.dto.vote.VoteResponse;
import fr.uge.booqIn.dto.vote.VoteUpdate;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.entity.Vote;
import fr.uge.booqIn.enums.VoteType;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.mapper.VoteMapper;
import fr.uge.booqIn.model.Votable;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.repository.VoteRepository;
import fr.uge.booqIn.service.impl.VoteServiceImpl;
import fr.uge.booqIn.service.interfaces.VoteServiceInterface;
import fr.uge.booqIn.strategy.EntityStrategyContext;
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

@DisplayName("VoteService Tests")
class VoteServiceTest {

  private VoteRepository voteRepository;
  private UserRepository userRepository;
  private EntityStrategyContext strategyContext;
  private VoteMapper voteMapper;
  private VoteServiceInterface voteService;
  private User testUser;
  private Vote testVote;
  private Votable testVotableEntity;

  @BeforeEach
  void setUp() {
    voteRepository = Mockito.mock(VoteRepository.class);
    userRepository = Mockito.mock(UserRepository.class);
    strategyContext = Mockito.mock(EntityStrategyContext.class);
    voteMapper = Mockito.mock(VoteMapper.class);

    voteService = new VoteServiceImpl(voteRepository, userRepository, strategyContext, voteMapper);

    // Setup test data
    testUser = new User();
    testUser.setId(1L);
    testUser.setFirstName("John");
    testUser.setLastName("Doe");

    // Create a mock Votable entity
    testVotableEntity = Mockito.mock(Votable.class);
    when(testVotableEntity.getId()).thenReturn(2L);
    when(testVotableEntity.getEntityType()).thenReturn("BOOK");

    testVote = new Vote(testUser, VoteType.UPVOTE);
    testVote.setId(1L);
    testVote.setEntityId(testVotableEntity.getId());
    testVote.setEntityType(testVotableEntity.getEntityType());
  }

  @Test
  @DisplayName("castVote - Should create new vote when it doesn't exist")
  void castVote_WhenVoteDoesNotExist_ShouldCreateVote() {
    // Given
    Long userId = testUser.getId();
    Long entityId = testVotableEntity.getId();
    String entityType = testVotableEntity.getEntityType();
    VoteType voteType = VoteType.UPVOTE;

    VoteCreate voteCreate = new VoteCreate(userId, entityId, entityType, voteType);

    VoteResponse expectedResponse = new VoteResponse(
            testVote.getId(),
            userId,
            testUser.getFirstName() + " " + testUser.getLastName(),
            entityId,
            entityType,
            voteType
    );

    when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser));
    when(strategyContext.findByTypeAndId(eq(entityType), eq(entityId))).thenReturn(testVotableEntity);
    when(voteRepository.findByUserAndEntityIdAndEntityType(eq(testUser), eq(entityId), eq(entityType))).thenReturn(Optional.empty());
    when(voteRepository.save(any(Vote.class))).thenReturn(testVote);
    when(voteMapper.toVoteResponse(any(Vote.class))).thenReturn(expectedResponse);

    // When
    VoteResponse result = voteService.castVote(voteCreate);

    // Then
    assertNotNull(result);
    assertEquals(expectedResponse, result);

    verify(userRepository).findById(eq(userId));
    verify(strategyContext).findByTypeAndId(eq(entityType), eq(entityId));
    verify(voteRepository).findByUserAndEntityIdAndEntityType(eq(testUser), eq(entityId), eq(entityType));
    verify(voteRepository).save(any(Vote.class));
    verify(voteMapper).toVoteResponse(eq(testVote));

    ArgumentCaptor<Vote> voteCaptor = ArgumentCaptor.forClass(Vote.class);
    verify(voteRepository).save(voteCaptor.capture());
    Vote capturedVote = voteCaptor.getValue();
    assertEquals(testUser, capturedVote.getUser());
    assertEquals(entityId, capturedVote.getEntityId());
    assertEquals(entityType, capturedVote.getEntityType());
    assertEquals(voteType, capturedVote.getVoteType());
  }

  @Test
  @DisplayName("castVote - Should update existing vote when it exists")
  void castVote_WhenVoteExists_ShouldUpdateVote() {
    // Given
    Long userId = testUser.getId();
    Long entityId = testVotableEntity.getId();
    String entityType = testVotableEntity.getEntityType();
    VoteType newVoteType = VoteType.DOWNVOTE;

    VoteCreate voteCreate = new VoteCreate(userId, entityId, entityType, newVoteType);

    VoteResponse expectedResponse = new VoteResponse(
            testVote.getId(),
            userId,
            testUser.getFirstName() + " " + testUser.getLastName(),
            entityId,
            entityType,
            newVoteType
    );

    when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser));
    when(strategyContext.findByTypeAndId(eq(entityType), eq(entityId))).thenReturn(testVotableEntity);
    when(voteRepository.findByUserAndEntityIdAndEntityType(eq(testUser), eq(entityId), eq(entityType))).thenReturn(Optional.of(testVote));
    when(voteRepository.save(any(Vote.class))).thenReturn(testVote);
    when(voteMapper.toVoteResponse(any(Vote.class))).thenReturn(expectedResponse);

    // When
    VoteResponse result = voteService.castVote(voteCreate);

    // Then
    assertNotNull(result);
    assertEquals(expectedResponse, result);

    verify(userRepository).findById(eq(userId));
    verify(strategyContext).findByTypeAndId(eq(entityType), eq(entityId));
    verify(voteRepository).findByUserAndEntityIdAndEntityType(eq(testUser), eq(entityId), eq(entityType));
    verify(voteRepository).save(eq(testVote));
    verify(voteMapper).toVoteResponse(eq(testVote));

    assertEquals(newVoteType, testVote.getVoteType());
  }

  @Test
  @DisplayName("castVote - Should throw ResourceNotFoundException when user not found")
  void castVote_WhenUserNotFound_ShouldThrowException() {
    // Given
    Long userId = 999L;
    Long entityId = testVotableEntity.getId();
    String entityType = testVotableEntity.getEntityType();
    VoteType voteType = VoteType.UPVOTE;

    VoteCreate voteCreate = new VoteCreate(userId, entityId, entityType, voteType);

    when(userRepository.findById(eq(userId))).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class, () ->
            voteService.castVote(voteCreate)
    );

    verify(userRepository).findById(eq(userId));
    verifyNoInteractions(voteRepository);
    verifyNoInteractions(voteMapper);
  }

  @Test
  @DisplayName("updateVote - Should update and return updated vote")
  void updateVote_WithValidData_ShouldUpdateAndReturnVote() {
    // Given
    Long userId = testUser.getId();
    Long entityId = testVotableEntity.getId();
    String entityType = testVotableEntity.getEntityType();
    VoteType newVoteType = VoteType.DOWNVOTE;

    VoteUpdate voteUpdate = new VoteUpdate(newVoteType);

    VoteResponse expectedResponse = new VoteResponse(
            testVote.getId(),
            userId,
            testUser.getFirstName() + " " + testUser.getLastName(),
            entityId,
            entityType,
            newVoteType
    );

    when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser));
    when(strategyContext.findByTypeAndId(eq(entityType), eq(entityId))).thenReturn(testVotableEntity);
    when(voteRepository.findByUserAndEntityIdAndEntityType(eq(testUser), eq(entityId), eq(entityType))).thenReturn(Optional.of(testVote));
    when(voteRepository.save(any(Vote.class))).thenReturn(testVote);
    when(voteMapper.toVoteResponse(any(Vote.class))).thenReturn(expectedResponse);

    // When
    VoteResponse result = voteService.updateVote(userId, entityId, entityType, voteUpdate);

    // Then
    assertNotNull(result);
    assertEquals(expectedResponse, result);

    verify(userRepository).findById(eq(userId));
    verify(strategyContext).findByTypeAndId(eq(entityType), eq(entityId));
    verify(voteRepository).findByUserAndEntityIdAndEntityType(eq(testUser), eq(entityId), eq(entityType));
    verify(voteRepository).save(eq(testVote));
    verify(voteMapper).toVoteResponse(eq(testVote));

    assertEquals(newVoteType, testVote.getVoteType());
  }

  @Test
  @DisplayName("updateVote - Should throw ResourceNotFoundException when vote not found")
  void updateVote_WhenVoteNotFound_ShouldThrowException() {
    // Given
    Long userId = testUser.getId();
    Long entityId = testVotableEntity.getId();
    String entityType = testVotableEntity.getEntityType();
    VoteType newVoteType = VoteType.DOWNVOTE;

    VoteUpdate voteUpdate = new VoteUpdate(newVoteType);

    when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser));
    when(strategyContext.findByTypeAndId(eq(entityType), eq(entityId))).thenReturn(testVotableEntity);
    when(voteRepository.findByUserAndEntityIdAndEntityType(eq(testUser), eq(entityId), eq(entityType))).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class, () ->
            voteService.updateVote(userId, entityId, entityType, voteUpdate)
    );

    verify(userRepository).findById(eq(userId));
    verify(strategyContext).findByTypeAndId(eq(entityType), eq(entityId));
    verify(voteRepository).findByUserAndEntityIdAndEntityType(eq(testUser), eq(entityId), eq(entityType));
    verify(voteRepository, never()).save(any(Vote.class));
  }

  @Test
  @DisplayName("deleteVote - Should delete existing vote")
  void deleteVote_WithValidData_ShouldDeleteVote() {
    // Given
    Long userId = testUser.getId();
    Long entityId = testVotableEntity.getId();
    String entityType = testVotableEntity.getEntityType();

    when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser));
    when(voteRepository.findByUserAndEntityIdAndEntityType(eq(testUser), eq(entityId), eq(entityType))).thenReturn(Optional.of(testVote));
    doNothing().when(voteRepository).delete(any(Vote.class));

    // When
    voteService.deleteVote(userId, entityId, entityType);

    // Then
    verify(userRepository).findById(eq(userId));
    verify(voteRepository).findByUserAndEntityIdAndEntityType(eq(testUser), eq(entityId), eq(entityType));
    verify(voteRepository).delete(eq(testVote));
  }

  @Test
  @DisplayName("getVote - Should return vote when it exists")
  void getVote_WhenVoteExists_ShouldReturnVote() {
    // Given
    Long userId = testUser.getId();
    Long entityId = testVotableEntity.getId();
    String entityType = testVotableEntity.getEntityType();

    VoteResponse expectedResponse = new VoteResponse(
            testVote.getId(),
            userId,
            testUser.getFirstName() + " " + testUser.getLastName(),
            entityId,
            entityType,
            testVote.getVoteType()
    );

    when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser));
    when(voteRepository.findByUserAndEntityIdAndEntityType(eq(testUser), eq(entityId), eq(entityType))).thenReturn(Optional.of(testVote));
    when(voteMapper.toVoteResponse(any(Vote.class))).thenReturn(expectedResponse);

    // When
    VoteResponse result = voteService.getVote(userId, entityId, entityType);

    // Then
    assertNotNull(result);
    assertEquals(expectedResponse, result);

    verify(userRepository).findById(eq(userId));
    verify(voteRepository).findByUserAndEntityIdAndEntityType(eq(testUser), eq(entityId), eq(entityType));
    verify(voteMapper).toVoteResponse(eq(testVote));
  }

  @Test
  @DisplayName("getVotesByEntity - Should return list of votes for entity")
  void getVotesByEntity_ShouldReturnVotesList() {
    // Given
    Long entityId = testVotableEntity.getId();
    String entityType = testVotableEntity.getEntityType();

    List<Vote> votes = Arrays.asList(testVote);

    VoteResponse voteResponse = new VoteResponse(
            testVote.getId(),
            testUser.getId(),
            testUser.getFirstName() + " " + testUser.getLastName(),
            entityId,
            entityType,
            testVote.getVoteType()
    );

    when(strategyContext.findByTypeAndId(eq(entityType), eq(entityId))).thenReturn(testVotableEntity);
    when(voteRepository.findAllByEntityIdAndEntityType(eq(entityId), eq(entityType))).thenReturn(votes);
    when(voteMapper.toVoteResponse(any(Vote.class))).thenReturn(voteResponse);

    // When
    List<VoteResponse> result = voteService.getVotesByEntity(entityId, entityType);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(voteResponse, result.get(0));

    verify(strategyContext).findByTypeAndId(eq(entityType), eq(entityId));
    verify(voteRepository).findAllByEntityIdAndEntityType(eq(entityId), eq(entityType));
    verify(voteMapper).toVoteResponse(eq(testVote));
  }

  @Test
  @DisplayName("getVotesByUser - Should return list of votes by user")
  void getVotesByUser_ShouldReturnVotesList() {
    // Given
    Long userId = testUser.getId();

    List<Vote> votes = Arrays.asList(testVote);

    VoteResponse voteResponse = new VoteResponse(
            testVote.getId(),
            userId,
            testUser.getFirstName() + " " + testUser.getLastName(),
            testVotableEntity.getId(),
            testVotableEntity.getEntityType(),
            testVote.getVoteType()
    );

    when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser));
    when(voteRepository.findAllByUser(eq(testUser))).thenReturn(votes);
    when(voteMapper.toVoteResponse(any(Vote.class))).thenReturn(voteResponse);

    // When
    List<VoteResponse> result = voteService.getVotesByUser(userId);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(voteResponse, result.get(0));

    verify(userRepository).findById(eq(userId));
    verify(voteRepository).findAllByUser(eq(testUser));
    verify(voteMapper).toVoteResponse(eq(testVote));
  }

  @Test
  @DisplayName("getVoteCounts - Should return vote counts for entity")
  void getVoteCounts_ShouldReturnVoteCounts() {
    // Given
    Long entityId = testVotableEntity.getId();
    String entityType = testVotableEntity.getEntityType();

    long upvoteCount = 5;
    long downvoteCount = 2;
    int score = 3;

    VoteCount expectedVoteCount = new VoteCount(entityId, entityType, upvoteCount, downvoteCount, score);

    when(strategyContext.findByTypeAndId(eq(entityType), eq(entityId))).thenReturn(testVotableEntity);
    when(voteRepository.countByEntityIdAndEntityTypeAndVoteType(eq(entityId), eq(entityType), eq(VoteType.UPVOTE))).thenReturn(upvoteCount);
    when(voteRepository.countByEntityIdAndEntityTypeAndVoteType(eq(entityId), eq(entityType), eq(VoteType.DOWNVOTE))).thenReturn(downvoteCount);
    when(voteRepository.getVoteScore(eq(entityId), eq(entityType))).thenReturn(score);
    when(voteMapper.toVoteCount(eq(entityId), eq(entityType), eq(upvoteCount), eq(downvoteCount), eq(score))).thenReturn(expectedVoteCount);

    // When
    VoteCount result = voteService.getVoteCounts(entityId, entityType);

    // Then
    assertNotNull(result);
    assertEquals(expectedVoteCount, result);

    verify(strategyContext).findByTypeAndId(eq(entityType), eq(entityId));
    verify(voteRepository).countByEntityIdAndEntityTypeAndVoteType(eq(entityId), eq(entityType), eq(VoteType.UPVOTE));
    verify(voteRepository).countByEntityIdAndEntityTypeAndVoteType(eq(entityId), eq(entityType), eq(VoteType.DOWNVOTE));
    verify(voteRepository).getVoteScore(eq(entityId), eq(entityType));
    verify(voteMapper).toVoteCount(eq(entityId), eq(entityType), eq(upvoteCount), eq(downvoteCount), eq(score));
  }

  @Test
  @DisplayName("hasVoted - Should return true when user has voted")
  void hasVoted_WhenUserHasVoted_ShouldReturnTrue() {
    // Given
    Long userId = testUser.getId();
    Long entityId = testVotableEntity.getId();
    String entityType = testVotableEntity.getEntityType();

    when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser));
    when(voteRepository.findByUserAndEntityIdAndEntityType(eq(testUser), eq(entityId), eq(entityType))).thenReturn(Optional.of(testVote));

    // When
    boolean result = voteService.hasVoted(userId, entityId, entityType);

    // Then
    assertTrue(result);

    verify(userRepository).findById(eq(userId));
    verify(voteRepository).findByUserAndEntityIdAndEntityType(eq(testUser), eq(entityId), eq(entityType));
  }

  @Test
  @DisplayName("hasVoted - Should return false when user has not voted")
  void hasVoted_WhenUserHasNotVoted_ShouldReturnFalse() {
    // Given
    Long userId = testUser.getId();
    Long entityId = testVotableEntity.getId();
    String entityType = testVotableEntity.getEntityType();

    when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser));
    when(voteRepository.findByUserAndEntityIdAndEntityType(eq(testUser), eq(entityId), eq(entityType))).thenReturn(Optional.empty());

    // When
    boolean result = voteService.hasVoted(userId, entityId, entityType);

    // Then
    assertFalse(result);

    verify(userRepository).findById(eq(userId));
    verify(voteRepository).findByUserAndEntityIdAndEntityType(eq(testUser), eq(entityId), eq(entityType));
  }

  @Test
  @DisplayName("getUserVoteType - Should return vote type when user has voted")
  void getUserVoteType_WhenUserHasVoted_ShouldReturnVoteType() {
    // Given
    Long userId = testUser.getId();
    Long entityId = testVotableEntity.getId();
    String entityType = testVotableEntity.getEntityType();

    when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser));
    when(voteRepository.findByUserAndEntityIdAndEntityType(eq(testUser), eq(entityId), eq(entityType))).thenReturn(Optional.of(testVote));

    // When
    VoteType result = voteService.getUserVoteType(userId, entityId, entityType);

    // Then
    assertEquals(testVote.getVoteType(), result);

    verify(userRepository).findById(eq(userId));
    verify(voteRepository).findByUserAndEntityIdAndEntityType(eq(testUser), eq(entityId), eq(entityType));
  }

  @Test
  @DisplayName("getUserVoteType - Should return null when user has not voted")
  void getUserVoteType_WhenUserHasNotVoted_ShouldReturnNull() {
    // Given
    Long userId = testUser.getId();
    Long entityId = testVotableEntity.getId();
    String entityType = testVotableEntity.getEntityType();

    when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser));
    when(voteRepository.findByUserAndEntityIdAndEntityType(eq(testUser), eq(entityId), eq(entityType))).thenReturn(Optional.empty());

    // When
    VoteType result = voteService.getUserVoteType(userId, entityId, entityType);

    // Then
    assertNull(result);

    verify(userRepository).findById(eq(userId));
    verify(voteRepository).findByUserAndEntityIdAndEntityType(eq(testUser), eq(entityId), eq(entityType));
  }
}*/
