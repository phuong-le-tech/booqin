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
    int countByUserIdAndReadFalse(Long userId);

    /**
     * Find notifications by containing text in message with pagination
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = ?1 AND LOWER(n.message) LIKE LOWER(CONCAT('%', ?2, '%')) ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdAndMessageContainingIgnoreCase(Long userId, String text, Pageable pageable);

    /**
     * Delete old read notifications for cleanup
     */
    void deleteByReadTrueAndCreatedAtBefore(LocalDateTime before);
}