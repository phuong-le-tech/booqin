package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.dto.comment.CommentCreateDTO;
import fr.uge.booqIn.dto.comment.CommentDTO;
import fr.uge.booqIn.dto.comment.CommentUpdateDTO;
import fr.uge.booqIn.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface CommentServiceInterface {

    /**
     * Finds comments matching the specified criteria.
     *
     * @param content Content to search for
     * @param userId User ID to filter by
     * @param entityType Entity type to filter by
     * @param entityId Entity ID to filter by
     * @param pageable Pagination information
     * @return Page of comments matching the criteria
     */
    Page<CommentDTO> findComments(String content, Long userId, String entityType, Long entityId, Pageable pageable);

    /**
     * Retrieves a comment by its ID.
     *
     * @param commentId The ID of the comment to retrieve
     * @return The comment DTO
     */
    CommentDTO getById(@NonNull Long commentId);

    /**
     * Creates a new comment.
     *
     * @param createDTO DTO containing comment creation details
     * @return The created comment DTO
     */
    CommentDTO create(@NonNull CommentCreateDTO createDTO);

    /**
     * Updates an existing comment.
     *
     * @param commentId The ID of the comment to update
     * @param userId The ID of the user performing the update
     * @param updateDTO DTO containing comment update details
     * @return The updated comment DTO
     */
    CommentDTO update(@NonNull Long commentId, @NonNull Long userId, @NonNull CommentUpdateDTO updateDTO);

    /**
     * Deletes a comment.
     *
     * @param commentId The ID of the comment to delete
     * @param userId The ID of the user performing the deletion
     */
    void delete(@NonNull Long commentId, @NonNull Long userId);

    /**
     * Creates a new comment from the entity.
     *
     * @param comment The comment entity to create
     * @return The created comment entity
     */
    Comment createComment(@NonNull Comment comment);

    /**
     * Updates an existing comment entity.
     *
     * @param comment The comment entity with updated values
     * @return The updated comment entity
     */
    Comment updateComment(@NonNull Comment comment);

    /**
     * Retrieves all root comments for an entity.
     *
     * @param entityId The entity ID
     * @param entityType The entity type
     * @return List of comment DTOs
     */
    List<CommentDTO> getByEntity(@NonNull Long entityId, @NonNull String entityType);

    /**
     * Retrieves all replies to a comment.
     *
     * @param commentId The parent comment ID
     * @return List of comment DTOs
     */
    List<CommentDTO> getRepliesByCommentId(@NonNull Long commentId);

    /**
     * Retrieves all comments by a user.
     *
     * @param userId The user ID
     * @return List of comment DTOs
     */
    List<CommentDTO> getByUser(@NonNull Long userId);

    /**
     * Counts the number of comments for an entity.
     *
     * @param entityId The entity ID
     * @param entityType The entity type
     * @return Number of comments
     */
    Long countByEntity(@NonNull Long entityId, @NonNull String entityType);

    /**
     * Retrieves the comment tree for an entity.
     *
     * @param entityId The entity ID
     * @param entityType The entity type
     * @return List of comment DTOs representing the tree structure
     */
    List<CommentDTO> getCommentTreeByEntity(@NonNull Long entityId, @NonNull String entityType);

    /**
     * Finds all comments.
     *
     * @return List of all comments
     */
    List<Comment> findAll();
}