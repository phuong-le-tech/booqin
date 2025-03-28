package fr.uge.booqIn.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.param.checkout.SessionCreateParams;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.Cart;
import com.stripe.net.Webhook;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.StatusPayment;
import fr.uge.booqIn.repository.CartRepository;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.interfaces.BookLendingInterface;
import fr.uge.booqIn.service.interfaces.CartServiceInterface;
import jakarta.annotation.PostConstruct;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.stripe.model.checkout.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class StripeService {
  private static final Logger logger = Logger.getLogger(StripeService.class.getName());
  @Value("${stripe.secret.key}")
  private String stripeSecretKey;

  @Value("${stripe.webhook.secret}")
  private String webhookSecret;

  private static final double SHIPPING_COST = 5.99;
  private final CartRepository cartRepository;
  private final UserRepository userRepository;
  private final BookLendingInterface bookLendingService;
  private final CartServiceInterface cartService;


  public StripeService(CartRepository cartRepository, BookLendingInterface bookLendingService,
                       UserRepository userRepository, CartServiceInterface cartService) {
    this.cartRepository = cartRepository;
    this.bookLendingService = bookLendingService;
    this.userRepository = userRepository;
    this.cartService = cartService;
  }

  @PostConstruct
  public void init() {
    Stripe.apiKey = stripeSecretKey;
  }

  private Event constructStripeEvent(String payload, String sigHeader) throws SignatureVerificationException {
    return Webhook.constructEvent(payload, sigHeader, webhookSecret);
  }

  public Event handleStripeWebhook(String payload, String sigHeader) throws SignatureVerificationException {
    return constructStripeEvent(payload, sigHeader);
  }


  public void handlePaymentFailure(String paymentIntentId) {
    Cart cart = cartRepository.findByPaymentIntentId(paymentIntentId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Cart not found for payment intent ID: " + paymentIntentId
            ));
    cart.setStatut(StatusPayment.CANCELLED);
    cartRepository.save(cart);
  }


  @Transactional
  public void processWebhookEvent(Event event) {
    switch (event.getType()) {
      case "checkout.session.completed":
        processCheckoutSessionCompleted(event);
        break;

      case "payment_intent.payment_failed":
        processPaymentIntentFailed(event);
        break;

      default:
        break;
    }
  }


  private void processCheckoutSessionCompleted(Event event) {
    try {
      JsonNode rootNode = parseEventData(event);

      String sessionId = rootNode.path("id").asText();
      String paymentIntentId = rootNode.path("payment_intent").asText();
      if (paymentIntentId != null && !paymentIntentId.isEmpty()) {
        updateCartPaymentStatus(sessionId);
        var cart = cartRepository.findByPaymentIntentId(sessionId);
        cart.ifPresent(value -> cartService.clearCartAfterPayment(value.getId()));

      } else {
        logger.warning("⚠️ Payment Intent ID not found in session");
      }

    } catch (Exception e) {
      logger.warning("session " +  e);
    }
  }


  private void processPaymentIntentFailed(Event event) {
    try {
      JsonNode rootNode = parseEventData(event);

      String paymentIntentId = rootNode.path("id").asText();

      if (paymentIntentId != null && !paymentIntentId.isEmpty()) {
        handlePaymentFailure(paymentIntentId);
      } else {
        logger.warning("⚠️ Payment Intent ID not found in failure event");
      }
    } catch (Exception e) {
      logger.warning("payment failure" + e);
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
                      "Cart not found for Stripe session ID: " + sessionId
              ));
      
      cart.setStatut(StatusPayment.CONFIRMED);
      cartRepository.save(cart);
      User user = userRepository.findByCartId(cart.getId())
              .orElseThrow(() -> new ResponseStatusException(
                      HttpStatus.NOT_FOUND,
                      "User not found for cart ID: " + cart.getId()
              ));

      List<Book> books = new ArrayList<>(cart.getBooks());
      System.out.println(books.size() + " books" + books);
      
      for (Book book : books) {

          System.out.println("Processing book id " + book.getId() + " for user id " + user.getId());
          bookLendingService.markAsPending(book.getId(), user.getId());
          System.out.println("Book id " + book.getId() + " successfully processed");
      }
      
    } catch (ResponseStatusException e) {
      logger.warning(e.getMessage());
    }
  }

  @Transactional
  public Session createCheckoutSession(Cart cart, String successUrl) {
    try {
      var cards = cart.getBooks();
      List<SessionCreateParams.LineItem> lineItems = cards.stream()
              .map(book -> SessionCreateParams.LineItem.builder()
                      .setQuantity(1L)
                      .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                              .setCurrency("eur")
                              .setUnitAmount((long) (6 * 100))
                              .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                      .setName(book.getTitle())
                                      .build())
                              .build())
                      .build())
              .collect(Collectors.toList());

      SessionCreateParams params = SessionCreateParams.builder()
              .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
              .setMode(SessionCreateParams.Mode.PAYMENT)
              .setSuccessUrl(successUrl)
              .setCancelUrl(successUrl)
              .addAllLineItem(lineItems)
              .build();

      Session session = Session.create(params);
      cart.setPaymentIntentId(session.getId());
      cartRepository.save(cart);
      return session;
    } catch (Exception e) {
      throw new RuntimeException("Error creating Stripe session", e);
    }
  }
}