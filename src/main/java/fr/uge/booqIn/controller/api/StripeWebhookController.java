package fr.uge.booqIn.controller.api;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import fr.uge.booqIn.service.StripeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/stripe")
public class StripeWebhookController {
  
  private final StripeService stripeService;
  
  @Value("${stripe.webhook.secret}")
  private String endpointSecret;
  
  public StripeWebhookController(StripeService stripeService) {
    this.stripeService = stripeService;
  }
  
  @PostMapping("/webhook")
  public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
                                                    @RequestHeader("Stripe-Signature") String sigHeader) {
    Event event;
    try {
      event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
    } catch (SignatureVerificationException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Signature invalide");
    }
    
    stripeService.processWebhookEvent(event);
    return ResponseEntity.ok("Événement traité");
  }
}
