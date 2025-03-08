package fr.uge.booqIn.controller.api;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import fr.uge.booqIn.dto.stripe.PaymentIntentResponseDTO;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.StripeService;
import fr.uge.booqIn.service.impl.CartServiceImpl;
import fr.uge.booqIn.service.interfaces.CartServiceInterface;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/payment")
public class StripeController {
    
    private final StripeService stripeService;
    private final CartServiceInterface cartService;
    
    public StripeController(StripeService stripeService, CartServiceInterface cartService) {
        this.stripeService = stripeService;
        this.cartService = cartService;
    }
    
   
    
    
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<String>> createCheckoutSession(@RequestParam Long cartId) {
        Optional<Cart> cartOptional = Optional.ofNullable(cartService.getCart(cartId));
        
        if (cartOptional.isEmpty() || cartOptional.get().getBooks().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Le panier est vide ou inexistant"));
        }
        
        Session session = stripeService.createCheckoutSession(cartOptional.get());
        
        return ResponseEntity.ok(ApiResponse.success("Session Stripe créée", session.getUrl()));
    }
    
}