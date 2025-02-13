package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.dto.auth.LoginDto;
import fr.uge.booqIn.dto.auth.RegisterDto;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/auth")
public class AuthWebController {

    private final AuthService authService;

    public AuthWebController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public String showSignInForm(Model model) {
        model.addAttribute("loginDto", new LoginDto(null, null));
        return "login";
    }

    @PostMapping("/login")
    public String processSignIn(
            @ModelAttribute("signInDto") LoginDto signInDto,
            Model model,
            HttpServletResponse response // needed to set cookies
    ) {
        try {
            // 1. Sign in and retrieve both access & refresh tokens
            var authResponse = authService.login(signInDto);

            // 2. Set the ACCESS TOKEN in an HttpOnly cookie
            Cookie jwtCookie = new Cookie("jwt", authResponse.accessToken());
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false);   // set to true if HTTPS
            jwtCookie.setPath("/");
            // Ex: 30 minutes in seconds => 1800
            jwtCookie.setMaxAge(30 * 60);

            // 3. Set the REFRESH TOKEN in another HttpOnly cookie
            Cookie refreshCookie = new Cookie("jwt-refresh", authResponse.refreshToken());
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(false);
            refreshCookie.setPath("/");
            // Ex: 7 days => 7*24*3600 = 604800
            refreshCookie.setMaxAge(7 * 24 * 60 * 60);

            // 4. Add them to the response
            response.addCookie(jwtCookie);
            response.addCookie(refreshCookie);

            // 5. Send user to home page (or wherever)
            return "home/index";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/register")
    public String showSignUpForm(Model model) {
        model.addAttribute("registerDto", new RegisterDto(null, null, null, null, null, null));
        return "Register";
    }

    @PostMapping("/register")
    public String processSignUp(
            @ModelAttribute("registerDto") RegisterDto registerDto,
            Model model
    ) {
        try {
            User user = authService.register(registerDto);
            model.addAttribute("user", user);
            return "home/index";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "Register";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        // Overwrite jwt cookie
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);

        // Overwrite refresh cookie
        Cookie refreshCookie = new Cookie("jwt-refresh", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(false);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(0);

        // Add them to response
        response.addCookie(jwtCookie);
        response.addCookie(refreshCookie);

        // (Optional) If you store refresh tokens in DB, remove or invalidate them here

        // Redirect to sign in or homepage
        return "redirect:/auth/login";
    }
}
