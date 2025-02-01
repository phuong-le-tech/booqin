package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.dto.auth.SignInDto;
import fr.uge.booqIn.dto.auth.SignUpDto;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthWebController {

    private final AuthService authService;

    public AuthWebController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/signin")
    public String showSignInForm(Model model) {
        model.addAttribute("signInDto", new SignInDto(null, null));
        return "auth/signin";
    }

    @PostMapping("/signin")
    public String processSignIn(@ModelAttribute("signInDto") SignInDto signInDto,
                                Model model) {
        try {
            String token = authService.signIn(signInDto);
            model.addAttribute("token", token);
            return "home/index";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/signin";
        }
    }

    @GetMapping("/signup")
    public String showSignUpForm(Model model) {
        model.addAttribute("signUpDto", new SignUpDto(null, null, null, null, null, null));
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String processSignUp(@ModelAttribute("signUpDto") SignUpDto signUpDto,
                                Model model) {
        try {
            User user = authService.signUp(signUpDto);

            model.addAttribute("user", user);
            return "home/index";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/signup";
        }
    }
}
