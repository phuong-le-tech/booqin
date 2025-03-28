package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.dto.comment.CommentCreateDTO;
import fr.uge.booqIn.dto.comment.CommentDTO;
import fr.uge.booqIn.dto.comment.CommentUpdateDTO;
import fr.uge.booqIn.entity.Comment;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Service interface for managing comments in the BooqIn application.
 * Provides methods to create, retrieve, update, and delete comments.
 */
@Validated
public interface CommentServiceInterface {

    /**
     * Finds comments matching the specified criteria.
     *
     * @param content Content to search for (optional)
     * @param userId User ID to filter by (optional)
     * @param entityType Entity type to filter by (optional)
     * @param entityId Entity ID to filter by (optional)
     * @param pageable Pagination information (required)
     * @return Page of comments matching the criteria
     */
    Page<CommentDTO> findComments(String content, Long userId, String entityType, Long entityId, @NonNull Pageable pageable);

    /**
     * Retrieves a comment by its ID.
     *
     * @param commentId The ID of the comment to retrieve (required)
     * @return The comment DTO
     * @throws fr.uge.booqIn.exception.ResourceNotFoundException if the comment is not found
     */
    CommentDTO getById(@NonNull Long commentId);

    /**
     * Creates a new comment.
     *
     * @param createDTO DTO containing comment creation details (required)
     * @return The created comment DTO
     * @throws fr.uge.booqIn.exception.ResourceNotFoundException if referenced entities are not found
     */
    CommentDTO create(@NonNull @Valid CommentCreateDTO createDTO);

    /**
     * Updates an existing comment.
     *
     * @param commentId The ID of the comment to update (required)
     * @param userId The ID of the user performing the update (required)
     * @param updateDTO DTO containing comment update details (required)
     * @return The updated comment DTO
     * @throws fr.uge.booqIn.exception.ResourceNotFoundException if the comment is not found
     */
    CommentDTO update(@NonNull Long commentId, @NonNull Long userId, @NonNull @Valid CommentUpdateDTO updateDTO);

    /**
     * Deletes a comment.
     *
     * @param commentId The ID of the comment to delete (required)
     * @param userId The ID of the user performing the deletion (required)
     * @throws fr.uge.booqIn.exception.ResourceNotFoundException if the comment is not found
     */
    void delete(@NonNull Long commentId, @NonNull Long userId);

    /**
     * Creates a new comment from the entity.
     *
     * @param comment The comment entity to create (required)
     * @return The created comment entity
     * @throws fr.uge.booqIn.exception.ResourceNotFoundException if referenced entities are not found
     */
    Comment createComment(@NonNull @Valid Comment comment);

    /**
     * Updates an existing comment entity.
     *
     * @param comment The comment entity with updated values (required)
     * @return The updated comment entity
     * @throws fr.uge.booqIn.exception.ResourceNotFoundException if the comment is not found
     */
    Comment updateComment(@NonNull @Valid Comment comment);

    /**
     * Retrieves all root comments for an entity.
     *
     * @param entityId The entity ID (required)
     * @param entityType The entity type (required)
     * @return List of comment DTOs
     * @throws fr.uge.booqIn.exception.ResourceNotFoundException if the entity is not found
     */
    List<CommentDTO> getByEntity(@NonNull Long entityId, @NonNull String entityType);

    /**
     * Retrieves all replies to a comment.
     *
     * @param commentId The parent comment ID (required)
     * @return List of comment DTOs
     * @throws fr.uge.booqIn.exception.ResourceNotFoundException if the comment is not found
     */
    List<CommentDTO> getRepliesByCommentId(@NonNull Long commentId);

    /**
     * Retrieves all comments by a user.
     *
     * @param userId The user ID (required)
     * @return List of comment DTOs
     * @throws fr.uge.booqIn.exception.ResourceNotFoundException if the user is not found
     */
    List<CommentDTO> getByUser(@NonNull Long userId);

    /**
     * Counts the number of comments for an entity.
     *
     * @param entityId The entity ID (required)
     * @param entityType The entity type (required)
     * @return Number of comments
     * @throws fr.uge.booqIn.exception.ResourceNotFoundException if the entity is not found
     */
    Long countByEntity(@NonNull Long entityId, @NonNull String entityType);

    /**
     * Retrieves the comment tree for an entity.
     *
     * @param entityId The entity ID (required)
     * @param entityType The entity type (required)
     * @return List of comment DTOs representing the tree structure
     * @throws fr.uge.booqIn.exception.ResourceNotFoundException if the entity is not found
     */
    List<CommentDTO> getCommentTreeByEntity(@NonNull Long entityId, @NonNull String entityType);

    /**
     * Finds all comments.
     *
     * @return List of all comments
     */
    List<Comment> findAll();
}