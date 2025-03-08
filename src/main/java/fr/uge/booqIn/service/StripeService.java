package fr.uge.booqIn.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.param.checkout.SessionCreateParams;
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
import com.stripe.model.checkout.Session;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StripeService {
    
    @Value("${stripe.secret.key}")
    private String stripeSecretKey;
    
    @Value("${stripe.webhook.secret}")
    private String webhookSecret;
    
    private static final double SHIPPING_COST = 5.99;
    private final CartRepository cartRepository;
    
    
    public StripeService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
        System.out.println("stripe.webhook.secret " + webhookSecret);
    }
    
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }
    
    private Event constructStripeEvent(String payload, String sigHeader) throws SignatureVerificationException {
        System.out.println("payload " + webhookSecret);
        return Webhook.constructEvent(payload, sigHeader, webhookSecret);
    }
    
    public Event handleStripeWebhook(String payload, String sigHeader) throws SignatureVerificationException {
        return constructStripeEvent(payload, sigHeader);
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
        System.out.println("Traitement de l'événement Stripe: " + event.getType() + " avec ID: " + event.getId());
        
        switch (event.getType()) {
            case "checkout.session.completed":
                processCheckoutSessionCompleted(event);
                break;
            
            case "payment_intent.payment_failed":
                processPaymentIntentFailed(event);
                break;
            
            default:
                System.out.println("ℹ️ Autre événement Stripe reçu : " + event.getType());
                break;
        }
    }
    
 
    private void processCheckoutSessionCompleted(Event event) {
        try {
            JsonNode rootNode = parseEventData(event);
            
            String sessionId = rootNode.path("id").asText();
            String paymentIntentId = rootNode.path("payment_intent").asText();
            
            if (paymentIntentId != null && !paymentIntentId.isEmpty()) {
                System.out.println("ℹ️ Paiement Stripe réussi pour la session ID: " + sessionId);
                System.out.println("ℹ️ Payment Intent ID: " + paymentIntentId);
                
                updateCartPaymentStatus(sessionId);
            } else {
                System.out.println("⚠️ Payment Intent ID introuvable dans la session");
            }
        } catch (Exception e) {
            handleProcessingError("session", e);
        }
    }
    

    private void processPaymentIntentFailed(Event event) {
        try {

            JsonNode rootNode = parseEventData(event);
            
            String paymentIntentId = rootNode.path("id").asText();
            
            if (paymentIntentId != null && !paymentIntentId.isEmpty()) {
                System.out.println("ℹ️ Paiement Stripe échoué : " + paymentIntentId);
                handlePaymentFailure(paymentIntentId);
            } else {
                System.out.println("⚠️ Payment Intent ID introuvable dans l'événement d'échec");
            }
        } catch (Exception e) {
            handleProcessingError("échec de paiement", e);
        }
    }
    

    private JsonNode parseEventData(Event event) throws IOException {
        String rawJson = event.getDataObjectDeserializer().getRawJson();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(rawJson);
    }
    

    private void updateCartPaymentStatus(String sessionId) {
        try {
            Cart cart = cartRepository.findByPaymentIntentId(sessionId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Panier introuvable pour la session Stripe ID : " + sessionId
                    ));
            
            cart.setStatut(StatutPayment.CONFIRMED);
            cartRepository.save(cart);
            
            System.out.println("✅ Statut de paiement mis à jour pour le panier ID : " + cart.getId() + " -> " + StatutPayment.CONFIRMED);
        } catch (ResponseStatusException e) {
            System.out.println(e.getMessage());
        } catch (Exception e) {
            System.out.println("⚠️ Erreur lors de la mise à jour du statut du panier: " + e.getMessage());
        }
    }
    

    private void handleProcessingError(String contextName, Exception e) {
        System.out.println("⚠️ Erreur lors du traitement de la " + contextName + ": " + e.getMessage());
    }
    
    public void handleCheckoutSuccess(Session session) {
        String sessionId = session.getId();
        
        Cart cart = cartRepository.findByPaymentIntentId(sessionId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Panier introuvable pour la session Stripe ID : " + sessionId
                ));
        
        cart.setStatut(StatutPayment.CONFIRMED);
        cartRepository.save(cart);
        
        System.out.println("✅ Paiement confirmé pour le panier ID : " + cart.getId());
    }
    
    
    

    @Transactional
    public Session createCheckoutSession(Cart cart) {
        try {
            List<SessionCreateParams.LineItem> lineItems = cart.getBooks().stream()
                    .map(book -> SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("eur")
                                    .setUnitAmount((long) (5.99 * 100))
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName(book.getTitle())
                                            .build())
                                    .build())
                            .build())
                    .collect(Collectors.toList());
            
            SessionCreateParams params = SessionCreateParams.builder()
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl("http://localhost:5173/")
                    .setCancelUrl("http://localhost:5173/")
                    .addAllLineItem(lineItems)
                    .build();
            
            Session session = Session.create(params);
            cart.setPaymentIntentId(session.getId());
            cartRepository.save(cart);
            
            return session;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création de la session Stripe", e);
        }
    }
}
