package fr.uge.booqIn.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.PaymentIntent;
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
    public ResponseEntity<ApiResponse<?>> createPaymentIntent(@RequestParam Long cartId) {
        try {
            Cart cart = cartService.getCart(cartId);
            PaymentIntent paymentIntent = stripeService.createPaymentIntent(cart);
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = paymentIntent.toJson();
            Map<String, Object> jsonResponse = objectMapper.readValue(jsonString, Map.class);

            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("PaymentIntent créé", jsonResponse));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Erreur lors de la création du PaymentIntent"));
        }
    }
    
    @PostMapping("/confirm/{paymentIntentId}")
    public ResponseEntity<ApiResponse<PaymentIntent>> confirmPayment(@PathVariable String paymentIntentId) {
        Optional<PaymentIntent> confirmedPayment = Optional.ofNullable(stripeService.confirmPayment(paymentIntentId));
        return confirmedPayment.map(paymentIntent -> ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Paiement confirmé", paymentIntent)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Erreur lors de la confirmation du paiement")));
    }
    
    @PostMapping("/cancel/{paymentIntentId}")
    public ResponseEntity<ApiResponse<PaymentIntent>> cancelPayment(@PathVariable String paymentIntentId) {
        Optional<PaymentIntent> canceledPayment = Optional.ofNullable(stripeService.cancelPayment(paymentIntentId));
        return canceledPayment.map(paymentIntent -> ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Paiement annulé", paymentIntent)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("Erreur lors de l'annulation du paiement")));
    }
}
