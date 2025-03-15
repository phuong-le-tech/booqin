package fr.uge.booqIn.specification;

import fr.uge.booqIn.entity.Comment;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

/**
 * Specifications for Comment entity to build dynamic queries.
 */
public class CommentSpecification {

    public static Specification<Comment> hasContent(String content) {
        return (root, query, criteriaBuilder) -> {
            // Utiliser le binding de paramètre pour LIKE au lieu de concaténation de chaînes
            String searchTerm = "%" + content.toLowerCase() + "%";
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("content")),
                    searchTerm
            );
        };
    }

    public static Specification<Comment> byUser(Long userId) {
        return (root, query, criteriaBuilder) -> {
            // Join user to avoid N+1 queries
            root.fetch("user", JoinType.INNER);
            // Distinct is needed when using fetch joins
            query.distinct(true);
            return criteriaBuilder.equal(root.get("user").get("id"), userId);
        };
    }

    public static Specification<Comment> forEntityType(String entityType) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("entityType"), entityType);
    }

    public static Specification<Comment> forEntityId(Long entityId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("entityId"), entityId);
    }

    public static Specification<Comment> createdBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.between(root.get("createdAt"), start, end);
    }

    public static Specification<Comment> isRootComment() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isNull(root.get("parentComment"));
    }

    public static Specification<Comment> hasParent(Long parentId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("parentComment").get("id"), parentId);
    }
}