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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.id = :id")
    Optional<Comment> findWithUserById(@Param("id") Long id);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.entityId = :entityId AND c.entityType = :entityType AND c.parentComment IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findRootCommentsByEntity(
            @Param("entityId") Long entityId,
            @Param("entityType") String entityType);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.parentComment.id = :parentId ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentCommentId(@Param("parentId") Long parentId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.entityId = :entityId AND c.entityType = :entityType")
    Long countByEntity(@Param("entityId") Long entityId, @Param("entityType") String entityType);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.user = :user ORDER BY c.createdAt DESC")
    List<Comment> findByUserOrderByCreatedAtDesc(@Param("user") User user);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.entityType = :entityType ORDER BY c.createdAt DESC")
    List<Comment> findRecentCommentsByType(@Param("entityType") String entityType, Pageable pageable);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE LOWER(c.content) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY c.createdAt DESC")
    List<Comment> searchByContent(@Param("searchTerm") String searchTerm);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.entityId = :entityId AND c.entityType = :entityType AND c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt ASC")
    List<Comment> findByEntityAndDateRange(
            @Param("entityId") Long entityId,
            @Param("entityType") String entityType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT DISTINCT c FROM Comment c LEFT JOIN FETCH c.replies r LEFT JOIN FETCH r.user WHERE c.entityId = :entityId AND c.entityType = :entityType AND c.parentComment IS NULL ORDER BY c.createdAt DESC")
    List<Comment> findRootCommentsWithRepliesByEntity(
            @Param("entityId") Long entityId,
            @Param("entityType") String entityType);

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.user = :user AND c.entityId = :entityId AND c.entityType = :entityType AND ((:parentId IS NULL AND c.parentComment IS NULL) OR (:parentId IS NOT NULL AND c.parentComment.id = :parentId))")
    List<Comment> findByUserAndEntityAndParent(
            @Param("user") User user,
            @Param("entityId") Long entityId,
            @Param("entityType") String entityType,
            @Param("parentId") Long parentId);

    @Override
    @Query("SELECT DISTINCT c FROM Comment c JOIN FETCH c.user WHERE (:spec is null or :spec = true)")
    Page<Comment> findAll(@Nullable Specification<Comment> spec, @NonNull Pageable pageable);
}