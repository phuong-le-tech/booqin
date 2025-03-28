package fr.uge.booqIn.controller.api;

import com.stripe.model.checkout.Session;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.StripeService;
import fr.uge.booqIn.service.interfaces.CartServiceInterface;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/payment")
public class StripeController {
    private String URL_WEBSITE = "http://localhost:5173/";
    private final StripeService stripeService;
    private final CartServiceInterface cartService;
    
    public StripeController(StripeService stripeService, CartServiceInterface cartService) {
        this.stripeService = stripeService;
        this.cartService = cartService;
    }
    
    
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<String>> createCheckoutSession(@RequestParam Long cartId , @RequestParam(required = false) String returnUrl)  {
        Optional<Cart> cartOptional = Optional.ofNullable(cartService.getCart(cartId));
        
        if (cartOptional.isEmpty() || cartOptional.get().getBooks().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Le panier est vide ou inexistant"));
        }
        String successUrl = returnUrl != null ? returnUrl : URL_WEBSITE;
        Session session = stripeService.createCheckoutSession(cartOptional.get() ,successUrl);
        
        return ResponseEntity.ok(ApiResponse.success("Session Stripe créée", session.getUrl()));
    }
    
}