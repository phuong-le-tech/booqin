package fr.uge.booqIn.api.controller;

import fr.uge.booqIn.controller.api.VoteController;
import fr.uge.booqIn.dto.vote.VoteCount;
import fr.uge.booqIn.dto.vote.VoteCreate;
import fr.uge.booqIn.dto.vote.VoteResponse;
import fr.uge.booqIn.dto.vote.VoteUpdate;
import fr.uge.booqIn.enums.VoteType;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.interfaces.VoteServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("VoteController Tests")
class VoteControllerTest {

  private VoteServiceInterface voteService;
  private VoteController voteController;

  @BeforeEach
  void setUp() {
    voteService = Mockito.mock(VoteServiceInterface.class);
    voteController = new VoteController(voteService);
  }

  @Test
  @DisplayName("POST /votes - Should cast vote and return vote response")
  void castVote_ShouldReturnVoteResponse() {
    // Given
    Long userId = 1L;
    Long entityId = 2L;
    String entityType = "BOOK";
    VoteType voteType = VoteType.UPVOTE;

    VoteCreate voteCreate = new VoteCreate(userId, entityId, entityType, voteType);

    VoteResponse voteResponse = new VoteResponse(
            1L,
            userId,
            "John Doe",
            entityId,
            entityType,
            voteType
    );

    when(voteService.castVote(any(VoteCreate.class))).thenReturn(voteResponse);

    // When
    ResponseEntity<ApiResponse<VoteResponse>> response = voteController.castVote(voteCreate);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    ApiResponse<VoteResponse> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Vote cast successfully", apiResponse.message());
    assertEquals(voteResponse, apiResponse.data());

    verify(voteService).castVote(eq(voteCreate));
  }

  @Test
  @DisplayName("PUT /votes/user/{userId}/entity/{entityId}/type/{entityType} - Should update vote and return vote response")
  void updateVote_ShouldReturnUpdatedVoteResponse() {
    // Given
    Long userId = 1L;
    Long entityId = 2L;
    String entityType = "BOOK";
    VoteType voteType = VoteType.DOWNVOTE;

    VoteUpdate voteUpdate = new VoteUpdate(voteType);

    VoteResponse voteResponse = new VoteResponse(
            1L,
            userId,
            "John Doe",
            entityId,
            entityType,
            voteType
    );

    when(voteService.updateVote(eq(userId), eq(entityId), eq(entityType), any(VoteUpdate.class)))
            .thenReturn(voteResponse);

    // When
    ResponseEntity<ApiResponse<VoteResponse>> response = voteController.updateVote(
            userId, entityId, entityType, voteUpdate);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<VoteResponse> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Vote updated successfully", apiResponse.message());
    assertEquals(voteResponse, apiResponse.data());

    verify(voteService).updateVote(eq(userId), eq(entityId), eq(entityType), eq(voteUpdate));
  }

  @Test
  @DisplayName("DELETE /votes/user/{userId}/entity/{entityId}/type/{entityType} - Should delete vote and return success")
  void deleteVote_ShouldReturnSuccess() {
    // Given
    Long userId = 1L;
    Long entityId = 2L;
    String entityType = "BOOK";

    doNothing().when(voteService).deleteVote(eq(userId), eq(entityId), eq(entityType));

    // When
    ResponseEntity<ApiResponse<Void>> response = voteController.deleteVote(userId, entityId, entityType);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<Void> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Vote deleted successfully", apiResponse.message());
    assertNull(apiResponse.data());

    verify(voteService).deleteVote(eq(userId), eq(entityId), eq(entityType));
  }

  @Test
  @DisplayName("GET /votes/user/{userId}/entity/{entityId}/type/{entityType} - Should return vote response")
  void getVote_ShouldReturnVoteResponse() {
    // Given
    Long userId = 1L;
    Long entityId = 2L;
    String entityType = "BOOK";
    VoteType voteType = VoteType.UPVOTE;

    VoteResponse voteResponse = new VoteResponse(
            1L,
            userId,
            "John Doe",
            entityId,
            entityType,
            voteType
    );

    when(voteService.getVote(eq(userId), eq(entityId), eq(entityType))).thenReturn(voteResponse);

    // When
    ResponseEntity<ApiResponse<VoteResponse>> response = voteController.getVote(userId, entityId, entityType);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<VoteResponse> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Vote found", apiResponse.message());
    assertEquals(voteResponse, apiResponse.data());

    verify(voteService).getVote(eq(userId), eq(entityId), eq(entityType));
  }

  @Test
  @DisplayName("GET /votes/entity/{entityId}/type/{entityType} - Should return list of votes for entity")
  void getVotesByEntity_ShouldReturnVotesList() {
    // Given
    Long entityId = 2L;
    String entityType = "BOOK";

    List<VoteResponse> votes = List.of(
            new VoteResponse(1L, 1L, "User 1", entityId, entityType, VoteType.UPVOTE),
            new VoteResponse(2L, 2L, "User 2", entityId, entityType, VoteType.DOWNVOTE)
    );

    when(voteService.getVotesByEntity(eq(entityId), eq(entityType))).thenReturn(votes);

    // When
    ResponseEntity<ApiResponse<List<VoteResponse>>> response = voteController.getVotesByEntity(entityId, entityType);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<List<VoteResponse>> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Votes found", apiResponse.message());
    assertEquals(votes, apiResponse.data());
    assertEquals(2, apiResponse.data().size());

    verify(voteService).getVotesByEntity(eq(entityId), eq(entityType));
  }

  @Test
  @DisplayName("GET /votes/user/{userId} - Should return list of votes by user")
  void getVotesByUser_ShouldReturnVotesList() {
    // Given
    Long userId = 1L;

    List<VoteResponse> votes = List.of(
            new VoteResponse(1L, userId, "John Doe", 2L, "BOOK", VoteType.UPVOTE),
            new VoteResponse(2L, userId, "John Doe", 3L, "COLLECTION", VoteType.DOWNVOTE)
    );

    when(voteService.getVotesByUser(eq(userId))).thenReturn(votes);

    // When
    ResponseEntity<ApiResponse<List<VoteResponse>>> response = voteController.getVotesByUser(userId);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<List<VoteResponse>> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Votes found", apiResponse.message());
    assertEquals(votes, apiResponse.data());
    assertEquals(2, apiResponse.data().size());

    verify(voteService).getVotesByUser(eq(userId));
  }

  @Test
  @DisplayName("GET /votes/count/entity/{entityId}/type/{entityType} - Should return vote counts")
  void getVoteCounts_ShouldReturnVoteCounts() {
    // Given
    Long entityId = 2L;
    String entityType = "BOOK";
    VoteCount voteCount = new VoteCount(entityId, entityType, 5L, 2L, 3);

    when(voteService.getVoteCounts(eq(entityId), eq(entityType))).thenReturn(voteCount);

    // When
    ResponseEntity<ApiResponse<VoteCount>> response = voteController.getVoteCounts(entityId, entityType);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<VoteCount> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Vote counts retrieved", apiResponse.message());
    assertEquals(voteCount, apiResponse.data());

    verify(voteService).getVoteCounts(eq(entityId), eq(entityType));
  }

  @Test
  @DisplayName("GET /votes/user/{userId}/entity/{entityId}/type/{entityType}/has-voted - Should return has voted status")
  void hasVoted_ShouldReturnHasVotedStatus() {
    // Given
    Long userId = 1L;
    Long entityId = 2L;
    String entityType = "BOOK";

    when(voteService.hasVoted(eq(userId), eq(entityId), eq(entityType))).thenReturn(true);

    // When
    ResponseEntity<ApiResponse<Boolean>> response = voteController.hasVoted(userId, entityId, entityType);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<Boolean> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Has voted status retrieved", apiResponse.message());
    assertTrue(apiResponse.data());

    verify(voteService).hasVoted(eq(userId), eq(entityId), eq(entityType));
  }

  @Test
  @DisplayName("GET /votes/user/{userId}/entity/{entityId}/type/{entityType}/vote-type - Should return vote type")
  void getUserVoteType_ShouldReturnVoteType() {
    // Given
    Long userId = 1L;
    Long entityId = 2L;
    String entityType = "BOOK";
    VoteType voteType = VoteType.UPVOTE;

    when(voteService.getUserVoteType(eq(userId), eq(entityId), eq(entityType))).thenReturn(voteType);

    // When
    ResponseEntity<ApiResponse<VoteType>> response = voteController.getUserVoteType(userId, entityId, entityType);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<VoteType> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Vote type retrieved", apiResponse.message());
    assertEquals(voteType, apiResponse.data());

    verify(voteService).getUserVoteType(eq(userId), eq(entityId), eq(entityType));
  }

  @Test
  @DisplayName("GET /votes/user/{userId}/entity/{entityId}/type/{entityType}/vote-type - Should return message when user has not voted")
  void getUserVoteType_ShouldReturnMessage_WhenUserHasNotVoted() {
    // Given
    Long userId = 1L;
    Long entityId = 2L;
    String entityType = "BOOK";

    when(voteService.getUserVoteType(eq(userId), eq(entityId), eq(entityType))).thenReturn(null);

    // When
    ResponseEntity<ApiResponse<VoteType>> response = voteController.getUserVoteType(userId, entityId, entityType);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<VoteType> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("User has not voted", apiResponse.message());
    assertNull(apiResponse.data());

    verify(voteService).getUserVoteType(eq(userId), eq(entityId), eq(entityType));
  }
}