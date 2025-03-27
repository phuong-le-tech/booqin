package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a user
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find notifications with pagination
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find unread notifications for a user
     */
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);

    /**
     * Count unread notifications for a user
     */
    long countByUserIdAndReadFalse(Long userId);

    /**
     * Find notifications by containing text in message (for filtering)
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = ?1 AND LOWER(n.message) LIKE LOWER(CONCAT('%', ?2, '%')) ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndMessageContainingIgnoreCase(Long userId, String text);

    /**
     * Find notifications by containing text in message with pagination
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = ?1 AND LOWER(n.message) LIKE LOWER(CONCAT('%', ?2, '%')) ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdAndMessageContainingIgnoreCase(Long userId, String text, Pageable pageable);

    /**
     * Find notifications created after a specific date
     */
    List<Notification> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(Long userId, LocalDateTime after);

    /**
     * Delete old read notifications for cleanup
     */
    void deleteByReadTrueAndCreatedAtBefore(LocalDateTime before);

    /**
     * Filter notifications by type prefix (based on message format)
     * This allows for type-like filtering without a dedicated type field
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = ?1 AND n.message LIKE CONCAT(?2, '%') ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndMessageStartsWithOrderByCreatedAtDesc(Long userId, String prefix);

    /**
     * Count notifications by type prefix
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = ?1 AND n.read = false AND n.message LIKE CONCAT(?2, '%')")
    long countByUserIdAndReadFalseAndMessageStartsWith(Long userId, String prefix);
}