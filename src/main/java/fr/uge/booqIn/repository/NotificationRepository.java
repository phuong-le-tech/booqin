package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Finds all notifications for a specific user, ordered by creation time.
     *
     * @param userId The unique identifier of the user
     * @return A list of notifications for the user, sorted by creation time (descending)
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(@NonNull Long userId);

    /**
     * Finds notifications for a specific user with pagination.
     *
     * @param userId The unique identifier of the user
     * @param pageable Pagination information
     * @return A page of notifications for the user, sorted by creation time (descending)
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(
            @NonNull Long userId,
            @NonNull Pageable pageable
    );

    /**
     * Finds unread notifications for a specific user.
     *
     * @param userId The unique identifier of the user
     * @return A list of unread notifications, sorted by creation time (descending)
     */
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(@NonNull Long userId);

    /**
     * Counts the number of unread notifications for a specific user.
     *
     * @param userId The unique identifier of the user
     * @return The number of unread notifications
     */
    int countByUserIdAndReadFalse(@NonNull Long userId);
}