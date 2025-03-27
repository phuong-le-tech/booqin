package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.dto.notification.BookNotificationDTO;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import fr.uge.booqIn.service.interfaces.BookWaitingQueueServiceInterface;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.logging.Logger;

@Controller
@RequestMapping("/book-queue")
public class BookWaitingQueueWebController {

  private static final Logger logger = Logger.getLogger(BookWaitingQueueWebController.class.getName());
  private final BookWaitingQueueServiceInterface queueService;
  private final UserServiceInterface userService;
  private final BookServiceInterface bookService;

  public BookWaitingQueueWebController(
          BookWaitingQueueServiceInterface queueService,
          UserServiceInterface userService,
          BookServiceInterface bookService) {
    this.queueService = queueService;
    this.userService = userService;
    this.bookService = bookService;
  }

  @PostMapping("/subscribe")
  public String subscribeToNotification(
          @RequestParam Long bookId,
          @AuthenticationPrincipal User currentUser,
          @RequestParam(required = false) String returnUrl,
          RedirectAttributes redirectAttributes) {

    if (currentUser == null || currentUser.getId() == null) {
      redirectAttributes.addFlashAttribute("errorMessage", "Vous devez être connecté pour vous abonner aux notifications");
      return returnUrl != null ? "redirect:" + returnUrl : "redirect:/books";
    }

    boolean added = queueService.addToWaitingQueue(bookId, currentUser.getId());

    if (added) {
      redirectAttributes.addFlashAttribute("successMessage", "Vous serez notifié lorsque ce livre sera disponible");
    } else {
      redirectAttributes.addFlashAttribute("infoMessage", "Vous êtes déjà sur la liste d'attente pour ce livre");
    }

    // Redirect to the book detail page or specified return URL
    return returnUrl != null ? "redirect:" + returnUrl : "redirect:/books/" + bookId;
  }

  @PostMapping("/cancel")
  public String cancelNotification(
          @RequestParam Long notificationId,
          @AuthenticationPrincipal User currentUser,
          @RequestParam(required = false) String returnUrl,
          @RequestParam(required = false) Long bookId,
          RedirectAttributes redirectAttributes) {

    if (currentUser == null || currentUser.getId() == null) {
      redirectAttributes.addFlashAttribute("errorMessage", "Vous devez être connecté pour effectuer cette action");
      return returnUrl != null ? "redirect:" + returnUrl : "redirect:/books";
    }

    boolean cancelled = queueService.cancelQueueEntry(notificationId, currentUser.getId());

    if (cancelled) {
      redirectAttributes.addFlashAttribute("successMessage", "Votre notification a été annulée avec succès");
    } else {
      redirectAttributes.addFlashAttribute("errorMessage", "Impossible d'annuler la notification");
    }

    // Redirect to the book detail page or specified return URL
    return returnUrl != null ? "redirect:" + returnUrl : bookId != null ? "redirect:/books/" + bookId : "redirect:/books";
  }

  @GetMapping("/active")
  public String viewActiveQueues(
          @AuthenticationPrincipal User currentUser,
          Model model) {

    if (currentUser == null || currentUser.getId() == null) {
      return "redirect:/login";
    }

    List<BookNotificationDTO> activeQueues = queueService.getUserActiveQueues(currentUser.getId());

    model.addAttribute("activeQueues", activeQueues);
    model.addAttribute("title", "Vos livres en attente");
    model.addAttribute("content", "pages/book/waiting-queues");

    return "layout";
  }

  @PostMapping("/reserve")
  public String reserveNotifiedBook(
          @RequestParam Long notificationId,
          @AuthenticationPrincipal User currentUser,
          @RequestParam(required = false) String returnUrl,
          RedirectAttributes redirectAttributes) {

    if (currentUser == null || currentUser.getId() == null) {
      redirectAttributes.addFlashAttribute("errorMessage", "Vous devez être connecté pour effectuer cette action");
      return returnUrl != null ? "redirect:" + returnUrl : "redirect:/books";
    }

    boolean reserved = queueService.reserveBookFromQueue(notificationId, currentUser.getId());

    if (reserved) {
      redirectAttributes.addFlashAttribute("successMessage", "Livre réservé avec succès");
      return "redirect:/cart";
    } else {
      redirectAttributes.addFlashAttribute("errorMessage", "Impossible de réserver le livre");
      return returnUrl != null ? "redirect:" + returnUrl : "redirect:/books";
    }
  }
}