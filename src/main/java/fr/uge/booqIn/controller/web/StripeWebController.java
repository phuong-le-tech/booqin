package fr.uge.booqIn.controller.web;

import org.springframework.context.annotation.Profile;
import com.stripe.model.checkout.Session;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.service.StripeService;
import fr.uge.booqIn.service.interfaces.CartServiceInterface;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Profile("!demo")
@Controller
@RequestMapping("/payment")
public class StripeWebController {
  
  private static String  URL_WEBSITE = "http://localhost:8080/";
  private final StripeService stripeService;
  private final CartServiceInterface cartService;
  
  public StripeWebController(StripeService stripeService, CartServiceInterface cartService) {
    this.stripeService = stripeService;
    this.cartService = cartService;
  }
  
  @PostMapping("/checkout")
  public String createCheckoutSession(@RequestParam Long cartId, Model model) {
    Optional<Cart> cartOptional = Optional.ofNullable(cartService.getCart(cartId));
    
    if (cartOptional.isEmpty() || cartOptional.get().getBooks().isEmpty()) {
      model.addAttribute("error", "Le panier est vide ou inexistant");
      return "cart"; // Redirection vers la page du panier en cas d'erreur
    }
    
    Session session = stripeService.createCheckoutSession(cartOptional.get() , URL_WEBSITE);
    // Ajouter l'URL de la session Stripe au modèle pour redirection dans la vue
    model.addAttribute("stripeUrl", session.getUrl());
    return "redirect:" + session.getUrl(); // Redirige vers la page de paiement Stripe
  }
}
