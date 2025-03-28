package fr.uge.booqIn.api.controller;

import com.stripe.model.checkout.Session;
import fr.uge.booqIn.controller.api.StripeController;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.StripeService;
import fr.uge.booqIn.service.interfaces.CartServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("StripeController Tests")
class StripeControllerTest {

  private StripeService stripeService;
  private CartServiceInterface cartService;
  private StripeController stripeController;

  @BeforeEach
  void setUp() {
    stripeService = Mockito.mock(StripeService.class);
    cartService = Mockito.mock(CartServiceInterface.class);
    stripeController = new StripeController(stripeService, cartService);
  }

  @Test
  @DisplayName("POST /checkout - Should create checkout session and return session URL")
  void createCheckoutSession_ShouldReturnCheckoutUrl() {
    // Given
    Long cartId = 1L;
    Cart cart = new Cart();
    cart.setId(cartId);

    Book book1 = new Book.Builder().id(1L).title("Test Book 1").build();
    Book book2 = new Book.Builder().id(2L).title("Test Book 2").build();

    Set<Book> books = new HashSet<>();
    books.add(book1);
    books.add(book2);
    cart.setBooks(books);
    var url = "http://localhost:5173/";
    Session stripeSession = new Session();
    stripeSession.setUrl("https://checkout.stripe.com/test-session");

    when(cartService.getCart(eq(cartId))).thenReturn(cart);
    when(stripeService.createCheckoutSession(any(Cart.class), any(String.class))).thenReturn(stripeSession);
    // When
    ResponseEntity<ApiResponse<String>> response = stripeController.createCheckoutSession(cartId , "");

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<String> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Session Stripe créée", apiResponse.message());
    assertEquals("https://checkout.stripe.com/test-session", apiResponse.data());

    verify(cartService).getCart(eq(cartId));
    verify(stripeService).createCheckoutSession(eq(cart) ,eq(url));
  }

  @Test
  @DisplayName("POST /checkout - Should return error when cart is empty")
  void createCheckoutSession_ShouldReturnError_WhenCartIsEmpty() {
    // Given
    Long cartId = 1L;
    Cart emptyCart = new Cart();
    emptyCart.setId(cartId);
    emptyCart.setBooks(new HashSet<>()); // Empty books set

    when(cartService.getCart(eq(cartId))).thenReturn(emptyCart);

    // When
    ResponseEntity<ApiResponse<String>> response = stripeController.createCheckoutSession(cartId , "");

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ApiResponse<String> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertFalse(apiResponse.success());
    assertEquals("Le panier est vide ou inexistant", apiResponse.message());
    assertNull(apiResponse.data());

    verify(cartService).getCart(eq(cartId));
    verify(stripeService, never()).createCheckoutSession(any(Cart.class) , any(String.class));
  }

  @Test
  @DisplayName("POST /checkout - Should return error when cart doesn't exist")
  void createCheckoutSession_ShouldReturnError_WhenCartDoesNotExist() {
    // Given
    Long cartId = 999L;

    when(cartService.getCart(eq(cartId))).thenReturn(null);

    // When
    ResponseEntity<ApiResponse<String>> response = stripeController.createCheckoutSession(cartId , "");

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ApiResponse<String> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertFalse(apiResponse.success());
    assertEquals("Le panier est vide ou inexistant", apiResponse.message());
    assertNull(apiResponse.data());

    verify(cartService).getCart(eq(cartId));
    verify(stripeService, never()).createCheckoutSession(any(Cart.class) , any(String.class));
  }
}