package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.dto.comment.CommentCreateDTO;
import fr.uge.booqIn.dto.comment.CommentDTO;
import fr.uge.booqIn.dto.comment.CommentUpdateDTO;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.interfaces.CommentServiceInterface;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);
    private final CommentServiceInterface commentService;

    public CommentController(CommentServiceInterface commentService) {
        this.commentService = commentService;
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<Page<CommentDTO>>> getComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) Long entityId) {

        Sort sort = sortDirection.equalsIgnoreCase("desc") ?
                Sort.by(sortField).descending() :
                Sort.by(sortField).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CommentDTO> comments = commentService.findComments(content, userId, entityType, entityId, pageable);

        return ResponseEntity.ok(ApiResponse.success("Comments found", comments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CommentDTO>> getCommentById(@PathVariable Long id) {
        CommentDTO comment = commentService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Comment found", comment));
    }

    @PostMapping("")
    public ResponseEntity<ApiResponse<CommentDTO>> createComment(@Valid @RequestBody CommentCreateDTO createDTO) {
        CommentDTO createdComment = commentService.create(createDTO);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comment created successfully", createdComment));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CommentDTO>> updateComment(
            @PathVariable Long id,
            @RequestHeader("User-Id") Long userId,
            @Valid @RequestBody CommentUpdateDTO updateDTO) {
        CommentDTO updatedComment = commentService.update(id, userId, updateDTO);
        return ResponseEntity.ok(
                ApiResponse.success("Comment updated successfully", updatedComment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Long id,
            @RequestHeader("User-Id") Long userId) {
        commentService.delete(id, userId);
        return ResponseEntity.ok(
                ApiResponse.success("Comment deleted successfully"));
    }

    @GetMapping("/{id}/replies")
    public ResponseEntity<ApiResponse<List<CommentDTO>>> getReplies(@PathVariable Long id) {
        List<CommentDTO> replies = commentService.getRepliesByCommentId(id);
        return ResponseEntity.ok(ApiResponse.success("Replies found", replies));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<List<CommentDTO>>> getCommentsByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        List<CommentDTO> comments = commentService.getByEntity(entityId, entityType);
        return ResponseEntity.ok(ApiResponse.success("Comments found", comments));
    }

    @GetMapping("/entity/{entityType}/{entityId}/tree")
    public ResponseEntity<ApiResponse<List<CommentDTO>>> getCommentTreeForEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        List<CommentDTO> commentTree = commentService.getCommentTreeByEntity(entityId, entityType);
        return ResponseEntity.ok(ApiResponse.success("Comment tree found", commentTree));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<CommentDTO>>> getCommentsByUser(@PathVariable Long userId) {
        List<CommentDTO> comments = commentService.getByUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User comments found", comments));
    }

    @GetMapping("/entity/{entityType}/{entityId}/count")
    public ResponseEntity<ApiResponse<Long>> countCommentsByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        Long count = commentService.countByEntity(entityId, entityType);
        return ResponseEntity.ok(ApiResponse.success("Comment count", count));
    }
}