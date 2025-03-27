package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import fr.uge.booqIn.service.interfaces.BookWaitingQueueServiceInterface;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/notifications")
public class NotificationWebController {
  
  private final BookWaitingQueueServiceInterface notificationService;
  private final UserServiceInterface userService;
  private final BookServiceInterface bookService;
  
  public NotificationWebController(
          BookWaitingQueueServiceInterface notificationService,
          UserServiceInterface userService,
          BookServiceInterface bookService) {
    this.notificationService = notificationService;
    this.userService = userService;
    this.bookService = bookService;
  }
  
  @PostMapping("/subscribe")
  public String subscribeToNotification(
          @RequestParam Long bookId,
          @AuthenticationPrincipal User currentUser,
          RedirectAttributes redirectAttributes) {
    
    if (currentUser == null || currentUser.getId() == null) {
      redirectAttributes.addFlashAttribute("errorMessage", "You must be logged in to subscribe to notifications");
      return "redirect:/books";
    }
    
    boolean added = notificationService.addToWaitingQueue(bookId, currentUser.getId());
    
    if (added) {
      redirectAttributes.addFlashAttribute("successMessage", "You will be notified when this book becomes available");
    } else {
      redirectAttributes.addFlashAttribute("infoMessage", "You are already on the notification list for this book");
    }
    return "redirect:/books";
  }
}