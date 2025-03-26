package fr.uge.booqIn.web.controller;

import fr.uge.booqIn.controller.web.CommentWebController;
import fr.uge.booqIn.dto.comment.CommentCreateDTO;
import fr.uge.booqIn.dto.comment.CommentDTO;
import fr.uge.booqIn.dto.comment.CommentUpdateDTO;
import fr.uge.booqIn.dto.vote.VoteCreate;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.VoteType;
import fr.uge.booqIn.service.interfaces.CommentServiceInterface;
import fr.uge.booqIn.service.interfaces.VoteServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("CommentWebController Tests")
class CommentWebControllerTest {

  @Mock
  private CommentServiceInterface commentService;

  @Mock
  private VoteServiceInterface voteService;

  @Mock
  private Model model;

  @Mock
  private RedirectAttributes redirectAttributes;

  @Mock
  private User currentUser;

  @InjectMocks
  private CommentWebController commentWebController;

  private CommentDTO testCommentDTO;
  private CommentCreateDTO testCommentCreateDTO;
  private CommentUpdateDTO testCommentUpdateDTO;
  private List<CommentDTO> testCommentReplies;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    // Setup test data
    testCommentReplies = new ArrayList<>();

    testCommentDTO = new CommentDTO(
            1L,
            "Test comment content",
            1L,
            "testuser",
            2L,
            "BOOK",
            LocalDateTime.now(),
            LocalDateTime.now(),
            testCommentReplies
    );

    testCommentCreateDTO = new CommentCreateDTO(
            1L,
            2L,
            "BOOK",
            "Test comment content",
            null
    );

    testCommentUpdateDTO = new CommentUpdateDTO("Updated comment content");

    // Common mock setup
    when(currentUser.getId()).thenReturn(1L);
  }

  @Test
  @DisplayName("addComment - Should add comment and redirect")
  void addComment_ShouldAddCommentAndRedirect() {
    // Given
    String content = "Test comment content";
    Long entityId = 2L;
    String entityType = "BOOK";
    Long parentId = null;
    String redirectParams = null;

    when(commentService.create(any(CommentCreateDTO.class))).thenReturn(testCommentDTO);

    // When
    String viewName = commentWebController.addComment(content, entityId, entityType, parentId, redirectParams, currentUser, redirectAttributes);

    // Then
    assertEquals("redirect:/books/2?highlight=1", viewName);

    ArgumentCaptor<CommentCreateDTO> dtoCaptor = ArgumentCaptor.forClass(CommentCreateDTO.class);
    verify(commentService).create(dtoCaptor.capture());
    CommentCreateDTO capturedDto = dtoCaptor.getValue();
    assertEquals(1L, capturedDto.userId());
    assertEquals(2L, capturedDto.entityId());
    assertEquals("BOOK", capturedDto.entityType());
    assertEquals("Test comment content", capturedDto.content());
    assertEquals(null, capturedDto.parentId());

    verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
  }

  @Test
  @DisplayName("addComment - Should handle custom redirect params")
  void addComment_WithRedirectParams_ShouldUseCustomRedirect() {
    // Given
    String content = "Test comment content";
    Long entityId = 2L;
    String entityType = "BOOK";
    Long parentId = null;
    String redirectParams = "page=1&sort=newest&{newCommentId}=active";

    when(commentService.create(any(CommentCreateDTO.class))).thenReturn(testCommentDTO);

    // When
    String viewName = commentWebController.addComment(content, entityId, entityType, parentId, redirectParams, currentUser, redirectAttributes);

    // Then
    assertEquals("redirect:/books/2?page=1&sort=newest&1=active", viewName);

    verify(commentService).create(any(CommentCreateDTO.class));
    verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
  }

  @Test
  @DisplayName("addComment - Should handle exception")
  void addComment_WithException_ShouldRedirectWithError() {
    // Given
    String content = "Test comment content";
    Long entityId = 2L;
    String entityType = "BOOK";
    Long parentId = null;
    String redirectParams = null;

    when(commentService.create(any(CommentCreateDTO.class))).thenThrow(new RuntimeException("Test error"));

    // When
    String viewName = commentWebController.addComment(content, entityId, entityType, parentId, redirectParams, currentUser, redirectAttributes);

    // Then
    assertEquals("redirect:/books/2", viewName);

    verify(commentService).create(any(CommentCreateDTO.class));
    verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
  }

  @Test
  @DisplayName("addReply - Should add reply and redirect")
  void addReply_ShouldAddReplyAndRedirect() {
    // Given
    String content = "Test reply content";
    Long entityId = 2L;
    String entityType = "BOOK";
    Long parentId = 1L;
    String redirectParams = null;

    when(commentService.create(any(CommentCreateDTO.class))).thenReturn(testCommentDTO);

    // When
    String viewName = commentWebController.addReply(content, entityId, entityType, parentId, redirectParams, currentUser, redirectAttributes);

    // Then
    assertEquals("redirect:/books/2?highlight=1&parent=1", viewName);

    ArgumentCaptor<CommentCreateDTO> dtoCaptor = ArgumentCaptor.forClass(CommentCreateDTO.class);
    verify(commentService).create(dtoCaptor.capture());
    CommentCreateDTO capturedDto = dtoCaptor.getValue();
    assertEquals(1L, capturedDto.userId());
    assertEquals(2L, capturedDto.entityId());
    assertEquals("BOOK", capturedDto.entityType());
    assertEquals("Test reply content", capturedDto.content());
    assertEquals(1L, capturedDto.parentId());

    verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
  }

  @Test
  @DisplayName("addReply - Should handle custom redirect params")
  void addReply_WithRedirectParams_ShouldUseCustomRedirect() {
    // Given
    String content = "Test reply content";
    Long entityId = 2L;
    String entityType = "BOOK";
    Long parentId = 1L;
    String redirectParams = "page=1&sort=newest&{newCommentId}=active";

    when(commentService.create(any(CommentCreateDTO.class))).thenReturn(testCommentDTO);

    // When
    String viewName = commentWebController.addReply(content, entityId, entityType, parentId, redirectParams, currentUser, redirectAttributes);

    // Then
    assertEquals("redirect:/books/2?page=1&sort=newest&1=active", viewName);

    verify(commentService).create(any(CommentCreateDTO.class));
    verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
  }

  @Test
  @DisplayName("addReply - Should handle exception")
  void addReply_WithException_ShouldRedirectWithError() {
    // Given
    String content = "Test reply content";
    Long entityId = 2L;
    String entityType = "BOOK";
    Long parentId = 1L;
    String redirectParams = null;

    when(commentService.create(any(CommentCreateDTO.class))).thenThrow(new RuntimeException("Test error"));

    // When
    String viewName = commentWebController.addReply(content, entityId, entityType, parentId, redirectParams, currentUser, redirectAttributes);

    // Then
    assertEquals("redirect:/books/2", viewName);

    verify(commentService).create(any(CommentCreateDTO.class));
    verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
  }

  @Test
  @DisplayName("showEditForm - Should display edit form")
  void showEditForm_ShouldDisplayEditForm() {
    // Given
    Long commentId = 1L;
    Long entityId = 2L;
    String entityType = "BOOK";

    when(commentService.getById(eq(commentId))).thenReturn(testCommentDTO);

    // When
    String viewName = commentWebController.showEditForm(commentId, entityId, entityType, currentUser, model, redirectAttributes);

    // Then
    assertEquals("comment/edit", viewName);

    verify(commentService).getById(eq(commentId));
    verify(model).addAttribute(eq("comment"), eq(testCommentDTO));
    verify(model).addAttribute(eq("entityId"), eq(entityId));
    verify(model).addAttribute(eq("entityType"), eq(entityType));
  }

  @Test
  @DisplayName("showEditForm - Should redirect when user is not comment owner")
  void showEditForm_UserNotOwner_ShouldRedirectWithError() {
    // Given
    Long commentId = 1L;
    Long entityId = 2L;
    String entityType = "BOOK";

    CommentDTO otherUserComment = new CommentDTO(
            1L,
            "Test comment content",
            2L, // Different user ID
            "otheruser",
            2L,
            "BOOK",
            LocalDateTime.now(),
            LocalDateTime.now()
    );

    when(commentService.getById(eq(commentId))).thenReturn(otherUserComment);

    // When
    String viewName = commentWebController.showEditForm(commentId, entityId, entityType, currentUser, model, redirectAttributes);

    // Then
    assertEquals("redirect:/books/2", viewName);

    verify(commentService).getById(eq(commentId));
    verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
  }

  @Test
  @DisplayName("showEditForm - Should handle exception")
  void showEditForm_WithException_ShouldRedirectWithError() {
    // Given
    Long commentId = 1L;
    Long entityId = 2L;
    String entityType = "BOOK";

    when(commentService.getById(eq(commentId))).thenThrow(new RuntimeException("Test error"));

    // When
    String viewName = commentWebController.showEditForm(commentId, entityId, entityType, currentUser, model, redirectAttributes);

    // Then
    assertEquals("redirect:/books/2", viewName);

    verify(commentService).getById(eq(commentId));
    verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
  }

  @Test
  @DisplayName("updateComment - Should update comment and redirect")
  void updateComment_ShouldUpdateCommentAndRedirect() {
    // Given
    Long commentId = 1L;
    String content = "Updated comment content";

    when(commentService.getById(eq(commentId))).thenReturn(testCommentDTO);

    // When
    String viewName = commentWebController.updateComment(commentId, content, currentUser, redirectAttributes);

    // Then
    assertEquals("redirect:/books/2?highlight=1", viewName);

    verify(commentService).getById(eq(commentId));
    verify(commentService).update(eq(commentId), eq(1L), any(CommentUpdateDTO.class));
    verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
  }

  @Test
  @DisplayName("updateComment - Should redirect when user is not comment owner")
  void updateComment_UserNotOwner_ShouldRedirectWithError() {
    // Given
    Long commentId = 1L;
    String content = "Updated comment content";

    CommentDTO otherUserComment = new CommentDTO(
            1L,
            "Test comment content",
            2L, // Different user ID
            "otheruser",
            2L,
            "BOOK",
            LocalDateTime.now(),
            LocalDateTime.now()
    );

    when(commentService.getById(eq(commentId))).thenReturn(otherUserComment);

    // When
    String viewName = commentWebController.updateComment(commentId, content, currentUser, redirectAttributes);

    // Then
    assertEquals("redirect:/books/2", viewName);

    verify(commentService).getById(eq(commentId));
    verify(commentService, never()).update(anyLong(), anyLong(), any(CommentUpdateDTO.class));
    verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
  }

  @Test
  @DisplayName("deleteComment - Should delete comment and redirect")
  void deleteComment_ShouldDeleteCommentAndRedirect() {
    // Given
    Long commentId = 1L;
    Long entityId = 2L;
    String entityType = "BOOK";

    doNothing().when(commentService).delete(eq(commentId), eq(1L));

    // When
    String viewName = commentWebController.deleteComment(commentId, entityId, entityType, currentUser, redirectAttributes);

    // Then
    assertEquals("redirect:/books/2", viewName);

    verify(commentService).delete(eq(commentId), eq(1L));
    verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
  }

  @Test
  @DisplayName("deleteComment - Should handle exception")
  void deleteComment_WithException_ShouldRedirectWithError() {
    // Given
    Long commentId = 1L;
    Long entityId = 2L;
    String entityType = "BOOK";

    doThrow(new RuntimeException("Test error")).when(commentService).delete(eq(commentId), eq(1L));

    // When
    String viewName = commentWebController.deleteComment(commentId, entityId, entityType, currentUser, redirectAttributes);

    // Then
    assertEquals("redirect:/books/2", viewName);

    verify(commentService).delete(eq(commentId), eq(1L));
    verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
  }

  @Test
  @DisplayName("vote - Should cast vote and redirect")
  void vote_ShouldCastVoteAndRedirect() {
    // Given
    Long commentId = 1L;
    String redirectEntity = "BOOK";
    Long redirectId = 2L;
    VoteType voteType = VoteType.UPVOTE;

    // When
    String viewName = commentWebController.vote(commentId, redirectEntity, redirectId, voteType, currentUser);

    // Then
    assertEquals("redirect:/books/2", viewName);

    ArgumentCaptor<VoteCreate> voteCaptor = ArgumentCaptor.forClass(VoteCreate.class);
    verify(voteService).castVote(voteCaptor.capture());
    VoteCreate capturedVote = voteCaptor.getValue();
    assertEquals(1L, capturedVote.userId());
    assertEquals(1L, capturedVote.entityId());
    assertEquals("COMMENT", capturedVote.entityType());
    assertEquals(VoteType.UPVOTE, capturedVote.voteType());
  }

  @Test
  @DisplayName("getEntityRedirectUrl - Should return correct URL for each entity type")
  void getEntityRedirectUrl_ShouldReturnCorrectUrl() {
    // Test private method via the public methods that use it
    assertEquals("redirect:/books/1", commentWebController.vote(1L, "BOOK", 1L, VoteType.UPVOTE, currentUser));
    assertEquals("redirect:/collections/1", commentWebController.vote(1L, "COLLECTION", 1L, VoteType.UPVOTE, currentUser));
    assertEquals("redirect:/authors/1", commentWebController.vote(1L, "AUTHOR", 1L, VoteType.UPVOTE, currentUser));
    assertEquals("redirect:/users/1", commentWebController.vote(1L, "USER", 1L, VoteType.UPVOTE, currentUser));
    assertEquals("redirect:/", commentWebController.vote(1L, "UNKNOWN", 1L, VoteType.UPVOTE, currentUser));
  }
}