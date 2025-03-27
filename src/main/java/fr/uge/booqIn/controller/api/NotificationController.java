package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.entity.Notification;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.interfaces.NotificationServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationServiceInterface notificationService;

    @Autowired
    public NotificationController(NotificationServiceInterface notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<Page<Notification>>> getNotifications(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getNotifications(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success("Notifications retrieved successfully", notifications));
    }

    @GetMapping("/counts")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> getUnreadCounts(
            @AuthenticationPrincipal User currentUser) {

        int unreadCount = notificationService.getUnreadCounts(currentUser.getId());
        Map<String, Integer> counts = new HashMap<>();
        counts.put("unread", unreadCount);
        return ResponseEntity.ok(ApiResponse.success("Unread counts retrieved successfully", counts));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Boolean>> markAsRead(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {

        boolean success = notificationService.markAsRead(id, currentUser.getId());
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("Notification marked as read", true));
        } else {
            return ResponseEntity.ok(ApiResponse.error("Failed to mark notification as read", false));
        }
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Integer>> markAllAsRead(
            @AuthenticationPrincipal User currentUser) {

        int count = notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(count + " notifications marked as read", count));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteNotification(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {

        boolean success = notificationService.deleteNotification(id, currentUser.getId());
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("Notification deleted successfully", true));
        } else {
            return ResponseEntity.ok(ApiResponse.error("Failed to delete notification", false));
        }
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<ApiResponse<Integer>> clearAllNotifications(
            @AuthenticationPrincipal User currentUser) {

        int count = notificationService.clearAllNotifications(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(count + " notifications cleared", count));
    }
}