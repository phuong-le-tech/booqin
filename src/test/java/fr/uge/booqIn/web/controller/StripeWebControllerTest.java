package fr.uge.booqIn.web.controller;

import com.stripe.model.checkout.Session;
import fr.uge.booqIn.controller.web.StripeWebController;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.service.StripeService;
import fr.uge.booqIn.service.interfaces.CartServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("StripeWebController Tests")
class StripeWebControllerTest {

  @Mock
  private StripeService stripeService;

  @Mock
  private CartServiceInterface cartService;

  @Mock
  private Model model;

  @Mock
  private Session stripeSession;

  @InjectMocks
  private StripeWebController stripeWebController;

  private Cart testCart;
  private Set<Book> testBooks;
  private String url = "http://localhost:8080/";

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    // Setup test data
    testBooks = new HashSet<>();
    testBooks.add(mock(Book.class));
    testBooks.add(mock(Book.class));

    testCart = new Cart();
    testCart.setId(1L);
    testCart.setBooks(testBooks);

    // Common mock setup
    when(stripeSession.getUrl()).thenReturn("https://stripe.com/checkout/session/123");
  }

  @Test
  @DisplayName("createCheckoutSession - Should create session and redirect when cart exists with books")
  void createCheckoutSession_WithValidCart_ShouldCreateSessionAndRedirect() {
    // Given
    Long cartId = 1L;
    when(cartService.getCart(eq(cartId))).thenReturn(testCart);
    when(stripeService.createCheckoutSession(eq(testCart) , eq(url))).thenReturn(stripeSession);

    // When
    String viewName = stripeWebController.createCheckoutSession(cartId, model);

    // Then
    assertEquals("redirect:https://stripe.com/checkout/session/123", viewName);

    verify(cartService).getCart(eq(cartId));
    verify(stripeService).createCheckoutSession(eq(testCart) , eq(url));
    verify(model).addAttribute(eq("stripeUrl"), eq("https://stripe.com/checkout/session/123"));
  }

  @Test
  @DisplayName("createCheckoutSession - Should show error when cart doesn't exist")
  void createCheckoutSession_WithNonExistentCart_ShouldShowError() {
    // Given
    Long cartId = 999L;
    when(cartService.getCart(eq(cartId))).thenReturn(null);

    // When
    String viewName = stripeWebController.createCheckoutSession(cartId, model);

    // Then
    assertEquals("cart", viewName);

    verify(cartService).getCart(eq(cartId));
    verify(model).addAttribute(eq("error"), anyString());
    verifyNoInteractions(stripeService);
  }

  @Test
  @DisplayName("createCheckoutSession - Should show error when cart is empty")
  void createCheckoutSession_WithEmptyCart_ShouldShowError() {
    // Given
    Long cartId = 1L;
    Cart emptyCart = new Cart();
    emptyCart.setId(cartId);
    emptyCart.setBooks(new HashSet<>());

    when(cartService.getCart(eq(cartId))).thenReturn(emptyCart);

    // When
    String viewName = stripeWebController.createCheckoutSession(cartId, model);

    // Then
    assertEquals("cart", viewName);

    verify(cartService).getCart(eq(cartId));
    verify(model).addAttribute(eq("error"), anyString());
    verifyNoInteractions(stripeService);
  }
}