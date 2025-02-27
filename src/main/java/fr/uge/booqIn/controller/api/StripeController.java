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
        PaymentIntent paymentIntent = stripeService.createPaymentIntent(cart);
        PaymentIntentResponseDTO response = new PaymentIntentResponseDTO(
                paymentIntent.getId(),
                paymentIntent.getClientSecret(),
                paymentIntent.getStatus()
        );
        
        return ResponseEntity.ok(ApiResponse.success("PaymentIntent créé", response));
    }
    
    @PostMapping("/confirm/{paymentIntentId}")
    public ResponseEntity<ApiResponse<PaymentIntent>> confirmPayment(@PathVariable String paymentIntentId) throws StripeException {
        Optional<PaymentIntent> confirmedPayment = Optional.ofNullable(stripeService.confirmPayment(paymentIntentId));
        return confirmedPayment.map(paymentIntent -> ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Paiement confirmé", paymentIntent)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Erreur lors de la confirmation du paiement")));
    }
    
    @PostMapping("/cancel/{paymentIntentId}")
    public ResponseEntity<ApiResponse<PaymentIntent>> cancelPayment(@PathVariable String paymentIntentId) throws StripeException {
        Optional<PaymentIntent> canceledPayment = Optional.ofNullable(stripeService.cancelPayment(paymentIntentId));
        return canceledPayment.map(paymentIntent -> ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Paiement annulé", paymentIntent)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Erreur lors de l'annulation du paiement")));
    }
}
