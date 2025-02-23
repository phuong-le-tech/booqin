package fr.uge.booqIn.service;

import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.enums.Statut;
import fr.uge.booqIn.repository.CartRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    private static final double SHIPPING_COST = 5.99;
    private  CartRepository cartRepository = null;
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
        
    }
    
    
    public StripeService(CartRepository cartRepository) {
        Stripe.apiKey = stripeSecretKey;
        this.cartRepository = cartRepository;
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

    public PaymentIntent confirmPayment(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            return paymentIntent.confirm();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la confirmation du paiement", e);
        }
    }

    public PaymentIntent cancelPayment(String paymentIntentId) {
        try {
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
            return paymentIntent.cancel();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'annulation du paiement", e);
        }
    }

    public void handlePaymentSuccess(String paymentIntentId) {
        Cart cart = cartRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Panier introuvable pour cet identifiant"));
        cart.setStatut(Statut.CONFIRMED);
        cartRepository.save(cart);
    }
    
 
    public void handlePaymentFailure(String paymentIntentId) {
        Cart cart = cartRepository.findByPaymentIntentId(paymentIntentId)
                .orElseThrow(() -> new RuntimeException("Panier introuvable pour cet identifiant"));
        cart.setStatut(Statut.CANCELLED);
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