package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.entity.Notification;
import fr.uge.booqIn.repository.NotificationRepository;
import fr.uge.booqIn.service.interfaces.NotificationServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationServiceInterface {
    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(@NonNull NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Page<Notification> getNotifications(@NonNull Long userId, @NonNull Pageable pageable) {
        logger.debug("Getting notifications for user ID: {}", userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public int getUnreadCounts(@NonNull Long userId) {
        logger.debug("Counting unread notifications for user ID: {}", userId);
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    @Retryable(
            maxAttemptsExpression = "${spring.retry.max-attempts}",
            retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
            backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
    )
    public boolean markAsRead(@NonNull Long notificationId, @NonNull Long userId) {
        logger.debug("Marking notification {} as read for user {}", notificationId, userId);

        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);

        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();

            if (notification.getUserId().equals(userId)) {
                notification.setRead(true);
                notificationRepository.save(notification);
                return true;
            } else {
                logger.warn("Unauthorized attempt to mark notification {} as read by user {}", notificationId, userId);
            }
        } else {
            logger.warn("Notification {} not found", notificationId);
        }

        return false;
    }

    @Override
    @Transactional
    @Retryable(
            maxAttemptsExpression = "${spring.retry.max-attempts}",
            retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
            backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
    )
    public int markAllAsRead(@NonNull Long userId) {
        logger.debug("Marking all notifications as read for user {}", userId);

        List<Notification> unreadNotifications =
                notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);

        if (unreadNotifications.isEmpty()) {
            logger.debug("No unread notifications found for user {}", userId);
            return 0;
        }

        // Mark all as read
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);

        logger.debug("Marked {} notifications as read for user {}", unreadNotifications.size(), userId);
        return unreadNotifications.size();
    }

    @Override
    @Transactional
    @Retryable(
            maxAttemptsExpression = "${spring.retry.max-attempts}",
            retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
            backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
    )
    public boolean deleteNotification(@NonNull Long notificationId, @NonNull Long userId) {
        logger.debug("Deleting notification {} for user {}", notificationId, userId);

        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);

        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();

            // Verify the notification belongs to the user
            if (notification.getUserId().equals(userId)) {
                notificationRepository.delete(notification);
                logger.debug("Notification {} deleted successfully", notificationId);
                return true;
            } else {
                logger.warn("Unauthorized attempt to delete notification {} by user {}", notificationId, userId);
            }
        } else {
            logger.warn("Notification {} not found", notificationId);
        }

        return false;
    }

    @Transactional
    @Retryable(
            maxAttemptsExpression = "${spring.retry.max-attempts}",
            retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
            backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
    )
    public void deleteAllForUser(@NonNull Long userId) {
        logger.debug("Deleting all notifications for user {}", userId);

        List<Notification> userNotifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        notificationRepository.deleteAll(userNotifications);

        logger.debug("Deleted {} notifications for user {}", userNotifications.size(), userId);
    }

    @Override
    @Transactional
    @Retryable(
            maxAttemptsExpression = "${spring.retry.max-attempts}",
            retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
            backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
    )
    public int clearAllNotifications(@NonNull Long userId) {
        logger.debug("Clearing all notifications for user {}", userId);

        List<Notification> userNotifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        if (userNotifications.isEmpty()) {
            logger.debug("No notifications found for user {}", userId);
            return 0;
        }

        int count = userNotifications.size();
        notificationRepository.deleteAll(userNotifications);

        logger.debug("Cleared {} notifications for user {}", count, userId);
        return count;
    }

    @Recover
    public boolean recoverFromOptimisticLockingFailureOnMarkAsRead(
            ObjectOptimisticLockingFailureException ex,
            Long notificationId,
            Long userId) {
        logger.error("Optimistic locking failure while marking notification {} as read: {}", notificationId, ex.getMessage());
        throw new ConcurrentModificationException("The notification has been modified by someone else. Please try again.", ex);
    }

    @Recover
    public int recoverFromOptimisticLockingFailureOnMarkAllAsRead(
            ObjectOptimisticLockingFailureException ex,
            Long userId) {
        logger.error("Optimistic locking failure while marking all notifications as read for user {}: {}", userId, ex.getMessage());
        throw new ConcurrentModificationException("The notifications have been modified by someone else. Please try again.", ex);
    }

    @Recover
    public boolean recoverFromOptimisticLockingFailureOnDeleteNotification(
            ObjectOptimisticLockingFailureException ex,
            Long notificationId,
            Long userId) {
        logger.error("Optimistic locking failure while deleting notification {}: {}", notificationId, ex.getMessage());
        throw new ConcurrentModificationException("The notification has been modified by someone else. Please try again.", ex);
    }

    @Recover
    public void recoverFromOptimisticLockingFailureOnDeleteAllForUser(
            ObjectOptimisticLockingFailureException ex,
            Long userId) {
        logger.error("Optimistic locking failure while deleting all notifications for user {}: {}", userId, ex.getMessage());
        throw new ConcurrentModificationException("The notifications have been modified by someone else. Please try again.", ex);
    }

    @Recover
    public int recoverFromOptimisticLockingFailureOnClearAllNotifications(
            ObjectOptimisticLockingFailureException ex,
            Long userId) {
        logger.error("Optimistic locking failure while clearing all notifications for user {}: {}", userId, ex.getMessage());
        throw new ConcurrentModificationException("The notifications have been modified by someone else. Please try again.", ex);
    }
}