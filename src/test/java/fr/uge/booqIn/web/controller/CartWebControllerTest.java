package fr.uge.booqIn.web.controller;

import fr.uge.booqIn.controller.web.CartWebController;
import fr.uge.booqIn.controller.web.HomeWebController;
import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.cart.ShopCartDto;
import fr.uge.booqIn.entity.BookLending;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.service.impl.CartServiceImpl;
import fr.uge.booqIn.service.impl.UserServiceImpl;
import fr.uge.booqIn.service.interfaces.BookLendingInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("CartWebController Tests")
class CartWebControllerTest {

  @Mock
  private CartServiceImpl cartService;

  @Mock
  private UserServiceImpl userService;

  @Mock
  private HomeWebController homeWebController;

  @Mock
  private BookLendingInterface bookLendingInterface;

  @Mock
  private Model model;

  @Mock
  private Principal principal;

  @Mock
  private RedirectAttributes redirectAttributes;

  @Mock
  private User currentUser;

  @InjectMocks
  private CartWebController cartWebController;

  private ShopCartDto testShopCartDto;
  private List<BookIndex> testBooks;
  private List<BookLending> testBookLendings;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    // Setup test data
    testBooks = new ArrayList<>();
    testBooks.add(new BookIndex(1L, "Test Book 1", Collections.singleton("Author 1"), new Date()));
    testBooks.add(new BookIndex(2L, "Test Book 2", Collections.singleton("Author 2"), new Date()));

    testShopCartDto = new ShopCartDto(1L, 1L, testBooks);

    testBookLendings = new ArrayList<>();
    testBookLendings.add(mock(BookLending.class));

    // Common mock setup
    when(currentUser.getId()).thenReturn(1L);
    doNothing().when(homeWebController).prepareModel(any(Model.class), any(Principal.class), anyString());
  }

  @Test
  @DisplayName("viewCart - Should display cart for authenticated user")
  void viewCart_AuthenticatedUser_ShouldDisplayCart() {
    // Given
    when(cartService.getCartDto(eq(1L))).thenReturn(testShopCartDto);

    // When
    String viewName = cartWebController.viewCart(currentUser, model, principal);

    // Then
    assertEquals("layout", viewName);

    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("cart"));
    verify(cartService).getCartDto(eq(1L));
    verify(model).addAttribute(eq("cart"), eq(testShopCartDto));
    verify(model).addAttribute(eq("subtotal"), eq(0.99));
    verify(model).addAttribute(eq("total"), eq(6.98)); // 0.99 + 5.99 shipping
    verify(model).addAttribute(eq("title"), eq("BooqIn - Votre Panier"));
    verify(model).addAttribute(eq("content"), eq("pages/shoppingCart/shoppingCart"));
  }

  @Test
  @DisplayName("viewCart - Should handle empty cart")
  void viewCart_EmptyCart_ShouldDisplayEmptyCart() {
    // Given
    when(cartService.getCartDto(eq(1L))).thenReturn(null);

    // When
    String viewName = cartWebController.viewCart(currentUser, model, principal);

    // Then
    assertEquals("layout", viewName);

    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("cart"));
    verify(cartService).getCartDto(eq(1L));
    verify(model).addAttribute(eq("cart"), eq(null));
    verify(model).addAttribute(eq("subtotal"), eq(0.0));
    verify(model).addAttribute(eq("total"), eq(5.99)); // Just shipping
    verify(model).addAttribute(eq("title"), eq("BooqIn - Votre Panier"));
    verify(model).addAttribute(eq("content"), eq("pages/shoppingCart/shoppingCart"));
  }

  @Test
  @DisplayName("viewCart - Should handle unauthenticated user")
  void viewCart_UnauthenticatedUser_ShouldShowError() {
    // Given
    when(currentUser.getId()).thenReturn(null);

    // When
    String viewName = cartWebController.viewCart(currentUser, model, principal);

    // Then
    assertEquals("layout", viewName);

    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("cart"));
    verify(model).addAttribute(eq("error"), anyString());
    verify(model).addAttribute(eq("title"), eq("BooqIn - Erreur"));
    verify(model).addAttribute(eq("content"), eq("pages/error"));
    verifyNoInteractions(cartService);
  }

  @Test
  @DisplayName("removeBookFromCart - Should remove book and redirect")
  void removeBookFromCart_ShouldRemoveBookAndRedirect() {
    // Given
    Long userId = 1L;
    Long bookId = 1L;
    when(bookLendingInterface.cancelBook(eq(bookId), eq(userId))).thenReturn(true);
    when(cartService.removeBookFromCart(eq(userId), eq(bookId))).thenReturn(true);

    // When
    String viewName = cartWebController.removeBookFromCart(userId, bookId, redirectAttributes);

    // Then
    assertEquals("redirect:/cart?userId=" + userId, viewName);

    verify(bookLendingInterface).cancelBook(eq(bookId), eq(userId));
    verify(cartService).removeBookFromCart(eq(userId), eq(bookId));
    verifyNoInteractions(redirectAttributes); // No flash attributes when successful
  }

  @Test
  @DisplayName("removeBookFromCart - Should handle failure")
  void removeBookFromCart_Failure_ShouldRedirectWithError() {
    // Given
    Long userId = 1L;
    Long bookId = 1L;
    when(bookLendingInterface.cancelBook(eq(bookId), eq(userId))).thenReturn(true);
    when(cartService.removeBookFromCart(eq(userId), eq(bookId))).thenReturn(false);

    // When
    String viewName = cartWebController.removeBookFromCart(userId, bookId, redirectAttributes);

    // Then
    assertEquals("redirect:/cart?userId=" + userId, viewName);

    verify(bookLendingInterface).cancelBook(eq(bookId), eq(userId));
    verify(cartService).removeBookFromCart(eq(userId), eq(bookId));
    verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
  }

  @Test
  @DisplayName("clearCart - Should clear cart and redirect")
  void clearCart_ShouldClearCartAndRedirect() {
    // Given
    Long userId = 1L;
    when(bookLendingInterface.findUserLendings(eq(userId))).thenReturn(testBookLendings);
    doNothing().when(bookLendingInterface).ListClearBookstoLend(eq(testBookLendings));
    when(cartService.clearCart(eq(userId))).thenReturn(true);

    // When
    String viewName = cartWebController.clearCart(userId, redirectAttributes);

    // Then
    assertEquals("redirect:/cart?userId=" + userId, viewName);

    verify(bookLendingInterface).findUserLendings(eq(userId));
    verify(bookLendingInterface).ListClearBookstoLend(eq(testBookLendings));
    verify(cartService).clearCart(eq(userId));
    verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
  }

  @Test
  @DisplayName("clearCart - Should handle failure")
  void clearCart_Failure_ShouldRedirectWithError() {
    // Given
    Long userId = 1L;
    when(bookLendingInterface.findUserLendings(eq(userId))).thenReturn(testBookLendings);
    doNothing().when(bookLendingInterface).ListClearBookstoLend(eq(testBookLendings));
    when(cartService.clearCart(eq(userId))).thenReturn(false);

    // When
    String viewName = cartWebController.clearCart(userId, redirectAttributes);

    // Then
    assertEquals("redirect:/cart?userId=" + userId, viewName);

    verify(bookLendingInterface).findUserLendings(eq(userId));
    verify(bookLendingInterface).ListClearBookstoLend(eq(testBookLendings));
    verify(cartService).clearCart(eq(userId));
    verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
  }

  @Test
  @DisplayName("addBookToCart - Should add book to cart and redirect")
  void addBookToCart_ShouldAddBookAndRedirect() {
    // Given
    Long userId = 1L;
    Long bookId = 1L;
    when(bookLendingInterface.reserveBook(eq(bookId), eq(userId))).thenReturn(true);
    when(cartService.addBookToCart(eq(userId), eq(bookId))).thenReturn(true);

    // When
    String viewName = cartWebController.addBookToCart(userId, bookId, redirectAttributes);

    // Then
    assertEquals("redirect:/cart?userId=" + userId, viewName);

    verify(bookLendingInterface).reserveBook(eq(bookId), eq(userId));
    verify(cartService).addBookToCart(eq(userId), eq(bookId));
    verify(redirectAttributes).addFlashAttribute(eq("success"), anyString());
  }

  @Test
  @DisplayName("addBookToCart - Should handle lending failure")
  void addBookToCart_LendingFailure_ShouldRedirectWithError() {
    // Given
    Long userId = 1L;
    Long bookId = 1L;
    when(bookLendingInterface.reserveBook(eq(bookId), eq(userId))).thenReturn(false);

    // When
    String viewName = cartWebController.addBookToCart(userId, bookId, redirectAttributes);

    // Then
    assertEquals("redirect:/cart?userId=" + userId, viewName);

    verify(bookLendingInterface).reserveBook(eq(bookId), eq(userId));
    verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
    verify(cartService, never()).addBookToCart(anyLong(), anyLong());
  }

  @Test
  @DisplayName("addBookToCart - Should handle cart service failure")
  void addBookToCart_CartServiceFailure_ShouldRedirectWithError() {
    // Given
    Long userId = 1L;
    Long bookId = 1L;
    when(bookLendingInterface.reserveBook(eq(bookId), eq(userId))).thenReturn(true);
    when(cartService.addBookToCart(eq(userId), eq(bookId))).thenReturn(false);

    // When
    String viewName = cartWebController.addBookToCart(userId, bookId, redirectAttributes);

    // Then
    assertEquals("redirect:/cart?userId=" + userId, viewName);

    verify(bookLendingInterface).reserveBook(eq(bookId), eq(userId));
    verify(cartService).addBookToCart(eq(userId), eq(bookId));
    verify(redirectAttributes).addFlashAttribute(eq("error"), anyString());
  }
}