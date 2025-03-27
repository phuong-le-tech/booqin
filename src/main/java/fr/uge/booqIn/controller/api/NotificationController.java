package fr.uge.booqIn.controller.api;
import fr.uge.booqIn.dto.notification.BookNotificationDTO;
import fr.uge.booqIn.dto.notification.NotificationDto;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.interfaces.NotificationServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
  
  private final NotificationServiceInterface notificationService;
  
  @Autowired
  public NotificationController(NotificationServiceInterface notificationService) {
    this.notificationService = notificationService;
  }
  
  @PostMapping("/subscribe")
  public ResponseEntity<ApiResponse<Boolean>> subscribeToNotifications(
          @RequestParam Long bookId,
          @RequestParam Long userId) {
    
    System.out.println("Received: bookId=" + bookId + ", userId=" + userId);
    
    boolean added = notificationService.addToNotificationQueue(bookId, userId);
    
    return added
            ? ResponseEntity.ok(ApiResponse.success("Added to notification queue", true))
            : ResponseEntity.badRequest().body(ApiResponse.error("Already in notification queue", false));
  }
  
  @GetMapping("/active")
  public ResponseEntity<ApiResponse<List<BookNotificationDTO>>> getActiveNotifications(@RequestParam Long userId) {
    List<BookNotificationDTO> notifications = notificationService.getActiveNotifications(userId);
    
    return ResponseEntity.ok(ApiResponse.success("Notifications actives récupérées", notifications));
  }
  
  @PostMapping("/reserve")
  public ResponseEntity<ApiResponse<Boolean>> reserveNotifiedBook(
          @RequestParam Long notificationId,
          @RequestParam Long userId) {
    
    boolean reserved = notificationService.reserveNotifiedBook(notificationId, userId);
    
    return reserved
            ? ResponseEntity.ok(ApiResponse.success("Livre réservé avec succès", true))
            : ResponseEntity.badRequest().body(ApiResponse.error("Impossible de réserver le livre", false));
  }
}