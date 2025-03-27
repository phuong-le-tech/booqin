package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.entity.Notification;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.service.interfaces.NotificationServiceInterface;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeWebController {

  private final UserServiceInterface userService;
  private final NotificationServiceInterface notificationService;

  @Autowired
  public HomeWebController(UserServiceInterface userService,
                           NotificationServiceInterface notificationService) {
    this.userService = userService;
    this.notificationService = notificationService;
  }

  /**
   * Prepare common model attributes used across multiple views
   * @param model The Spring MVC model
   * @param principal The authenticated user principal
   * @param currentPage Current page name for navigation highlighting
   */
  public void prepareModel(Model model, Principal principal, String currentPage) {
    boolean isAuthenticated = principal != null;
    String username = isAuthenticated ? principal.getName() : null;

    model.addAttribute("isAuthenticated", isAuthenticated);
    model.addAttribute("username", username);
    model.addAttribute("currentPage", currentPage);

    // Add administrator status if user is authenticated
    if (isAuthenticated) {
      User user = userService.findByUsername(username);
      boolean isAdmin = user != null && user.getRole() == UserRole.ADMIN;
      model.addAttribute("isAdmin", isAdmin);

      // Get unread notification count
      assert user != null;
      Long userId = user.getId();
      int unreadCount = notificationService.getUnreadCounts(userId);
      model.addAttribute("unreadNotificationsCount", unreadCount);

      // Get latest 5 notifications for dropdown
      PageRequest pageRequest = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
      Page<Notification> notificationsPage = notificationService.getNotifications(userId, pageRequest);
      List<Notification> latestNotifications = notificationsPage.getContent();
      model.addAttribute("latestNotifications", latestNotifications);
    } else {
      model.addAttribute("isAdmin", false);
      model.addAttribute("unreadNotificationsCount", 0);
      model.addAttribute("latestNotifications", new ArrayList<>());
    }
  }

  @GetMapping({"/", "/home"})
  public String home(Model model, Principal principal) {
    prepareModel(model, principal, "/home");

    model.addAttribute("title", "BooqIn - Home");
    model.addAttribute("content", "pages/home");

    return "layout";
  }
}