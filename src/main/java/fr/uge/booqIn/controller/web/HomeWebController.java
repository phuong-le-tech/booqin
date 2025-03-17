package fr.uge.booqIn.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Collections;

@Controller
public class HomeWebController {

  @GetMapping({"/", "/home"})
  public String home(Model model, Principal principal) {
    prepareModel(model, principal, "home");

    // Set page title for navbar
    model.addAttribute("title", "BooqIn - Home");

    return "layout"; // Will use layout template with content="pages/home"
  }

  /**
   * Helper method to prepare common model attributes
   */
  public void prepareModel(Model model, Principal principal, String currentPage) {
    if (principal != null) {
      String username = principal.getName();
      model.addAttribute("isAuthenticated", true);
      model.addAttribute("username", username);
      model.addAttribute("currentPage", currentPage);

      // For demonstration purposes, set isAdmin to true
      // In a real app, this would come from your user service
      model.addAttribute("isAdmin", true);

      // Sample notifications - in a real app, these would come from your notification service
      model.addAttribute("notifications", null);
      model.addAttribute("unreadNotificationsCount", 2);

      // Set content template
      model.addAttribute("content", "pages/" + currentPage);
    } else {
      model.addAttribute("isAuthenticated", false);
      model.addAttribute("currentPage", currentPage);
      model.addAttribute("notifications", Collections.emptyList());
      model.addAttribute("unreadNotificationsCount", 0);

      // Set content template
      model.addAttribute("content", "pages/" + currentPage);
    }
  }
}