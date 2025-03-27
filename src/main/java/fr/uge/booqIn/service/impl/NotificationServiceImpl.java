package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.entity.Notification;
import fr.uge.booqIn.repository.NotificationRepository;
import fr.uge.booqIn.service.interfaces.NotificationServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class NotificationServiceImpl implements NotificationServiceInterface {
    private final NotificationRepository notificationRepository;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Page<Notification> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public int getUnreadCounts(Long userId) {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("unread", notificationRepository.countByUserIdAndReadFalse(userId));
        return counts.get("unread");
    }

    @Override
    @Transactional
    public boolean markAsRead(Long notificationId, Long userId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);

        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();

            if (notification.getUserId().equals(userId)) {
                notification.setRead(true);
                notificationRepository.save(notification);
                return true;
            }
        }

        return false;
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        List<Notification> unreadNotifications =
                notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);

        if (unreadNotifications.isEmpty()) {
            return 0;
        }

        // Mark all as read
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);

        return unreadNotifications.size();
    }

    @Override
    @Transactional
    public boolean deleteNotification(Long notificationId, Long userId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);

        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();

            // Verify the notification belongs to the user
            if (notification.getUserId().equals(userId)) {
                notificationRepository.delete(notification);
                return true;
            }
        }

        return false;
    }

    @Transactional
    public void deleteAllForUser(Long userId) {
        List<Notification> userNotifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        notificationRepository.deleteAll(userNotifications);
    }

    @Override
    @Transactional
    public int clearAllNotifications(Long userId) {
        List<Notification> userNotifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        if (userNotifications.isEmpty()) {
            return 0;
        }

        int count = userNotifications.size();
        notificationRepository.deleteAll(userNotifications);

        return count;
    }

}