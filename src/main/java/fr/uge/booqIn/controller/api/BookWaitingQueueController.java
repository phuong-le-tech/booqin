package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.dto.notification.BookNotificationDTO;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.interfaces.BookWaitingQueueServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/book-queue")
public class BookWaitingQueueController {

  private final BookWaitingQueueServiceInterface notificationService;

  @Autowired
  public BookWaitingQueueController(BookWaitingQueueServiceInterface notificationService) {
    this.notificationService = notificationService;
  }

  @PostMapping("/join")
  public ResponseEntity<ApiResponse<Boolean>> subscribeToNotifications(
          @RequestParam Long bookId,
          @RequestParam Long userId) {
    boolean added = notificationService.addToWaitingQueue(bookId, userId);

    return added
            ? ResponseEntity.ok(ApiResponse.success("Added to notification queue", true))
            : ResponseEntity.badRequest().body(ApiResponse.error("Already in notification queue", false));
  }

  @GetMapping("/active")
  public ResponseEntity<ApiResponse<List<BookNotificationDTO>>> getActiveNotifications(@RequestParam Long userId) {
    List<BookNotificationDTO> notifications = notificationService.getUserActiveQueues(userId);

    return ResponseEntity.ok(ApiResponse.success("Active User in queues", notifications));
  }

  @PostMapping("/reserve")
  public ResponseEntity<ApiResponse<Boolean>> reserveNotifiedBook(
          @RequestParam Long notificationId,
          @RequestParam Long userId) {

    boolean reserved = notificationService.reserveBookFromQueue(notificationId, userId);

    return reserved
            ? ResponseEntity.ok(ApiResponse.success("Book reserved", true))
            : ResponseEntity.badRequest().body(ApiResponse.error("impossible to reserve book", false));
  }

  @PostMapping("/cancel")
  public ResponseEntity<ApiResponse<Boolean>> cancelBookNotification(
          @RequestParam Long notificationId,
          @RequestParam Long userId) {

    boolean canceled = notificationService.cancelQueueEntry(notificationId, userId);

    return canceled
            ? ResponseEntity.ok(ApiResponse.success("Notification cancelled successfully", true))
            : ResponseEntity.badRequest().body(ApiResponse.error("Unable to cancel notification", false));
  }

  @GetMapping("/check")
  public ResponseEntity<ApiResponse<Integer>> checkQueuePosition(
          @RequestParam Long bookId,
          @RequestParam Long userId) {

    int position = notificationService.getUserQueuePosition(bookId, userId);

    return ResponseEntity.ok(ApiResponse.success(
            position > 0 ? "User is in queue" : "User is not in queue",
            position));
  }
}