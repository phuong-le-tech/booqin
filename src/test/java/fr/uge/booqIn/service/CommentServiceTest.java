package fr.uge.booqIn.service;

import fr.uge.booqIn.dto.comment.CommentCreateDTO;
import fr.uge.booqIn.dto.comment.CommentDTO;
import fr.uge.booqIn.dto.comment.CommentUpdateDTO;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.Comment;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.mapper.CommentMapper;
import fr.uge.booqIn.repository.CommentRepository;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.impl.CommentServiceImpl;
import fr.uge.booqIn.service.interfaces.CommentServiceInterface;
import fr.uge.booqIn.strategy.EntityStrategyContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("CommentService Tests")
class CommentServiceTest {

  private CommentRepository commentRepository;
  private UserRepository userRepository;
  private EntityStrategyContext strategyContext;
  private CommentMapper commentMapper;
  private CommentServiceInterface commentService;
  private Comment testComment;
  private User testUser;

  @BeforeEach
  void setUp() {
    commentRepository = Mockito.mock(CommentRepository.class);
    userRepository = Mockito.mock(UserRepository.class);
    strategyContext = Mockito.mock(EntityStrategyContext.class);
    commentMapper = Mockito.mock(CommentMapper.class);

    commentService = new CommentServiceImpl(commentRepository, userRepository, strategyContext, commentMapper);

    // Setup test data
    testUser = new User();
    testUser.setId(1L);
    testUser.setFirstName("John");
    testUser.setLastName("Doe");

    testComment = new Comment();
    testComment.setId(1L);
    testComment.setContent("Test comment");
    testComment.setUser(testUser);
    testComment.setEntityId(2L);
    testComment.setEntityType("BOOK");
    testComment.setCreatedAt(LocalDateTime.now());
  }

  @Test
  @DisplayName("findComments - Should return page of comments when filters are applied")
  void findComments_WithFilters_ShouldReturnFilteredComments() {
    // Given
    String content = "Test";
    Long userId = 1L;
    String entityType = "BOOK";
    Long entityId = 2L;
    Pageable pageable = Pageable.ofSize(10);

    List<Comment> commentList = Collections.singletonList(testComment);
    Page<Comment> commentPage = new PageImpl<>(commentList);

    CommentDTO commentDTO = new CommentDTO(
            testComment.getId(),
            testComment.getContent(),
            testUser.getId(),
            testUser.getFirstName() + " " + testUser.getLastName(),
            entityId,
            entityType,
            testComment.getCreatedAt(),
            testComment.getUpdatedAt()
    );

    when(commentRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(commentPage);
    when(commentMapper.toDTO(any(Comment.class))).thenReturn(commentDTO);

    // When
    Page<CommentDTO> result = commentService.findComments(content, userId, entityType, entityId, pageable);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getTotalElements());
    assertEquals(commentDTO, result.getContent().get(0));

    verify(commentRepository).findAll(any(Specification.class), eq(pageable));
    verify(commentMapper).toDTO(eq(testComment));
  }

  @Test
  @DisplayName("getById - Should return comment when it exists")
  void getById_WithExistingId_ShouldReturnComment() {
    // Given
    Long commentId = testComment.getId();
    CommentDTO commentDTO = new CommentDTO(
            commentId,
            testComment.getContent(),
            testUser.getId(),
            testUser.getFirstName() + " " + testUser.getLastName(),
            2L,
            "BOOK",
            testComment.getCreatedAt(),
            testComment.getUpdatedAt()
    );

    when(commentRepository.findWithUserById(eq(commentId))).thenReturn(Optional.of(testComment));
    when(commentMapper.toDTO(any(Comment.class))).thenReturn(commentDTO);

    // When
    CommentDTO result = commentService.getById(commentId);

    // Then
    assertNotNull(result);
    assertEquals(commentId, result.id());
    assertEquals(testComment.getContent(), result.content());

    verify(commentRepository).findWithUserById(eq(commentId));
    verify(commentMapper).toDTO(eq(testComment));
  }

  @Test
  @DisplayName("getById - Should throw ResourceNotFoundException when comment doesn't exist")
  void getById_WithNonExistingId_ShouldThrowException() {
    // Given
    Long commentId = 999L;

    when(commentRepository.findWithUserById(eq(commentId))).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class, () ->
            commentService.getById(commentId)
    );

    verify(commentRepository).findWithUserById(eq(commentId));
    verifyNoInteractions(commentMapper);
  }

  @Test
  @DisplayName("create - Should create and return new comment")
  void create_WithValidData_ShouldCreateAndReturnComment() {
    // Given
    Long userId = testUser.getId();
    Long entityId = 2L;
    String entityType = "BOOK";
    String content = "New comment";

    CommentCreateDTO createDTO = new CommentCreateDTO(
            userId,
            entityId,
            entityType,
            content,
            null
    );

    CommentDTO commentDTO = new CommentDTO(
            1L,
            content,
            userId,
            testUser.getFirstName() + " " + testUser.getLastName(),
            entityId,
            entityType,
            LocalDateTime.now(),
            null
    );

    when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser));
    when(strategyContext.findByTypeAndId(eq(entityType), eq(entityId))).thenReturn(new Book());
    when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
    when(commentMapper.toDTO(any(Comment.class))).thenReturn(commentDTO);

    // When
    CommentDTO result = commentService.create(createDTO);

    // Then
    assertNotNull(result);
    assertEquals(content, result.content());
    assertEquals(userId, result.userId());
    assertEquals(entityId, result.entityId());
    assertEquals(entityType, result.entityType());

    verify(userRepository).findById(eq(userId));
    verify(strategyContext).findByTypeAndId(eq(entityType), eq(entityId));
    verify(commentRepository).save(any(Comment.class));
    verify(commentMapper).toDTO(any(Comment.class));
  }

  @Test
  @DisplayName("create - Should throw ResourceNotFoundException when user doesn't exist")
  void create_WithNonExistingUser_ShouldThrowException() {
    // Given
    Long userId = 999L;
    Long entityId = 2L;
    String entityType = "BOOK";

    CommentCreateDTO createDTO = new CommentCreateDTO(
            userId,
            entityId,
            entityType,
            "Test comment",
            null
    );

    when(userRepository.findById(eq(userId))).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class, () ->
            commentService.create(createDTO)
    );

    verify(userRepository).findById(eq(userId));
    verifyNoInteractions(commentRepository);
  }

  @Test
  @DisplayName("create - With parent comment should link correctly")
  void create_WithParentComment_ShouldLinkCorrectly() {
    // Given
    Long userId = testUser.getId();
    Long entityId = 2L;
    String entityType = "BOOK";
    Long parentId = 5L;

    Comment parentComment = new Comment();
    parentComment.setId(parentId);
    parentComment.setEntityId(entityId);
    parentComment.setEntityType(entityType);

    CommentCreateDTO createDTO = new CommentCreateDTO(
            userId,
            entityId,
            entityType,
            "Reply comment",
            parentId
    );

    CommentDTO commentDTO = new CommentDTO(
            1L,
            "Reply comment",
            userId,
            testUser.getFirstName() + " " + testUser.getLastName(),
            entityId,
            entityType,
            LocalDateTime.now(),
            null
    );

    when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser));
    when(strategyContext.findByTypeAndId(eq(entityType), eq(entityId))).thenReturn(new Book());
    when(commentRepository.findById(eq(parentId))).thenReturn(Optional.of(parentComment));
    when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
    when(commentMapper.toDTO(any(Comment.class))).thenReturn(commentDTO);

    // When
    CommentDTO result = commentService.create(createDTO);

    // Then
    assertNotNull(result);

    verify(commentRepository).findById(eq(parentId));

    ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
    verify(commentRepository).save(commentCaptor.capture());
    Comment capturedComment = commentCaptor.getValue();
    assertNotNull(capturedComment.getParentComment());
    assertEquals(parentId, capturedComment.getParentComment().getId());
  }

  @Test
  @DisplayName("createComment - Should create and return new comment entity")
  void createComment_WithValidComment_ShouldCreateAndReturnComment() {
    // Given
    Comment comment = new Comment();
    comment.setContent("Test comment");
    comment.setUser(testUser);

    when(userRepository.existsById(eq(testUser.getId()))).thenReturn(true);
    when(commentRepository.save(any(Comment.class))).thenReturn(comment);

    // When
    Comment result = commentService.createComment(comment);

    // Then
    assertNotNull(result);
    assertEquals(comment.getContent(), result.getContent());

    verify(userRepository).existsById(eq(testUser.getId()));
    verify(commentRepository).save(eq(comment));

    ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
    verify(commentRepository).save(commentCaptor.capture());
    Comment capturedComment = commentCaptor.getValue();
    assertNotNull(capturedComment.getCreatedAt());
  }

  @Test
  @DisplayName("update - Should update and return updated comment")
  void update_WithValidData_ShouldUpdateAndReturnComment() {
    // Given
    Long commentId = testComment.getId();
    Long userId = testUser.getId();
    String updatedContent = "Updated content";

    CommentUpdateDTO updateDTO = new CommentUpdateDTO(updatedContent);

    CommentDTO updatedCommentDTO = new CommentDTO(
            commentId,
            updatedContent,
            userId,
            testUser.getFirstName() + " " + testUser.getLastName(),
            2L,
            "BOOK",
            testComment.getCreatedAt(),
            LocalDateTime.now()
    );

    when(commentRepository.findWithUserById(eq(commentId))).thenReturn(Optional.of(testComment));
    when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
    when(commentMapper.toDTO(any(Comment.class))).thenReturn(updatedCommentDTO);

    // When
    CommentDTO result = commentService.update(commentId, userId, updateDTO);

    // Then
    assertNotNull(result);
    assertEquals(updatedContent, result.content());

    verify(commentRepository).findWithUserById(eq(commentId));
    verify(commentRepository).save(any(Comment.class));
    verify(commentMapper).toDTO(any(Comment.class));

    ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
    verify(commentRepository).save(commentCaptor.capture());
    Comment capturedComment = commentCaptor.getValue();
    assertEquals(updatedContent, capturedComment.getContent());
    assertNotNull(capturedComment.getUpdatedAt());
  }

  @Test
  @DisplayName("update - Should throw IllegalArgumentException when user not authorized")
  void update_WithUnauthorizedUser_ShouldThrowException() {
    // Given
    Long commentId = testComment.getId();
    Long unauthorizedUserId = 999L;
    CommentUpdateDTO updateDTO = new CommentUpdateDTO("Updated content");

    when(commentRepository.findWithUserById(eq(commentId))).thenReturn(Optional.of(testComment));

    // When & Then
    assertThrows(IllegalArgumentException.class, () ->
            commentService.update(commentId, unauthorizedUserId, updateDTO)
    );

    verify(commentRepository).findWithUserById(eq(commentId));
    verify(commentRepository, never()).save(any(Comment.class));
  }

  @Test
  @DisplayName("delete - Should delete existing comment")
  void delete_WithValidData_ShouldDeleteComment() {
    // Given
    Long commentId = testComment.getId();
    Long userId = testUser.getId();

    when(commentRepository.findWithUserById(eq(commentId))).thenReturn(Optional.of(testComment));
    doNothing().when(commentRepository).delete(any(Comment.class));

    // When
    commentService.delete(commentId, userId);

    // Then
    verify(commentRepository).findWithUserById(eq(commentId));
    verify(commentRepository).delete(eq(testComment));
  }

  @Test
  @DisplayName("delete - Should throw IllegalArgumentException when user not authorized")
  void delete_WithUnauthorizedUser_ShouldThrowException() {
    // Given
    Long commentId = testComment.getId();
    Long unauthorizedUserId = 999L;

    when(commentRepository.findWithUserById(eq(commentId))).thenReturn(Optional.of(testComment));

    // When & Then
    assertThrows(IllegalArgumentException.class, () ->
            commentService.delete(commentId, unauthorizedUserId)
    );

    verify(commentRepository).findWithUserById(eq(commentId));
    verify(commentRepository, never()).delete(any(Comment.class));
  }

  @Test
  @DisplayName("getByEntity - Should return comments for an entity")
  void getByEntity_ShouldReturnCommentsForEntity() {
    // Given
    Long entityId = 2L;
    String entityType = "BOOK";

    List<Comment> comments = Collections.singletonList(testComment);
    List<CommentDTO> commentDTOs = Collections.singletonList(
            new CommentDTO(
                    testComment.getId(),
                    testComment.getContent(),
                    testUser.getId(),
                    testUser.getFirstName() + " " + testUser.getLastName(),
                    entityId,
                    entityType,
                    testComment.getCreatedAt(),
                    testComment.getUpdatedAt()
            )
    );

    when(strategyContext.findByTypeAndId(eq(entityType), eq(entityId))).thenReturn(new Book());
    when(commentRepository.findRootCommentsByEntity(eq(entityId), eq(entityType))).thenReturn(comments);
    when(commentMapper.toDTOList(eq(comments))).thenReturn(commentDTOs);

    // When
    List<CommentDTO> result = commentService.getByEntity(entityId, entityType);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(testComment.getId(), result.get(0).id());

    verify(strategyContext).findByTypeAndId(eq(entityType), eq(entityId));
    verify(commentRepository).findRootCommentsByEntity(eq(entityId), eq(entityType));
    verify(commentMapper).toDTOList(eq(comments));
  }

  @Test
  @DisplayName("getRepliesByCommentId - Should return replies to a comment")
  void getRepliesByCommentId_ShouldReturnReplies() {
    // Given
    Long commentId = 1L;

    Comment reply1 = new Comment();
    reply1.setId(2L);
    reply1.setContent("Reply 1");
    reply1.setUser(testUser);

    Comment reply2 = new Comment();
    reply2.setId(3L);
    reply2.setContent("Reply 2");
    reply2.setUser(testUser);

    List<Comment> replies = Arrays.asList(reply1, reply2);

    List<CommentDTO> replyDTOs = Arrays.asList(
            new CommentDTO(2L, "Reply 1", testUser.getId(), testUser.getFirstName() + " " + testUser.getLastName(),
                    2L, "BOOK", LocalDateTime.now(), null),
            new CommentDTO(3L, "Reply 2", testUser.getId(), testUser.getFirstName() + " " + testUser.getLastName(),
                    2L, "BOOK", LocalDateTime.now(), null)
    );

    when(commentRepository.existsById(eq(commentId))).thenReturn(true);
    when(commentRepository.findRepliesByParentCommentId(eq(commentId))).thenReturn(replies);
    when(commentMapper.toDTOList(eq(replies))).thenReturn(replyDTOs);

    // When
    List<CommentDTO> result = commentService.getRepliesByCommentId(commentId);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());

    verify(commentRepository).existsById(eq(commentId));
    verify(commentRepository).findRepliesByParentCommentId(eq(commentId));
    verify(commentMapper).toDTOList(eq(replies));
  }

  @Test
  @DisplayName("getByUser - Should return comments by a user")
  void getByUser_ShouldReturnUserComments() {
    // Given
    Long userId = testUser.getId();

    List<Comment> comments = Collections.singletonList(testComment);
    List<CommentDTO> commentDTOs = Collections.singletonList(
            new CommentDTO(
                    testComment.getId(),
                    testComment.getContent(),
                    userId,
                    testUser.getFirstName() + " " + testUser.getLastName(),
                    2L,
                    "BOOK",
                    testComment.getCreatedAt(),
                    testComment.getUpdatedAt()
            )
    );

    when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUser));
    when(commentRepository.findByUserOrderByCreatedAtDesc(eq(testUser))).thenReturn(comments);
    when(commentMapper.toDTOList(eq(comments))).thenReturn(commentDTOs);

    // When
    List<CommentDTO> result = commentService.getByUser(userId);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());

    verify(userRepository).findById(eq(userId));
    verify(commentRepository).findByUserOrderByCreatedAtDesc(eq(testUser));
    verify(commentMapper).toDTOList(eq(comments));
  }

  @Test
  @DisplayName("countByEntity - Should return count of comments for an entity")
  void countByEntity_ShouldReturnCount() {
    // Given
    Long entityId = 2L;
    String entityType = "BOOK";
    Long commentCount = 5L;

    when(strategyContext.findByTypeAndId(eq(entityType), eq(entityId))).thenReturn(new Book());
    when(commentRepository.countByEntity(eq(entityId), eq(entityType))).thenReturn(commentCount);

    // When
    Long result = commentService.countByEntity(entityId, entityType);

    // Then
    assertEquals(commentCount, result);

    verify(strategyContext).findByTypeAndId(eq(entityType), eq(entityId));
    verify(commentRepository).countByEntity(eq(entityId), eq(entityType));
  }

  @Test
  @DisplayName("findAll - Should return all comments")
  void findAll_ShouldReturnAllComments() {
    // Given
    List<Comment> comments = Collections.singletonList(testComment);

    when(commentRepository.findAll()).thenReturn(comments);

    // When
    List<Comment> result = commentService.findAll();

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(testComment, result.getFirst());

    verify(commentRepository).findAll();
  }
}