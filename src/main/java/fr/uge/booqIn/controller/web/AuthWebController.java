package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.dto.auth.LoginDto;
import fr.uge.booqIn.dto.auth.RegisterDto;
import fr.uge.booqIn.service.interfaces.AuthServiceInterface;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthWebController {

  private final AuthServiceInterface authService;

  public AuthWebController(AuthServiceInterface authService) {
    this.authService = authService;
  }

  @GetMapping("/login")
  public String showLoginForm(Model model,
                              @RequestParam(value = "error", required = false) String error) {
    model.addAttribute("loginDto", new LoginDto(null, null));
    model.addAttribute("error", error);
    return "auth/login";
  }

  @GetMapping("/register")
  public String showSignUpForm(Model model) {
    model.addAttribute("registerDto", new RegisterDto(null, null, null, null, null, null));
    return "auth/register";
  }

  @PostMapping("/register")
  public String processRegisterForm(@ModelAttribute RegisterDto registerDto, Model model) {
    System.out.println(registerDto);
    try {
      authService.register(registerDto);
      return "home/index";
    } catch (Exception e) {
      model.addAttribute("error", e.getMessage());
      return "auth/register";
    }
  }
}
