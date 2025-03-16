package fr.uge.booqIn.api.controller;

import fr.uge.booqIn.controller.api.CartController;
import fr.uge.booqIn.dto.author.AuthorDto;
import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.cart.CartDto;
import fr.uge.booqIn.dto.cart.ShopCartDto;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookLending;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.interfaces.BookLendingInterface;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import fr.uge.booqIn.service.interfaces.CartServiceInterface;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("CartController Tests")
class CartControllerTest {

    private CartServiceInterface cartService;
    private UserServiceInterface userService;
    private BookServiceInterface bookService;
    private BookLendingInterface bookLendingService;
    private CartController cartController;

    @BeforeEach
    void setUp() {
        cartService = Mockito.mock(CartServiceInterface.class);
        userService = Mockito.mock(UserServiceInterface.class);
        bookService = Mockito.mock(BookServiceInterface.class);
        bookLendingService = Mockito.mock(BookLendingInterface.class);
        cartController = new CartController(cartService, userService, bookService, bookLendingService);
    }

    @Test
    @DisplayName("POST /add-book - Ajoute un livre au panier et retourne succès")
    void addBookToCart_ShouldReturnSuccess() {
        // Given
        Long userId = 1L;
        Long bookId = 1L;
        CartDto cartDto = new CartDto(userId, bookId);
        UserIndex userIndex = new UserIndex(userId, "John", "Doe", "john.doe@example.com", null, UserRole.USER);
        BookShow bookShow = new BookShow(
                bookId,
                Set.of("1234567890"),
                "Test Book",
                Set.of(new AuthorDto(1L, "Author","Name")),
                "1234567890",
                new Date(),
                new Date(),
                new Date(),
                Set.of("Genre")
        );

        when(userService.findUserByIdToIndex(userId)).thenReturn(userIndex);
        when(bookService.findById(bookId)).thenReturn(bookShow);
        when(bookLendingService.reserveBook(bookId, userId)).thenReturn(true);
        when(cartService.addBookToCart(userId, bookId)).thenReturn(true);

        // When
        ResponseEntity<ApiResponse<Boolean>> response = cartController.addBookToCart(cartDto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<Boolean> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Book added to cart", apiResponse.message());
        assertTrue(apiResponse.data());

        verify(userService).findUserByIdToIndex(userId);
        verify(bookService).findById(bookId);
        verify(bookLendingService).reserveBook(bookId, userId);
        verify(cartService).addBookToCart(userId, bookId);
    }

    @Test
    @DisplayName("POST /add-book - Livre déjà dans le panier retourne faux")
    void addBookToCart_BookAlreadyInCart_ShouldReturnFalse() {
        // Given
        Long userId = 1L;
        Long bookId = 1L;
        CartDto cartDto = new CartDto(userId, bookId);
        UserIndex userIndex = new UserIndex(userId, "John", "Doe", "john.doe@example.com", null, UserRole.USER);
        BookShow bookShow = new BookShow(
                bookId,
                Set.of("1234567890"),
                "Test Book",
                Set.of(new AuthorDto(1L, "Author","Name")),
                "1234567890",
                new Date(),
                new Date(),
                new Date(),
                Set.of("Genre")
        );

        when(userService.findUserByIdToIndex(userId)).thenReturn(userIndex);
        when(bookService.findById(bookId)).thenReturn(bookShow);
        when(bookLendingService.reserveBook(bookId, userId)).thenReturn(true);
        when(cartService.addBookToCart(userId, bookId)).thenReturn(false);

        // When
        ResponseEntity<ApiResponse<Boolean>> response = cartController.addBookToCart(cartDto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<Boolean> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Book already in cart", apiResponse.message());
        assertFalse(apiResponse.data());
    }

    @Test
    @DisplayName("POST /add-book - Livre non disponible retourne faux")
    void addBookToCart_BookNotAvailable_ShouldReturnFalse() {
        // Given
        Long userId = 1L;
        Long bookId = 1L;
        CartDto cartDto = new CartDto(userId, bookId);
        UserIndex userIndex = new UserIndex(userId, "John", "Doe", "john.doe@example.com", null, UserRole.USER);
        BookShow bookShow = new BookShow(
                bookId,
                Set.of("1234567890"),
                "Test Book",
                Set.of(new AuthorDto(1L, "Author","Name")),
                "1234567890",
                new Date(),
                new Date(),
                new Date(),
                Set.of("Genre")
        );

        when(userService.findUserByIdToIndex(userId)).thenReturn(userIndex);
        when(bookService.findById(bookId)).thenReturn(bookShow);
        when(bookLendingService.reserveBook(bookId, userId)).thenReturn(false);

        // When
        ResponseEntity<ApiResponse<Boolean>> response = cartController.addBookToCart(cartDto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse<Boolean> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Book not available", apiResponse.message());
        assertFalse(apiResponse.data());

        verify(bookLendingService).reserveBook(bookId, userId);
        verify(cartService, never()).addBookToCart(userId, bookId);
    }

    @Test
    @DisplayName("POST /add-book - Utilisateur ou livre non trouvé retourne erreur")
    void addBookToCart_UserOrBookNotFound_ShouldReturnError() {
        // Given
        Long userId = 1L;
        Long bookId = 1L;
        CartDto cartDto = new CartDto(userId, bookId);

        when(userService.findUserByIdToIndex(userId)).thenReturn(null);
        when(bookService.findById(bookId)).thenReturn(null);

        // When
        ResponseEntity<ApiResponse<Boolean>> response = cartController.addBookToCart(cartDto);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse<Boolean> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.success());
        assertEquals("User or Book not found", apiResponse.message());
        assertFalse(apiResponse.data());

        verify(bookLendingService, never()).reserveBook(anyLong(), anyLong());
        verify(cartService, never()).addBookToCart(anyLong(), anyLong());
    }

    @Test
    @DisplayName("DELETE /remove-book - Supprime un livre du panier et retourne succès")
    void removeBookFromCart_ShouldReturnSuccess() {
        // Given
        Long userId = 1L;
        Long bookId = 1L;
        UserIndex userIndex = new UserIndex(userId, "John", "Doe", "john.doe@example.com", null, UserRole.USER);
        BookShow bookShow = new BookShow(
                bookId,
                Set.of("1234567890"),
                "Test Book",
                Set.of(new AuthorDto(1L, "Author","Name")),
                "1234567890",
                new Date(),
                new Date(),
                new Date(),
                Set.of("Genre")
        );

        when(userService.findUserByIdToIndex(userId)).thenReturn(userIndex);
        when(bookService.findById(bookId)).thenReturn(bookShow);
        when(bookLendingService.cancelBook(bookId, userId)).thenReturn(true);
        when(cartService.removeBookFromCart(userId, bookId)).thenReturn(true);

        // When
        ResponseEntity<ApiResponse<Boolean>> response = cartController.removeBookFromCart(userId, bookId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<Boolean> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Book removed from cart", apiResponse.message());
        assertTrue(apiResponse.data());

        verify(userService).findUserByIdToIndex(userId);
        verify(bookService).findById(bookId);
        verify(bookLendingService).cancelBook(bookId, userId);
        verify(cartService).removeBookFromCart(userId, bookId);
    }

    @Test
    @DisplayName("DELETE /remove-book - Utilisateur ou livre non trouvé retourne erreur")
    void removeBookFromCart_UserOrBookNotFound_ShouldReturnError() {
        // Given
        Long userId = 1L;
        Long bookId = 1L;

        when(userService.findUserByIdToIndex(userId)).thenReturn(null);

        // When
        ResponseEntity<ApiResponse<Boolean>> response = cartController.removeBookFromCart(userId, bookId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        ApiResponse<Boolean> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.success());
        assertEquals("User or Book not found", apiResponse.message());
        assertFalse(apiResponse.data());

        verify(bookLendingService, never()).cancelBook(anyLong(), anyLong());
        verify(cartService, never()).removeBookFromCart(anyLong(), anyLong());
    }

    @Test
    @DisplayName("GET /user-cart/{userId} - Retourne le panier de l'utilisateur")
    void getUserCart_ShouldReturnCart() {
        // Given
        Long userId = 1L;
        UserIndex userIndex = new UserIndex(userId, "John", "Doe", "john.doe@example.com", null, UserRole.USER);
        List<fr.uge.booqIn.dto.book.BookIndex> bookIndexes = List.of(
                new BookIndex(1L, "Book 1", Set.of("Author 1"), new Date()),
                new BookIndex(2L, "Book 2", Set.of("Author 2"), new Date())
        );
        ShopCartDto shopCartDto = new ShopCartDto(1L, userId, bookIndexes);

        when(userService.findUserByIdToIndex(userId)).thenReturn(userIndex);
        when(cartService.getCartDto(userId)).thenReturn(shopCartDto);

        // When
        ResponseEntity<ApiResponse<ShopCartDto>> response = cartController.getUserCart(userId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<ShopCartDto> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Cart retrieved", apiResponse.message());
        assertSame(shopCartDto, apiResponse.data());

        verify(userService).findUserByIdToIndex(userId);
        verify(cartService).getCartDto(userId);
    }

    @Test
    @DisplayName("GET /user-cart/{userId} - Utilisateur non trouvé retourne erreur")
    void getUserCart_UserNotFound_ShouldReturnError() {
        // Given
        Long userId = 1L;

        when(userService.findUserByIdToIndex(userId)).thenReturn(null);

        // When
        ResponseEntity<ApiResponse<ShopCartDto>> response = cartController.getUserCart(userId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse<ShopCartDto> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.success());
        assertEquals("User not found", apiResponse.message());
        assertNull(apiResponse.data());

        verify(cartService, never()).getCartDto(anyLong());
    }

    @Test
    @DisplayName("DELETE /clear-cart/{userId} - Vide le panier et retourne succès")
    void clearCart_ShouldReturnSuccess() {
        // Given
        Long userId = 1L;
        UserIndex userIndex = new UserIndex(userId, "John", "Doe", "john.doe@example.com", null, UserRole.USER);
        List<BookLending> lendings = Collections.emptyList(); // Remplacer par un type approprié si nécessaire

        when(userService.findUserByIdToIndex(userId)).thenReturn(userIndex);
        when(bookLendingService.findUserLendings(userId)).thenReturn(lendings);
        doNothing().when(bookLendingService).ListClearBookstoLend(lendings);
        when(cartService.clearCart(userId)).thenReturn(true);

        // When
        ResponseEntity<ApiResponse<String>> response = cartController.clearCart(userId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<String> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertTrue(apiResponse.success());
        assertEquals("Cart cleared", apiResponse.message());
        assertEquals("Cart has been emptied successfully", apiResponse.data());

        verify(userService).findUserByIdToIndex(userId);
        verify(bookLendingService).findUserLendings(userId);
        verify(bookLendingService).ListClearBookstoLend(lendings);
        verify(cartService).clearCart(userId);
    }

    @Test
    @DisplayName("DELETE /clear-cart/{userId} - Utilisateur non trouvé retourne erreur")
    void clearCart_UserNotFound_ShouldReturnError() {
        // Given
        Long userId = 1L;

        when(userService.findUserByIdToIndex(userId)).thenReturn(null);

        // When
        ResponseEntity<ApiResponse<String>> response = cartController.clearCart(userId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse<String> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.success());
        assertEquals("User not found", apiResponse.message());
        assertNull(apiResponse.data());

        verify(bookLendingService, never()).findUserLendings(anyLong());
        verify(bookLendingService, never()).ListClearBookstoLend(anyList());
        verify(cartService, never()).clearCart(anyLong());
    }

    @Test
    @DisplayName("DELETE /clear-cart/{userId} - Échec de vidage du panier retourne erreur")
    void clearCart_FailureToClear_ShouldReturnError() {
        // Given
        Long userId = 1L;
        UserIndex userIndex = new UserIndex(userId, "John", "Doe", "john.doe@example.com", null, UserRole.USER);
        List<BookLending> lendings = Collections.emptyList();

        when(userService.findUserByIdToIndex(userId)).thenReturn(userIndex);
        when(bookLendingService.findUserLendings(userId)).thenReturn(lendings);
        doNothing().when(bookLendingService).ListClearBookstoLend(lendings);
        when(cartService.clearCart(userId)).thenReturn(false);

        // When
        ResponseEntity<ApiResponse<String>> response = cartController.clearCart(userId);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiResponse<String> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertFalse(apiResponse.success());
        assertEquals("Cannot clear a completed cart", apiResponse.message());
        assertNull(apiResponse.data());

        verify(userService).findUserByIdToIndex(userId);
        verify(bookLendingService).findUserLendings(userId);
        verify(bookLendingService).ListClearBookstoLend(lendings);
        verify(cartService).clearCart(userId);
    }
}