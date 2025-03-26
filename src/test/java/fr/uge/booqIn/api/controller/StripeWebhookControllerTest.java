package fr.uge.booqIn.api.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import fr.uge.booqIn.controller.api.StripeWebhookController;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.StripeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("StripeWebhookController Tests")
class StripeWebhookControllerTest {

  private StripeService stripeService;
  private StripeWebhookController stripeWebhookController;

  @BeforeEach
  void setUp() {
    stripeService = Mockito.mock(StripeService.class);
    stripeWebhookController = new StripeWebhookController(stripeService);
  }

  @Test
  @DisplayName("POST /webhook - Should process webhook and return success")
  void handleStripeWebhook_ShouldProcessAndReturnSuccess() throws SignatureVerificationException {
    // Given
    String payload = "{\"type\":\"payment_intent.succeeded\"}";
    String sigHeader = "test_signature";

    Event event = new Event();
    event.setType("payment_intent.succeeded");

    when(stripeService.handleStripeWebhook(eq(payload), eq(sigHeader))).thenReturn(event);
    doNothing().when(stripeService).processWebhookEvent(any(Event.class));

    // When
    ResponseEntity<ApiResponse<String>> response = stripeWebhookController.handleStripeWebhook(payload, sigHeader);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<String> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Webhook traité", apiResponse.message());

    verify(stripeService).handleStripeWebhook(eq(payload), eq(sigHeader));
    verify(stripeService).processWebhookEvent(eq(event));
  }

  @Test
  @DisplayName("POST /webhook - Should propagate signature verification exception")
  void handleStripeWebhook_ShouldPropagateSignatureVerificationException() throws SignatureVerificationException {
    // Given
    String payload = "{\"type\":\"payment_intent.succeeded\"}";
    String sigHeader = "invalid_signature";

    when(stripeService.handleStripeWebhook(eq(payload), eq(sigHeader)))
            .thenThrow(new SignatureVerificationException("Invalid signature", sigHeader));

    // When & Then
    assertThrows(SignatureVerificationException.class, () ->
            stripeWebhookController.handleStripeWebhook(payload, sigHeader)
    );

    verify(stripeService).handleStripeWebhook(eq(payload), eq(sigHeader));
    verify(stripeService, never()).processWebhookEvent(any(Event.class));
  }
}