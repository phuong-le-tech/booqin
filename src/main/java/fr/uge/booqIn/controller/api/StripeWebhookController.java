package fr.uge.booqIn.controller.api;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.StripeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stripe")
public class StripeWebhookController {
  
  private final StripeService stripeService;

  public StripeWebhookController(StripeService stripeService) {
    this.stripeService = stripeService;
  }
  
  @PostMapping("/webhook")
  public ResponseEntity<ApiResponse<String>> handleStripeWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) throws SignatureVerificationException {
    Event event;
    event = stripeService.handleStripeWebhook(payload, sigHeader);
    stripeService.processWebhookEvent(event);
    return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Webhook traité"));
  }
}

