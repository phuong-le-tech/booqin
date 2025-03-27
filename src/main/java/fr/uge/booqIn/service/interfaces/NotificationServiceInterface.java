package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

public interface NotificationServiceInterface {

    /**
     * Gets all notifications for a user
     * @param userId ID of the user
     * @param pageable Pagination information
     * @return DTO containing notifications and counts
     */
    Page<Notification> getNotifications(Long userId, Pageable pageable);

    /**
     * Gets count of unread notifications for a user
     * @param userId ID of the user
     * @return Map containing counts by type, with key "unread" for total unread count
     */
    int getUnreadCounts(Long userId);

    /**
     * Marks a notification as read
     * @param notificationId ID of the notification to mark
     * @param userId ID of the user who owns the notification
     * @return true if notification was marked, false otherwise
     */
    boolean markAsRead(Long notificationId, Long userId);

    /**
     * Marks all notifications as read for a user
     * @param userId ID of the user
     * @return number of notifications marked as read
     */
    int markAllAsRead(Long userId);

    /**
     * Deletes a notification
     * @param notificationId ID of the notification
     * @param userId ID of the user
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteNotification(Long notificationId, Long userId);

    /**
     * Deletes all notifications for a user
     * @param userId ID of the user
     * @return number of notifications deleted
     */
    int clearAllNotifications(Long userId);
}