package fr.uge.booqIn.api.controller;

import fr.uge.booqIn.controller.api.CommentController;
import fr.uge.booqIn.dto.comment.CommentCreateDTO;
import fr.uge.booqIn.dto.comment.CommentDTO;
import fr.uge.booqIn.dto.comment.CommentUpdateDTO;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.interfaces.CommentServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("CommentController Tests")
class CommentControllerTest {

    private CommentServiceInterface commentService;
    private CommentController commentController;

    @BeforeEach
    void setUp() {
        commentService = Mockito.mock(CommentServiceInterface.class);
        commentController = new CommentController(commentService);
    }

    @Test
    @DisplayName("GET /comments - Retourne une page de commentaires avec tri ascendant")
    void getComments_ShouldReturnPageOfComments() {
        // Given
        int page = 0;
        int size = 10;
        String sortField = "id";
        String sortDirection = "asc";
        String content = "Test comment";
        Long userId = 1L;
        String entityType = "BOOK";
        Long entityId = 2L;

        LocalDateTime now = LocalDateTime.now();
        List<CommentDTO> commentList = List.of(
                new CommentDTO(1L, "Test comment 1", 1L, "User1", 2L, "BOOK", now, now),
                new CommentDTO(2L, "Test comment 2", 1L, "User1", 2L, "BOOK", now, now)
        );
        Page<CommentDTO> commentPage = new PageImpl<>(commentList);

        when(commentService.findComments(eq(content), eq(userId), eq(entityType), eq(entityId), any(PageRequest.class)))
                .thenReturn(commentPage);

        // When
        ResponseEntity<ApiResponse<Page<CommentDTO>>> response = commentController.getComments(
                page, size, sortField, sortDirection, content, userId, entityType, entityId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<Page<CommentDTO>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Comments found", apiResponse.message());
        assertEquals(commentPage, apiResponse.data());

        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(commentService).findComments(eq(content), eq(userId), eq(entityType), eq(entityId), pageRequestCaptor.capture());
        PageRequest capturedPageRequest = pageRequestCaptor.getValue();
        assertEquals(page, capturedPageRequest.getPageNumber());
        assertEquals(size, capturedPageRequest.getPageSize());
        assertEquals(Sort.by(sortField).ascending(), capturedPageRequest.getSort());
    }

    @Test
    @DisplayName("GET /comments - Avec tri descendant, retourne une page de commentaires")
    void getComments_WithDescendingSort_ShouldReturnPageOfComments() {
        // Given
        int page = 0;
        int size = 10;
        String sortField = "createdAt";
        String sortDirection = "desc";
        String content = null;
        Long userId = null;
        String entityType = null;
        Long entityId = null;

        LocalDateTime now = LocalDateTime.now();
        List<CommentDTO> commentList = List.of(
                new CommentDTO(2L, "Newer comment", 1L, "User1", 2L, "BOOK", now, now),
                new CommentDTO(1L, "Older comment", 1L, "User1", 2L, "BOOK", now.minusDays(1), now.minusDays(1))
        );
        Page<CommentDTO> commentPage = new PageImpl<>(commentList);

        when(commentService.findComments(eq(content), eq(userId), eq(entityType), eq(entityId), any(PageRequest.class)))
                .thenReturn(commentPage);

        // When
        ResponseEntity<ApiResponse<Page<CommentDTO>>> response = commentController.getComments(
                page, size, sortField, sortDirection, content, userId, entityType, entityId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<Page<CommentDTO>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Comments found", apiResponse.message());

        ArgumentCaptor<PageRequest> pageRequestCaptor = ArgumentCaptor.forClass(PageRequest.class);
        verify(commentService).findComments(eq(content), eq(userId), eq(entityType), eq(entityId), pageRequestCaptor.capture());
        PageRequest capturedPageRequest = pageRequestCaptor.getValue();
        assertEquals(page, capturedPageRequest.getPageNumber());
        assertEquals(size, capturedPageRequest.getPageSize());
        assertEquals(Sort.by(sortField).descending(), capturedPageRequest.getSort());
    }

    @Test
    @DisplayName("GET /comments/{id} - Retourne un commentaire lorsqu'il existe")
    void getCommentById_ShouldReturnComment() {
        // Given
        Long commentId = 1L;
        LocalDateTime now = LocalDateTime.now();
        CommentDTO commentDTO = new CommentDTO(
                commentId, "Test comment", 1L, "User1", 2L, "BOOK", now, now
        );
        when(commentService.getById(eq(commentId))).thenReturn(commentDTO);

        // When
        ResponseEntity<ApiResponse<CommentDTO>> response = commentController.getCommentById(commentId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<CommentDTO> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Comment found", apiResponse.message());
        assertEquals(commentDTO, apiResponse.data());
        verify(commentService).getById(eq(commentId));
    }

    @Test
    @DisplayName("POST /comments - Crée et retourne un nouveau commentaire")
    void createComment_ShouldReturnCreatedComment() {
        // Given
        CommentCreateDTO createDTO = new CommentCreateDTO(1L, 2L, "BOOK", "Test comment content", null);
        LocalDateTime now = LocalDateTime.now();
        CommentDTO createdComment = new CommentDTO(
                1L, "Test comment content", 1L, "User1", 2L, "BOOK", now, now
        );
        when(commentService.create(any(CommentCreateDTO.class))).thenReturn(createdComment);

        // When
        ResponseEntity<ApiResponse<CommentDTO>> response = commentController.createComment(createDTO);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ApiResponse<CommentDTO> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Comment created successfully", apiResponse.message());
        assertEquals(createdComment, apiResponse.data());
        verify(commentService).create(eq(createDTO));
    }

    @Test
    @DisplayName("PUT /comments/{id} - Met à jour et retourne le commentaire modifié")
    void updateComment_ShouldReturnUpdatedComment() {
        // Given
        Long commentId = 1L;
        Long userId = 1L;
        CommentUpdateDTO updateDTO = new CommentUpdateDTO("Updated comment content");
        LocalDateTime now = LocalDateTime.now();
        CommentDTO updatedComment = new CommentDTO(
                commentId, "Updated comment content", userId, "User1", 2L, "BOOK", now, now
        );
        when(commentService.update(eq(commentId), eq(userId), any(CommentUpdateDTO.class))).thenReturn(updatedComment);

        // When
        ResponseEntity<ApiResponse<CommentDTO>> response = commentController.updateComment(commentId, userId, updateDTO);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<CommentDTO> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Comment updated successfully", apiResponse.message());
        assertEquals(updatedComment, apiResponse.data());
        verify(commentService).update(eq(commentId), eq(userId), eq(updateDTO));
    }

    @Test
    @DisplayName("DELETE /comments/{id} - Supprime le commentaire et retourne un succès")
    void deleteComment_ShouldReturnSuccessResponse() {
        // Given
        Long commentId = 1L;
        Long userId = 1L;
        doNothing().when(commentService).delete(eq(commentId), eq(userId));

        // When
        ResponseEntity<ApiResponse<Void>> response = commentController.deleteComment(commentId, userId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<Void> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Comment deleted successfully", apiResponse.message());
        assertNull(apiResponse.data());
        verify(commentService).delete(eq(commentId), eq(userId));
    }

    @Test
    @DisplayName("GET /comments/{id}/replies - Retourne les réponses à un commentaire")
    void getReplies_ShouldReturnRepliesList() {
        // Given
        Long commentId = 1L;
        LocalDateTime now = LocalDateTime.now();
        List<CommentDTO> replies = List.of(
                new CommentDTO(2L, "Reply 1", 1L, "User1", 1L, "COMMENT", now, now),
                new CommentDTO(3L, "Reply 2", 2L, "User2", 1L, "COMMENT", now, now)
        );
        when(commentService.getRepliesByCommentId(eq(commentId))).thenReturn(replies);

        // When
        ResponseEntity<ApiResponse<List<CommentDTO>>> response = commentController.getReplies(commentId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<List<CommentDTO>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Replies found", apiResponse.message());
        assertEquals(replies, apiResponse.data());
        assertEquals(2, apiResponse.data().size());
        verify(commentService).getRepliesByCommentId(eq(commentId));
    }

    @Test
    @DisplayName("GET /comments/entity/{entityType}/{entityId} - Retourne les commentaires pour une entité")
    void getCommentsByEntity_ShouldReturnCommentsList() {
        // Given
        String entityType = "BOOK";
        Long entityId = 2L;
        LocalDateTime now = LocalDateTime.now();
        List<CommentDTO> comments = List.of(
                new CommentDTO(1L, "Comment 1", 1L, "User1", entityId, entityType, now, now),
                new CommentDTO(2L, "Comment 2", 2L, "User2", entityId, entityType, now, now)
        );
        when(commentService.getByEntity(eq(entityId), eq(entityType))).thenReturn(comments);

        // When
        ResponseEntity<ApiResponse<List<CommentDTO>>> response = commentController.getCommentsByEntity(entityType, entityId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<List<CommentDTO>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Comments found", apiResponse.message());
        assertEquals(comments, apiResponse.data());
        assertEquals(2, apiResponse.data().size());
        verify(commentService).getByEntity(eq(entityId), eq(entityType));
    }

    @Test
    @DisplayName("GET /comments/entity/{entityType}/{entityId}/tree - Retourne l'arbre des commentaires pour une entité")
    void getCommentTreeForEntity_ShouldReturnCommentTree() {
        // Given
        String entityType = "BOOK";
        Long entityId = 2L;
        LocalDateTime now = LocalDateTime.now();
        
        List<CommentDTO> replies = new ArrayList<>();
        replies.add(new CommentDTO(3L, "Reply 1", 2L, "User2", 1L, "COMMENT", now, now));
        
        CommentDTO parentComment = new CommentDTO(1L, "Parent comment", 1L, "User1", entityId, entityType, now, now, replies);
        CommentDTO secondComment = new CommentDTO(2L, "Second comment", 3L, "User3", entityId, entityType, now, now);
        
        List<CommentDTO> commentTree = List.of(parentComment, secondComment);
        
        when(commentService.getCommentTreeByEntity(eq(entityId), eq(entityType))).thenReturn(commentTree);

        // When
        ResponseEntity<ApiResponse<List<CommentDTO>>> response = commentController.getCommentTreeForEntity(entityType, entityId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<List<CommentDTO>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Comment tree found", apiResponse.message());
        assertEquals(commentTree, apiResponse.data());
        assertEquals(2, apiResponse.data().size());
        assertEquals(1, apiResponse.data().get(0).replies().size());
        verify(commentService).getCommentTreeByEntity(eq(entityId), eq(entityType));
    }

    @Test
    @DisplayName("GET /comments/user/{userId} - Retourne les commentaires d'un utilisateur")
    void getCommentsByUser_ShouldReturnUserComments() {
        // Given
        Long userId = 1L;
        LocalDateTime now = LocalDateTime.now();
        List<CommentDTO> userComments = List.of(
                new CommentDTO(1L, "Comment on book", userId, "User1", 2L, "BOOK", now, now),
                new CommentDTO(2L, "Comment on collection", userId, "User1", 3L, "COLLECTION", now, now)
        );
        when(commentService.getByUser(eq(userId))).thenReturn(userComments);

        // When
        ResponseEntity<ApiResponse<List<CommentDTO>>> response = commentController.getCommentsByUser(userId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<List<CommentDTO>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("User comments found", apiResponse.message());
        assertEquals(userComments, apiResponse.data());
        assertEquals(2, apiResponse.data().size());
        verify(commentService).getByUser(eq(userId));
    }

    @Test
    @DisplayName("GET /comments/entity/{entityType}/{entityId}/count - Retourne le nombre de commentaires pour une entité")
    void countCommentsByEntity_ShouldReturnCommentCount() {
        // Given
        String entityType = "BOOK";
        Long entityId = 2L;
        Long commentCount = 5L;
        when(commentService.countByEntity(eq(entityId), eq(entityType))).thenReturn(commentCount);

        // When
        ResponseEntity<ApiResponse<Long>> response = commentController.countCommentsByEntity(entityType, entityId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<Long> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Comment count", apiResponse.message());
        assertEquals(commentCount, apiResponse.data());
        verify(commentService).countByEntity(eq(entityId), eq(entityType));
    }
}