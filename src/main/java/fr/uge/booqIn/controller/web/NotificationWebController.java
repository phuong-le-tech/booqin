package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.entity.Notification;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.service.interfaces.NotificationServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Map;
import java.util.logging.Logger;

@Controller
@RequestMapping("/notifications")
public class NotificationWebController {
  private static final Logger logger = Logger.getLogger(NotificationWebController.class.getName());

  private final NotificationServiceInterface notificationService;
  private final HomeWebController homeWebController;

  @Autowired
  public NotificationWebController(NotificationServiceInterface notificationService,
                                   HomeWebController homeWebController) {
    this.notificationService = notificationService;
    this.homeWebController = homeWebController;
  }

  /**
   * Display all notifications for the current user
   */
  @GetMapping
  public String getAllNotifications(
          Model model,
          @AuthenticationPrincipal User currentUser,
          Principal principal,
          @RequestParam(value = "page", defaultValue = "0") int page,
          @RequestParam(value = "size", defaultValue = "20") int size,
          @RequestParam(value = "type", required = false) String type,
          @RequestParam(value = "sort", defaultValue = "createdAt") String sort,
          @RequestParam(value = "direction", defaultValue = "desc") String direction) {

    // Prepare common model
    homeWebController.prepareModel(model, principal, "notifications");

    if (currentUser == null) {
      return "redirect:/auth/login";
    }

    // Configure pagination and sorting
    Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ?
            Sort.Direction.DESC : Sort.Direction.ASC;
    Sort sortObj = Sort.by(sortDirection, sort);
    Pageable pageable = PageRequest.of(page, size, sortObj);

    // Get notifications based on type filter
    Page<Notification> notifications = notificationService.getNotifications(currentUser.getId(), pageable);


    // Get unread counts
    int counts = notificationService.getUnreadCounts(currentUser.getId());

    model.addAttribute("notifications", notifications);
    model.addAttribute("unreadCounts", counts);
    model.addAttribute("totalPages", notifications.getTotalPages());
    model.addAttribute("currentPage", page);
    model.addAttribute("selectedType", type);
    model.addAttribute("sort", sort);
    model.addAttribute("direction", direction);

    // Set title and content template
    model.addAttribute("title", "BooqIn - Notifications");
    model.addAttribute("content", "pages/notification/notification-list");

    return "layout";
  }

  @GetMapping("/{id}/read")
  public String markAsReadAndRedirect(
          @PathVariable Long id,
          @RequestParam(required = false) String returnUrl,
          @AuthenticationPrincipal User currentUser,
          RedirectAttributes redirectAttributes) {

    if (currentUser == null) {
      return "redirect:/auth/login";
    }

    try {
      boolean success = notificationService.markAsRead(id, currentUser.getId());

      // If returnUrl is provided, redirect back to the original page
      if (returnUrl != null && !returnUrl.isEmpty()) {
        return "redirect:" + returnUrl;
      }

      // Otherwise go to the notifications page
      return "redirect:/notifications";

    } catch (Exception e) {
      logger.warning("Error marking notification as read: " + e.getMessage());
      redirectAttributes.addFlashAttribute("error", "Failed to mark notification as read");
      return "redirect:/notifications";
    }
  }

  @PostMapping("/read-all")
  public String markAllAsRead(
          @AuthenticationPrincipal User currentUser,
          RedirectAttributes redirectAttributes) {

    if (currentUser == null) {
      return "redirect:/auth/login";
    }

    try {
      int count = notificationService.markAllAsRead(currentUser.getId());
      redirectAttributes.addFlashAttribute("success", count + " notifications marked as read");
    } catch (Exception e) {
      logger.warning("Error marking all notifications as read: " + e.getMessage());
      redirectAttributes.addFlashAttribute("error", "Failed to mark notifications as read");
    }

    return "redirect:/notifications";
  }

  @DeleteMapping("/{id}/delete")
  public String deleteNotification(
          @PathVariable Long id,
          @RequestParam(required = false) String returnUrl,
          @AuthenticationPrincipal User currentUser,
          RedirectAttributes redirectAttributes) {

    if (currentUser == null) {
      return "redirect:/auth/login";
    }

    try {
      boolean success = notificationService.deleteNotification(id, currentUser.getId());

      if (success) {
        redirectAttributes.addFlashAttribute("success", "Notification deleted");
      } else {
        redirectAttributes.addFlashAttribute("error", "Failed to delete notification");
      }

      // If returnUrl is provided, redirect back to the original page
      if (returnUrl != null && !returnUrl.isEmpty()) {
        return "redirect:" + returnUrl;
      }

    } catch (Exception e) {
      logger.warning("Error deleting notification: " + e.getMessage());
      redirectAttributes.addFlashAttribute("error", "Failed to delete notification");
    }

    return "redirect:/notifications";
  }

  @PostMapping("/clear-all")
  public String clearAllNotifications(
          @AuthenticationPrincipal User currentUser,
          RedirectAttributes redirectAttributes) {

    if (currentUser == null) {
      return "redirect:/auth/login";
    }

    try {
      int count = notificationService.clearAllNotifications(currentUser.getId());
      redirectAttributes.addFlashAttribute("success", count + " notifications cleared");
    } catch (Exception e) {
      logger.warning("Error clearing notifications: " + e.getMessage());
      redirectAttributes.addFlashAttribute("error", "Failed to clear notifications");
    }

    return "redirect:/notifications";
  }

  /**
   * Subscribe to notifications for a book
   */
  @PostMapping("/subscribe")
  public String subscribeToNotifications(
          @RequestParam("bookId") Long bookId,
          @RequestParam("userId") Long userId,
          @AuthenticationPrincipal User currentUser,
          RedirectAttributes redirectAttributes) {

    if (currentUser == null || !currentUser.getId().equals(userId)) {
      return "redirect:/auth/login";
    }

    try {
      // Call your notification queue service here
      // Example: bookQueueService.joinQueue(bookId, userId);
      redirectAttributes.addFlashAttribute("success", "You will be notified when the book becomes available");
    } catch (Exception e) {
      logger.warning("Error subscribing to notifications: " + e.getMessage());
      redirectAttributes.addFlashAttribute("error", "Failed to subscribe to notifications");
    }

    return "redirect:/books/" + bookId;
  }

  /**
   * AJAX endpoint to get notification counts
   */
  @GetMapping("/counts")
  @ResponseBody
  public ResponseEntity<Integer> getNotificationCounts(@AuthenticationPrincipal User currentUser) {
    if (currentUser == null) {
      return ResponseEntity.badRequest().build();
    }

    int counts = notificationService.getUnreadCounts(currentUser.getId());
    return ResponseEntity.ok(counts);
  }
}