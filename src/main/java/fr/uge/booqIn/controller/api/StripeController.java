package fr.uge.booqIn.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.PaymentIntent;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.service.CartService;
import fr.uge.booqIn.service.StripeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class StripeController {
    
    private final StripeService stripeService;
    private final CartService cartService;
    
    public StripeController(StripeService stripeService, CartService cartService) {
        this.stripeService = stripeService;
        this.cartService = cartService;
    }
    
    @PostMapping("/create")
    public ResponseEntity<?> createPaymentIntent(@RequestParam Long cartId) {
        try {
            Cart cart = cartService.getCart(cartId);
            PaymentIntent paymentIntent = stripeService.createPaymentIntent(cart);
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = paymentIntent.toJson();
            Map<String, Object> jsonResponse = objectMapper.readValue(jsonString, Map.class);
            
            return ResponseEntity.ok(jsonResponse);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erreur : " + e.getMessage());
        }
    }
    
    @PostMapping("/confirm/{paymentIntentId}")
    public ResponseEntity<PaymentIntent> confirmPayment(@PathVariable String paymentIntentId) {
        PaymentIntent confirmedPayment = stripeService.confirmPayment(paymentIntentId);
        return ResponseEntity.ok(confirmedPayment);
    }
    
    @PostMapping("/cancel/{paymentIntentId}")
    public ResponseEntity<PaymentIntent> cancelPayment(@PathVariable String paymentIntentId) {
        PaymentIntent canceledPayment = stripeService.cancelPayment(paymentIntentId);
        return ResponseEntity.ok(canceledPayment);
    }
}
