package fr.uge.booqIn.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.exception.SignatureVerificationException ;
import fr.uge.booqIn.entity.Cart;
import com.stripe.net.Webhook;
import fr.uge.booqIn.enums.StatutPayment;
import fr.uge.booqIn.repository.CartRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StripeService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;
    /*
    @Value("${stripe.webhook.secret}")
     */
    private String endpointSecret;
    
    private static final double SHIPPING_COST = 5.99;
    private final CartRepository cartRepository ;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        
    }
    
    
    public StripeService(CartRepository cartRepository) {
        Stripe.apiKey = stripeSecretKey;
        this.cartRepository = cartRepository;
    }
    
    private Event constructStripeEvent(String payload, String sigHeader) throws SignatureVerificationException {
        return Webhook.constructEvent(payload, sigHeader, endpointSecret);
    }
    
    public Event handleStripeWebhook(String payload, String sigHeader) throws SignatureVerificationException {
        return constructStripeEvent(payload, sigHeader);
    }

    public PaymentIntent createPaymentIntent(Cart cart) {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount((long) (SHIPPING_COST * 100))
                    .setCurrency("eur")
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build())
                    .build();

            return PaymentIntent.create(params);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création du paiement", e);
        }
    }
    
    /*** Confirmation du paiement ***/
    public PaymentIntent confirmPayment(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        return paymentIntent.confirm();
    }
    
    /*** Annulation du paiement ***/
    public PaymentIntent cancelPayment(String paymentIntentId) throws  StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        return paymentIntent.cancel();
    }
    public void handlePaymentSuccess(String paymentIntentId) {
        Cart cart = cartRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Panier introuvable pour l'identifiant de paiement : " + paymentIntentId
                ));
        cart.setStatut(StatutPayment.CONFIRMED);
        cartRepository.save(cart);
    }
    
    public void handlePaymentFailure(String paymentIntentId) {
        Cart cart = cartRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Panier introuvable pour l'identifiant de paiement : " + paymentIntentId
                ));
        cart.setStatut(StatutPayment.CANCELLED);
        cartRepository.save(cart);
    }
    @Transactional
    public void processWebhookEvent(Event event) {
        switch (event.getType()) {
            case "payment_intent.succeeded":
                PaymentIntent intentSucceeded = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (intentSucceeded != null) {
                    handlePaymentSuccess(intentSucceeded.getId());
                }
                break;
            case "payment_intent.payment_failed":
                PaymentIntent intentFailed = (PaymentIntent) event.getDataObjectDeserializer()
                        .getObject().orElse(null);
                if (intentFailed != null) {
                    handlePaymentFailure(intentFailed.getId());
                }
                break;
            // à voir pour d'autres types d'événements
            default:
                break;
        }
    }

}