package fr.uge.booqIn.controller.web;

import org.springframework.context.annotation.Profile;
import fr.uge.booqIn.dto.auth.LoginDto;
import fr.uge.booqIn.dto.auth.RegisterDto;
import fr.uge.booqIn.service.interfaces.AuthServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Profile("!demo")
@Controller
@RequestMapping("/auth")
public class AuthWebController {

  private final AuthServiceInterface authService;
  private final HomeWebController homeWebController;

  @Autowired
  public AuthWebController(AuthServiceInterface authService, HomeWebController homeWebController) {
    this.authService = authService;
    this.homeWebController = homeWebController;
  }

  @GetMapping("/login")
  public String showLoginForm(Model model,
                              @RequestParam(value = "error", required = false) String error,
                              Principal principal) {
    // Préparer le modèle commun
    homeWebController.prepareModel(model, principal, "login");

    model.addAttribute("loginDto", new LoginDto(null, null));
    model.addAttribute("error", error);

    // Définir le titre et le contenu
    model.addAttribute("title", "BooqIn - Connexion");
    model.addAttribute("content", "pages/auth/login");

    return "layout";
  }

  @GetMapping("/register")
  public String showSignUpForm(Model model, Principal principal) {
    // Préparer le modèle commun
    homeWebController.prepareModel(model, principal, "register");

    model.addAttribute("registerDto", new RegisterDto(null, null, null, null, null, null));

    // Définir le titre et le contenu
    model.addAttribute("title", "BooqIn - Inscription");
    model.addAttribute("content", "pages/auth/register");

    return "layout";
  }

  @PostMapping("/register")
  public String processRegisterForm(@ModelAttribute RegisterDto registerDto, Model model, Principal principal) {
    try {
      authService.register(registerDto);
      return "redirect:/";
    } catch (Exception e) {
      // Préparer le modèle commun même en cas d'erreur
      homeWebController.prepareModel(model, principal, "register");

      model.addAttribute("error", e.getMessage());
      model.addAttribute("registerDto", registerDto);

      // Définir le titre et le contenu
      model.addAttribute("title", "BooqIn - Inscription");
      model.addAttribute("content", "pages/auth/register");

      return "layout";
    }
  }

}