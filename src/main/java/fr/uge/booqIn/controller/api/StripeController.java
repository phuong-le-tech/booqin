package fr.uge.booqIn.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import fr.uge.booqIn.dto.stripe.PaymentIntentResponseDTO;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.CartService;
import fr.uge.booqIn.service.StripeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/payment")
public class StripeController {
    
    private final StripeService stripeService;
    private final CartService cartService;
    
    public StripeController(StripeService stripeService, CartService cartService) {
        this.stripeService = stripeService;
        this.cartService = cartService;
    }
    
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<PaymentIntentResponseDTO>> createPaymentIntent(@RequestParam Long cartId) {
        Cart cart = cartService.getCart(cartId);
        if (cart == null || cart.getBooks().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Le panier est vide ou inexistant"));
        }
        
        PaymentIntent paymentIntent = stripeService.createPaymentIntent(cart);
        PaymentIntentResponseDTO response = new PaymentIntentResponseDTO(
                paymentIntent.getId(),
                paymentIntent.getClientSecret(),
                paymentIntent.getStatus()
        );
        
        return ResponseEntity.ok(ApiResponse.success("PaymentIntent créé", response));
    }
    
    @PostMapping("/confirm/{paymentIntentId}")
    public ResponseEntity<ApiResponse<PaymentIntent>> confirmPayment(@PathVariable String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = stripeService.confirmPayment(paymentIntentId);
            
            if (paymentIntent == null || !"succeeded".equals(paymentIntent.getStatus())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("La confirmation du paiement a échoué"));
            }
            
            stripeService.handlePaymentSuccess(paymentIntentId);
            
            return ResponseEntity.ok(ApiResponse.success("Paiement confirmé avec succès", paymentIntent));
            
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur Stripe : " + e.getMessage()));
        }
    }
    
    @PostMapping("/cancel/{paymentIntentId}")
    public ResponseEntity<ApiResponse<PaymentIntent>> cancelPayment(@PathVariable String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = stripeService.cancelPayment(paymentIntentId);
            
            if (paymentIntent == null || !"canceled".equals(paymentIntent.getStatus())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("L'annulation du paiement a échoué"));
            }
            
            stripeService.handlePaymentFailure(paymentIntentId);
            
            return ResponseEntity.ok(ApiResponse.success("Paiement annulé avec succès", paymentIntent));
            
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erreur Stripe : " + e.getMessage()));
        }
    }

}
