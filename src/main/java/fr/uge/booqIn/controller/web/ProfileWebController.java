package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.dto.user.UserUpdate;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/profile")
public class ProfileWebController {

  private static final Logger logger = LoggerFactory.getLogger(ProfileWebController.class);

  private final UserServiceInterface userService;
  private final HomeWebController homeWebController;

  @Autowired
  public ProfileWebController(UserServiceInterface userService, HomeWebController homeWebController) {
    this.userService = userService;
    this.homeWebController = homeWebController;
  }

  @GetMapping("")
  public String showProfile(Model model, Principal principal) {
    homeWebController.prepareModel(model, principal, "profile");

    if (principal != null) {
      User authenticatedUser = (User) ((Authentication) principal).getPrincipal();
      UserShow userShow = userService.findById(authenticatedUser.getId());

      model.addAttribute("user", userShow);
    }

    model.addAttribute("title", "BooqIn - My Profile");
    model.addAttribute("content", "pages/user/profile");
    return "layout";
  }

  @GetMapping("/edit")
  public String showEditProfileForm(Model model, Principal principal) {
    homeWebController.prepareModel(model, principal, "profile");

    if (principal != null) {
      User authenticatedUser = (User) ((Authentication) principal).getPrincipal();
      UserShow userShow = userService.findById(authenticatedUser.getId());

      UserUpdate userUpdate = new UserUpdate(
              Optional.ofNullable(userShow.firstName()),
              Optional.ofNullable(userShow.lastName()),
              Optional.ofNullable(userShow.email()),
              Optional.ofNullable(userShow.address()),
              Optional.empty()
      );

      model.addAttribute("user", userShow);
      model.addAttribute("userUpdate", userUpdate);
      model.addAttribute("firstName", userUpdate.firstName().orElse(""));
      model.addAttribute("lastName", userUpdate.lastName().orElse(""));
      model.addAttribute("email", userUpdate.email().orElse(""));
    }

    model.addAttribute("title", "BooqIn - Edit my profile");
    model.addAttribute("content", "pages/user/edit-profile");
    return "layout";
  }

  @PostMapping("/edit")
  public String processEditProfileForm(
          @ModelAttribute UserUpdate userUpdate,
          Model model,
          Principal principal,
          RedirectAttributes redirectAttributes)
  {
    try {
      if (principal != null) {
        User authenticatedUser = (User) ((Authentication) principal).getPrincipal();
        Long userId = authenticatedUser.getId();

        userService.patchUser(userId, userUpdate);
        redirectAttributes.addFlashAttribute("success", "Your profile has been updated with success");
      }
      return "redirect:/profile";
    } catch (Exception e) {
      homeWebController.prepareModel(model, principal, "profile");

      model.addAttribute("error", e.getMessage());
      model.addAttribute("userUpdate", userUpdate);
      model.addAttribute("isEditing", true);

      model.addAttribute("title", "BooqIn - Edit my profile");
      model.addAttribute("content", "pages/user/edit-profile");
      return "layout";
    }
  }
}