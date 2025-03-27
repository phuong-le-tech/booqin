package fr.uge.booqIn.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.Map;

@Controller
@RequestMapping("/api/v1/public")
public class PublicPaymentController {
  
  @GetMapping("/payment-success")
    public ModelAndView getPaymentSuccessPage(@RequestParam Long cartId) {
      ModelAndView modelAndView = new ModelAndView("pages/payment-redirect");
      modelAndView.addObject("cartId", cartId);
      return modelAndView;
  }
}