package fr.uge.booqIn.service;

import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.cart.ShopCartDto;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.StatusPayment;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.repository.CartRepository;
import fr.uge.booqIn.service.impl.CartServiceImpl;
import fr.uge.booqIn.service.interfaces.CartServiceInterface;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("CartService Tests")
class CartServiceTest {

  private CartRepository cartRepository;
  private BookRepository bookRepository;
  private UserServiceInterface userService;
  private CartServiceInterface cartService;
  private Cart testCart;
  private User testUser;
  private Book testBook;

  @BeforeEach
  void setUp() {
    cartRepository = Mockito.mock(CartRepository.class);
    bookRepository = Mockito.mock(BookRepository.class);
    userService = Mockito.mock(UserServiceInterface.class);

    cartService = new CartServiceImpl(cartRepository, bookRepository, userService);

    // Setup test data
    testUser = new User();
    testUser.setId(1L);

    testCart = new Cart();
    testCart.setId(1L);
    testCart.setStatut(StatusPayment.PENDING);
    testCart.setBooks(new HashSet<>());

    testUser.setCart(testCart);

    testBook = new Book();
    testBook.setId(1L);
    testBook.setTitle("Test Book");
    testBook.setAuthors(new HashSet<>());
  }

  @Test
  @DisplayName("getCart - Should return cart when it exists")
  void getCart_WithExistingId_ShouldReturnCart() {
    // Given
    Long userId = testUser.getId();

    when(userService.findUserById(eq(userId))).thenReturn(testUser);
    when(cartRepository.findById(eq(testCart.getId()))).thenReturn(Optional.of(testCart));

    // When
    Cart result = cartService.getCart(userId);

    // Then
    assertNotNull(result);
    assertEquals(testCart.getId(), result.getId());
    assertEquals(testCart.getStatus(), result.getStatus());

    verify(userService).findUserById(eq(userId));
    verify(cartRepository).findById(eq(testCart.getId()));
  }

  @Test
  @DisplayName("getCart - Should throw ResourceNotFoundException when cart doesn't exist")
  void getCart_WithNonExistingCart_ShouldThrowException() {
    // Given
    Long userId = testUser.getId();

    when(userService.findUserById(eq(userId))).thenReturn(testUser);
    when(cartRepository.findById(eq(testCart.getId()))).thenReturn(Optional.empty());

    // When & Then
    assertThrows(ResourceNotFoundException.class, () ->
            cartService.getCart(userId)
    );

    verify(userService).findUserById(eq(userId));
    verify(cartRepository).findById(eq(testCart.getId()));
  }

  @Test
  @DisplayName("updateCartStatus - Should update cart status")
  void updateCartStatus_ShouldUpdateStatus() {
    // Given
    Long userId = testUser.getId();
    StatusPayment newStatus = StatusPayment.CONFIRMED;

    when(userService.findUserById(eq(userId))).thenReturn(testUser);
    when(cartRepository.findById(eq(testCart.getId()))).thenReturn(Optional.of(testCart));
    when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

    // When
    Cart result = cartService.updateCartStatus(userId, newStatus);

    // Then
    assertNotNull(result);
    assertEquals(newStatus, testCart.getStatus());

    verify(userService).findUserById(eq(userId));
    verify(cartRepository).findById(eq(testCart.getId()));
    verify(cartRepository).save(eq(testCart));

    ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
    verify(cartRepository).save(cartCaptor.capture());
    Cart capturedCart = cartCaptor.getValue();
    assertEquals(newStatus, capturedCart.getStatus());
  }

  @Test
  @DisplayName("addBookToCart - Should add book to cart when book not in cart")
  void addBookToCart_WithNewBook_ShouldAddBookAndReturnTrue() {
    // Given
    Long userId = testUser.getId();
    Long bookId = testBook.getId();

    when(userService.findUserById(eq(userId))).thenReturn(testUser);
    when(cartRepository.findById(eq(testCart.getId()))).thenReturn(Optional.of(testCart));
    when(bookRepository.findById(eq(bookId))).thenReturn(Optional.of(testBook));
    when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

    // When
    boolean result = cartService.addBookToCart(userId, bookId);

    // Then
    assertTrue(result);
    assertTrue(testCart.getBooks().contains(testBook));

    verify(userService).findUserById(eq(userId));
    verify(cartRepository).findById(eq(testCart.getId()));
    verify(bookRepository).findById(eq(bookId));
    verify(cartRepository).save(eq(testCart));
  }

  @Test
  @DisplayName("addBookToCart - Should return false when book already in cart")
  void addBookToCart_WithExistingBook_ShouldReturnFalse() {
    // Given
    Long userId = testUser.getId();
    Long bookId = testBook.getId();

    testCart.getBooks().add(testBook);

    when(userService.findUserById(eq(userId))).thenReturn(testUser);
    when(cartRepository.findById(eq(testCart.getId()))).thenReturn(Optional.of(testCart));
    when(bookRepository.findById(eq(bookId))).thenReturn(Optional.of(testBook));

    // When
    boolean result = cartService.addBookToCart(userId, bookId);

    // Then
    assertFalse(result);
    verify(userService).findUserById(eq(userId));
    verify(cartRepository).findById(eq(testCart.getId()));
    verify(bookRepository).findById(eq(bookId));
    verify(cartRepository, never()).save(any(Cart.class));
  }

  @Test
  @DisplayName("removeBookFromCart - Should remove book from cart")
  void removeBookFromCart_ShouldRemoveBookAndReturnTrue() {
    // Given
    Long userId = testUser.getId();
    Long bookId = testBook.getId();

    when(userService.findUserById(eq(userId))).thenReturn(testUser);
    when(cartRepository.findById(eq(testCart.getId()))).thenReturn(Optional.of(testCart));
    doNothing().when(cartRepository).deleteBookFromCart(eq(userId), eq(bookId));

    // When
    boolean result = cartService.removeBookFromCart(userId, bookId);

    // Then
    assertTrue(result);
    verify(cartRepository).deleteBookFromCart(eq(userId), eq(bookId));
  }

  @Test
  @DisplayName("getCartDto - Should return cart DTO")
  void getCartDto_ShouldReturnCartDto() {
    // Given
    Long userId = testUser.getId();
    Set<Book> books = new HashSet<>();
    books.add(testBook);
    testCart.setBooks(books);

    when(userService.findUserById(eq(userId))).thenReturn(testUser);
    when(cartRepository.findById(eq(testCart.getId()))).thenReturn(Optional.of(testCart));

    // When
    ShopCartDto result = cartService.getCartDto(userId);

    // Then
    assertNotNull(result);
    assertEquals(testCart.getId(), result.id());
    assertEquals(userId, result.userId());
    assertEquals(1, result.books().size());

    BookIndex bookIndex = result.books().get(0);
    assertEquals(testBook.getId(), bookIndex.id());
    assertEquals(testBook.getTitle(), bookIndex.title());

    verify(userService).findUserById(eq(userId));
    verify(cartRepository).findById(eq(testCart.getId()));
  }

  @Test
  @DisplayName("clearCart - Should clear cart and return true")
  void clearCart_ShouldClearCartAndReturnTrue() {
    // Given
    Long userId = testUser.getId();
    Set<Book> books = new HashSet<>();
    books.add(testBook);
    testCart.setBooks(books);

    when(userService.findUserById(eq(userId))).thenReturn(testUser);
    when(cartRepository.findById(eq(testCart.getId()))).thenReturn(Optional.of(testCart));
    when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

    // When
    boolean result = cartService.clearCart(userId);

    // Then
    assertTrue(result);
    assertTrue(testCart.getBooks().isEmpty());
    assertEquals(StatusPayment.PENDING, testCart.getStatus());

    verify(userService).findUserById(eq(userId));
    verify(cartRepository).findById(eq(testCart.getId()));
    verify(cartRepository).save(eq(testCart));

    ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
    verify(cartRepository).save(cartCaptor.capture());
    Cart capturedCart = cartCaptor.getValue();
    assertTrue(capturedCart.getBooks().isEmpty());
    assertEquals(StatusPayment.PENDING, capturedCart.getStatus());
  }

  @Test
  @DisplayName("clearCartAfterPayment - Should clear cart after payment")
  void clearCartAfterPayment_ShouldClearCart() {
    // Given
    Long userId = testUser.getId();
    Set<Book> books = new HashSet<>();
    books.add(testBook);
    testCart.setBooks(books);
    testCart.setStatut(StatusPayment.CONFIRMED);

    when(userService.findUserById(eq(userId))).thenReturn(testUser);
    when(cartRepository.findById(eq(testCart.getId()))).thenReturn(Optional.of(testCart));
    when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

    // When
    cartService.clearCartAfterPayment(userId);

    // Then
    assertTrue(testCart.getBooks().isEmpty());
    assertEquals(StatusPayment.PENDING, testCart.getStatus());

    verify(userService).findUserById(eq(userId));
    verify(cartRepository).findById(eq(testCart.getId()));
    verify(cartRepository).save(eq(testCart));
  }
}