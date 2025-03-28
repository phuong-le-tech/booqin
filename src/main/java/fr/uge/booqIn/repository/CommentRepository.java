package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.Comment;
import fr.uge.booqIn.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {

  /**
   * Finds a comment by its ID with its associated user eagerly loaded.
   *
   * @param id The unique identifier of the comment
   * @return An optional containing the comment with its user
   */
  @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.id = :id")
  Optional<Comment> findWithUserById(@NonNull Long id);

  /**
   * Finds root-level comments for a specific entity, ordered by creation time.
   *
   * @param entityId   The unique identifier of the entity
   * @param entityType The type of the entity
   * @return A list of root comments for the entity
   */
  @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.entityId = :entityId AND c.entityType = :entityType AND c.parentComment IS NULL ORDER BY c.createdAt DESC")
  List<Comment> findRootCommentsByEntity(
          @NonNull Long entityId,
          @NonNull String entityType);

  /**
   * Finds replies to a specific parent comment, ordered by creation time.
   *
   * @param parentId The unique identifier of the parent comment
   * @return A list of reply comments
   */
  @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.parentComment.id = :parentId ORDER BY c.createdAt ASC")
  List<Comment> findRepliesByParentCommentId(@NonNull Long parentId);

  /**
   * Counts the number of comments for a specific entity.
   *
   * @param entityId   The unique identifier of the entity
   * @param entityType The type of the entity
   * @return The total number of comments for the entity
   */
  @Query("SELECT COUNT(c) FROM Comment c WHERE c.entityId = :entityId AND c.entityType = :entityType")
  Long countByEntity(
          @NonNull Long entityId,
          @NonNull String entityType);

  /**
   * Finds comments by a specific user, ordered by creation time.
   *
   * @param user The user who created the comments
   * @return A list of comments by the user
   */
  @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.user = :user ORDER BY c.createdAt DESC")
  List<Comment> findByUserOrderByCreatedAtDesc(@NonNull User user);

  /**
   * Finds root comments with their replies for a specific entity.
   *
   * @param entityId   The unique identifier of the entity
   * @param entityType The type of the entity
   * @return A list of root comments with their replies
   */
  @Query("SELECT DISTINCT c FROM Comment c LEFT JOIN FETCH c.replies r LEFT JOIN FETCH r.user WHERE c.entityId = :entityId AND c.entityType = :entityType AND c.parentComment IS NULL ORDER BY c.createdAt DESC")
  List<Comment> findRootCommentsWithRepliesByEntity(
          @NonNull Long entityId,
          @NonNull String entityType);

  /**
   * Finds all comments with optional specification and pagination.
   *
   * @param spec     Optional specification for filtering comments
   * @param pageable Pagination information
   * @return A page of comments
   */
  @Override
  @Query("SELECT DISTINCT c FROM Comment c JOIN FETCH c.user WHERE (:spec is null or :spec = true)")
  @NonNull
  Page<Comment> findAll(
          @Nullable Specification<Comment> spec,
          @NonNull Pageable pageable
  );
}