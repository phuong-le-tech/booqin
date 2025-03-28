package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

/**
 * Service interface for managing user notifications in the BooqIn application.
 * Provides methods to retrieve, mark, and manage notifications.
 */
@Validated
public interface NotificationServiceInterface {

    /**
     * Gets all notifications for a user
     *
     * @param userId ID of the user (required)
     * @param pageable Pagination information (required)
     * @return Page of notifications for the user
     * @throws IllegalArgumentException if any required parameter is null
     */
    Page<Notification> getNotifications(@NonNull Long userId, @NonNull Pageable pageable);

    /**
     * Gets count of unread notifications for a user
     *
     * @param userId ID of the user (required)
     * @return Count of unread notifications
     * @throws IllegalArgumentException if userId is null
     */
    int getUnreadCounts(@NonNull Long userId);

    /**
     * Marks a notification as read
     *
     * @param notificationId ID of the notification to mark (required)
     * @param userId ID of the user who owns the notification (required)
     * @return true if notification was marked as read, false if notification doesn't exist or doesn't belong to the user
     * @throws IllegalArgumentException if any required parameter is null
     */
    boolean markAsRead(@NonNull Long notificationId, @NonNull Long userId);

    /**
     * Marks all notifications as read for a user
     *
     * @param userId ID of the user (required)
     * @return number of notifications marked as read
     * @throws IllegalArgumentException if userId is null
     */
    int markAllAsRead(@NonNull Long userId);

    /**
     * Deletes a notification
     *
     * @param notificationId ID of the notification (required)
     * @param userId ID of the user (required)
     * @return true if deleted successfully, false if notification doesn't exist or doesn't belong to the user
     * @throws IllegalArgumentException if any required parameter is null
     */
    boolean deleteNotification(@NonNull Long notificationId, @NonNull Long userId);

    /**
     * Deletes all notifications for a user
     *
     * @param userId ID of the user (required)
     * @return number of notifications deleted
     * @throws IllegalArgumentException if userId is null
     */
    int clearAllNotifications(@NonNull Long userId);
}