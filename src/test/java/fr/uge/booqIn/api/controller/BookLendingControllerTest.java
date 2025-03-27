package fr.uge.booqIn.api.controller;

import fr.uge.booqIn.controller.api.BookLendingController;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.lending.BookLendingShow;
import fr.uge.booqIn.dto.lending.LendingResponseDTO;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.entity.BookLending;
import fr.uge.booqIn.enums.BookStatus;
import fr.uge.booqIn.enums.LendStatus;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.interfaces.BookLendingInterface;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("BookLendingController Tests")
class BookLendingControllerTest {

  private UserServiceInterface userService;
  private BookServiceInterface bookService;
  private BookLendingInterface bookLendingService;
  private BookLendingController bookLendingController;
  private HttpServletResponse httpServletResponse;

  @BeforeEach
  void setUp() {
    userService = Mockito.mock(UserServiceInterface.class);
    bookService = Mockito.mock(BookServiceInterface.class);
    bookLendingService = Mockito.mock(BookLendingInterface.class);
    bookLendingController = new BookLendingController(userService, bookService, bookLendingService);
    httpServletResponse = Mockito.mock(HttpServletResponse.class);
  }

  @Test
  @DisplayName("GET /confirm-loan - Should confirm loan and return success")
  void confirmLoan_ShouldReturnSuccess() {
    // Given
    Long lendingId = 1L;
    when(bookLendingService.confirmLoan(eq(lendingId))).thenReturn(true);

    // When
    ResponseEntity<ApiResponse<Boolean>> response = bookLendingController.confirmLoan(lendingId);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<Boolean> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Loan confirmed", apiResponse.message());
    assertTrue(apiResponse.data());
    verify(bookLendingService).confirmLoan(eq(lendingId));
  }

  @Test
  @DisplayName("GET /confirm-loan - Should return error when confirmation fails")
  void confirmLoan_ShouldReturnError_WhenConfirmationFails() {
    // Given
    Long lendingId = 1L;
    when(bookLendingService.confirmLoan(eq(lendingId))).thenReturn(false);

    // When
    ResponseEntity<ApiResponse<Boolean>> response = bookLendingController.confirmLoan(lendingId);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ApiResponse<Boolean> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertFalse(apiResponse.success());
    assertEquals("Loan not confirmed", apiResponse.message());
    assertFalse(apiResponse.data());
    verify(bookLendingService).confirmLoan(eq(lendingId));
  }

  @Test
  @DisplayName("GET /ship-book - Should mark book as shipped and return success")
  void shipBook_ShouldReturnSuccess() {
    // Given
    Long lendingId = 1L;
    when(bookLendingService.markAsShipped(eq(lendingId))).thenReturn(true);

    // When
    ResponseEntity<ApiResponse<Boolean>> response = bookLendingController.shipBook(lendingId);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<Boolean> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Book shipped", apiResponse.message());
    assertTrue(apiResponse.data());
    verify(bookLendingService).markAsShipped(eq(lendingId));
  }

  @Test
  @DisplayName("GET /ship-book - Should return error when shipping fails")
  void shipBook_ShouldReturnError_WhenShippingFails() {
    // Given
    Long lendingId = 1L;
    when(bookLendingService.markAsShipped(eq(lendingId))).thenReturn(false);

    // When
    ResponseEntity<ApiResponse<Boolean>> response = bookLendingController.shipBook(lendingId);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ApiResponse<Boolean> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertFalse(apiResponse.success());
    assertEquals("Book not shipped", apiResponse.message());
    assertFalse(apiResponse.data());
    verify(bookLendingService).markAsShipped(eq(lendingId));
  }

  @Test
  @DisplayName("GET /return-book - Should mark book as returned and return success")
  void returnBook_ShouldReturnSuccess() {
    // Given
    Long lendingId = 1L;
    when(bookLendingService.returnBook(eq(lendingId))).thenReturn(true);

    // When
    ResponseEntity<ApiResponse<Boolean>> response = bookLendingController.returnBook(lendingId);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<Boolean> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Book returned", apiResponse.message());
    assertTrue(apiResponse.data());
    verify(bookLendingService).returnBook(eq(lendingId));
  }

  @Test
  @DisplayName("GET /return-book - Should return error when return fails")
  void returnBook_ShouldReturnError_WhenReturnFails() {
    // Given
    Long lendingId = 1L;
    when(bookLendingService.returnBook(eq(lendingId))).thenReturn(false);

    // When
    ResponseEntity<ApiResponse<Boolean>> response = bookLendingController.returnBook(lendingId);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ApiResponse<Boolean> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertFalse(apiResponse.success());
    assertEquals("Book not returned", apiResponse.message());
    assertFalse(apiResponse.data());
    verify(bookLendingService).returnBook(eq(lendingId));
  }

  @Test
  @DisplayName("GET /user/{userId} - Should return user lendings for type 'borrowed'")
  void getUserLendings_ShouldReturnUserBorrowedLendings() {
    // Given
    Long userId = 1L;
    LendStatus type = LendStatus.BORROWED;

    LocalDateTime now = LocalDateTime.now();
    UserIndex borrower = new UserIndex(userId, "John", "Doe", "john.doe@example.com", null, UserRole.USER);
    UserIndex lender = new UserIndex(2L, "Jane", "Smith", "jane.smith@example.com", null, UserRole.USER);

    // Create BookShow for tests
    BookShow bookShow = new BookShow(
            1L,
            Set.of("1234567890"),
            "Test Book",
            new HashSet<>(),
            "Test description",
            null,
            now,
            now,
            Set.of("Fiction"),
            "",
            ""
    );

    BookLendingShow lending1 = new BookLendingShow(
            1L,
            bookShow,
            borrower,
            lender,
            BookStatus.BORROWED,
            now.minusDays(7),
            now.minusDays(5),
            now.plusDays(9),
            null
    );

    BookLendingShow lending2 = new BookLendingShow(
            2L,
            bookShow,
            borrower,
            lender,
            BookStatus.SHIPPED,
            now.minusDays(14),
            now.minusDays(12),
            now.plusDays(2),
            null
    );

    List<BookLendingShow> borrowedBooks = List.of(lending1, lending2);
    List<BookLendingShow> lentBooks = new ArrayList<>();
    LendingResponseDTO responseDTO = new LendingResponseDTO(borrowedBooks, lentBooks, false);

    when(bookLendingService.findAllUserLendingsAsDTO(eq(userId), (eq(type)))).thenReturn(responseDTO);

    // When
    ResponseEntity<ApiResponse<LendingResponseDTO>> response = bookLendingController.getUserLendings(userId, type);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<LendingResponseDTO> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Borrowed books retrieved successfully", apiResponse.message());
    assertEquals(responseDTO, apiResponse.data());
    verify(bookLendingService).findAllUserLendingsAsDTO(eq(userId), (eq(type)));
  }

  @Test
  @DisplayName("GET /user/{userId} - Should return user lendings for type 'lent'")
  void getUserLendings_ShouldReturnUserLentLendings() {
    // Given
    Long userId = 1L;
    LendStatus type = LendStatus.LENT;

    LocalDateTime now = LocalDateTime.now();
    UserIndex borrower = new UserIndex(2L, "Jane", "Smith", "jane.smith@example.com", null, UserRole.USER);
    UserIndex lender = new UserIndex(userId, "John", "Doe", "john.doe@example.com", null, UserRole.USER);

    // Create BookShow for tests
    BookShow bookShow = new BookShow(
            1L,
            Set.of("1234567890"),
            "Test Book",
            new HashSet<>(),
            "Test description",
            null,
            now,
            now,
            Set.of("Fiction"),
            "",
            ""
    );

    BookLendingShow lending1 = new BookLendingShow(
            1L,
            bookShow,
            borrower,
            lender,
            BookStatus.BORROWED,
            now.minusDays(7),
            now.minusDays(5),
            now.plusDays(9),
            null
    );

    BookLendingShow lending2 = new BookLendingShow(
            2L,
            bookShow,
            borrower,
            lender,
            BookStatus.SHIPPED,
            now.minusDays(14),
            now.minusDays(12),
            now.plusDays(2),
            null
    );

    List<BookLendingShow> borrowedBooks = new ArrayList<>();
    List<BookLendingShow> lentBooks = List.of(lending1, lending2);
    LendingResponseDTO responseDTO = new LendingResponseDTO(borrowedBooks, lentBooks, false);

    when(bookLendingService.findAllUserLendingsAsDTO(eq(userId), (eq(type)))).thenReturn(responseDTO);

    // When
    ResponseEntity<ApiResponse<LendingResponseDTO>> response = bookLendingController.getUserLendings(userId, type);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<LendingResponseDTO> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Lent books retrieved successfully", apiResponse.message());
    assertEquals(responseDTO, apiResponse.data());
    verify(bookLendingService).findAllUserLendingsAsDTO(eq(userId), eq(type));
  }

  @Test
  @DisplayName("GET /user/{userId} - Should return user lendings for type 'all'")
  void getUserLendings_ShouldReturnAllUserLendings() {
    // Given
    Long userId = 1L;
    LendStatus type = LendStatus.ALL;

    LocalDateTime now = LocalDateTime.now();
    UserIndex user = new UserIndex(userId, "John", "Doe", "john.doe@example.com", null, UserRole.USER);
    UserIndex otherUser = new UserIndex(2L, "Jane", "Smith", "jane.smith@example.com", null, UserRole.USER);

    // Create BookShow for tests
    BookShow bookShow = new BookShow(
            1L,
            Set.of("1234567890"),
            "Test Book",
            new HashSet<>(),
            "Test description",
            null,
            now,
            now,
            Set.of("Fiction"),
            "",
            ""
    );

    BookLendingShow borrowedLending = new BookLendingShow(
            1L,
            bookShow,
            user,
            otherUser,
            BookStatus.BORROWED,
            now.minusDays(7),
            now.minusDays(5),
            now.plusDays(9),
            null
    );

    BookLendingShow lentLending = new BookLendingShow(
            2L,
            bookShow,
            otherUser,
            user,
            BookStatus.SHIPPED,
            now.minusDays(14),
            now.minusDays(12),
            now.plusDays(2),
            null
    );

    List<BookLendingShow> borrowedBooks = List.of(borrowedLending);
    List<BookLendingShow> lentBooks = List.of(lentLending);
    LendingResponseDTO responseDTO = new LendingResponseDTO(borrowedBooks, lentBooks, true);

    when(bookLendingService.findAllUserLendingsAsDTO(eq(userId), eq(type))).thenReturn(responseDTO);

    // When
    ResponseEntity<ApiResponse<LendingResponseDTO>> response = bookLendingController.getUserLendings(userId, type);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<LendingResponseDTO> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("All user book lendings retrieved", apiResponse.message());
    assertEquals(responseDTO, apiResponse.data());
    verify(bookLendingService).findAllUserLendingsAsDTO(eq(userId), eq(type));
  }

  @Test
  @DisplayName("POST /make-loanable - Should make book loanable and return success")
  void makeBookLoanable_ShouldReturnSuccess() {
    // Given
    Long bookId = 1L;
    Long userId = 1L;
    when(bookLendingService.loanable(eq(bookId), eq(userId))).thenReturn(true);

    // When
    ResponseEntity<ApiResponse<Boolean>> response = bookLendingController.makeBookLoanable(bookId, userId);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<Boolean> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Book added to lending list", apiResponse.message());
    assertTrue(apiResponse.data());
    verify(bookLendingService).loanable(eq(bookId), eq(userId));
  }

  @Test
  @DisplayName("POST /make-loanable - Should return error when making loanable fails")
  void makeBookLoanable_ShouldReturnError_WhenMakingLoanableFails() {
    // Given
    Long bookId = 1L;
    Long userId = 1L;
    when(bookLendingService.loanable(eq(bookId), eq(userId))).thenReturn(false);

    // When
    ResponseEntity<ApiResponse<Boolean>> response = bookLendingController.makeBookLoanable(bookId, userId);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ApiResponse<Boolean> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertFalse(apiResponse.success());
    assertEquals("Failed to add book to lending list", apiResponse.message());
    assertFalse(apiResponse.data());
    verify(bookLendingService).loanable(eq(bookId), eq(userId));
  }

  @Test
  @DisplayName("DELETE /remove-loanable - Should remove book from loanable and return success")
  void removeBookFromLoanable_ShouldReturnSuccess() {
    // Given
    Long bookId = 1L;
    Long userId = 1L;
    when(bookLendingService.isNoMoreLoanable(eq(bookId), eq(userId))).thenReturn(true);

    // When
    ResponseEntity<ApiResponse<Boolean>> response = bookLendingController.removeBookFromLoanable(bookId, userId);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<Boolean> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Book removed from lending list", apiResponse.message());
    assertTrue(apiResponse.data());
    verify(bookLendingService).isNoMoreLoanable(eq(bookId), eq(userId));
  }

  @Test
  @DisplayName("DELETE /remove-loanable - Should return error when remove loanable fails")
  void removeBookFromLoanable_ShouldReturnError_WhenRemoveLoanableFails() {
    // Given
    Long bookId = 1L;
    Long userId = 1L;
    when(bookLendingService.isNoMoreLoanable(eq(bookId), eq(userId))).thenReturn(false);

    // When
    ResponseEntity<ApiResponse<Boolean>> response = bookLendingController.removeBookFromLoanable(bookId, userId);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ApiResponse<Boolean> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertFalse(apiResponse.success());
    assertEquals("Failed to remove book from lending list", apiResponse.message());
    assertFalse(apiResponse.data());
    verify(bookLendingService).isNoMoreLoanable(eq(bookId), eq(userId));
  }

  @Test
  @DisplayName("GET /my-lendings - Should return user's lent books")
  void getCurrentUserLendings_ShouldReturnLentBooks() {
    // Given
    Long userId = 1L;

    LocalDateTime now = LocalDateTime.now();
    UserIndex borrower = new UserIndex(2L, "Jane", "Smith", "jane.smith@example.com", null, UserRole.USER);
    UserIndex lender = new UserIndex(userId, "John", "Doe", "john.doe@example.com", null, UserRole.USER);

    // Create BookShow for tests
    BookShow bookShow = new BookShow(
            1L,
            Set.of("1234567890"),
            "Test Book",
            new HashSet<>(),
            "Test description",
            null,
            now,
            now,
            Set.of("Fiction"),
            "",
            ""
    );

    BookLendingShow lending1 = new BookLendingShow(
            1L,
            bookShow,
            borrower,
            lender,
            BookStatus.BORROWED,
            now.minusDays(7),
            now.minusDays(5),
            now.plusDays(9),
            null
    );

    List<BookLendingShow> borrowedBooks = new ArrayList<>();
    List<BookLendingShow> lentBooks = List.of(lending1);
    LendingResponseDTO responseDTO = new LendingResponseDTO(borrowedBooks, lentBooks, false);

    when(bookLendingService.findAllUserLendingsAsDTO(eq(userId), eq(LendStatus.LENT))).thenReturn(responseDTO);

    // When
    ResponseEntity<ApiResponse<LendingResponseDTO>> response = bookLendingController.getCurrentUserLendings(userId);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<LendingResponseDTO> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("User lent books retrieved", apiResponse.message());
    assertEquals(responseDTO, apiResponse.data());
    verify(bookLendingService).findAllUserLendingsAsDTO(eq(userId), eq(LendStatus.LENT));
  }

  @Test
  @DisplayName("GET /my-lendings - Should return message when no lent books found")
  void getCurrentUserLendings_ShouldReturnMessage_WhenNoLentBooksFound() {
    // Given
    Long userId = 1L;

    List<BookLendingShow> borrowedBooks = new ArrayList<>();
    List<BookLendingShow> lentBooks = new ArrayList<>();
    LendingResponseDTO responseDTO = new LendingResponseDTO(borrowedBooks, lentBooks, false);

    when(bookLendingService.findAllUserLendingsAsDTO(eq(userId), eq(LendStatus.LENT))).thenReturn(responseDTO);

    // When
    ResponseEntity<ApiResponse<LendingResponseDTO>> response = bookLendingController.getCurrentUserLendings(userId);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<LendingResponseDTO> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("No lent books found", apiResponse.message());
    assertEquals(responseDTO, apiResponse.data());
    verify(bookLendingService).findAllUserLendingsAsDTO(eq(userId), eq(LendStatus.LENT));
  }

  @Test
  @DisplayName("GET /ticket-download/{lendingId} - Should download shipping ticket")
  void downloadTicket_ShouldReturnTicketPDF() throws IOException {
    // Given
    Long lendingId = 1L;
    byte[] pdfBytes = new byte[]{1, 2, 3, 4, 5}; // Sample PDF bytes
    ServletOutputStream outputStream = mock(ServletOutputStream.class);

    when(bookLendingService.getShippingTicketBytes(eq(lendingId))).thenReturn(pdfBytes);
    when(httpServletResponse.getOutputStream()).thenReturn(outputStream);

    // When
    bookLendingController.downloadTicket(lendingId, httpServletResponse);

    // Then
    verify(httpServletResponse).setContentType("application/pdf");
    verify(httpServletResponse).setHeader(eq("Content-Disposition"), contains("etiquette_expedition_" + lendingId));
    verify(httpServletResponse).setContentLength(pdfBytes.length);
    verify(outputStream).write(pdfBytes);
    verify(outputStream).flush();
  }

  @Test
  @DisplayName("GET /ticket-download/{lendingId} - Should return error when ticket not found")
  void downloadTicket_ShouldReturnError_WhenTicketNotFound() throws IOException {
    // Given
    Long lendingId = 1L;

    when(bookLendingService.getShippingTicketBytes(eq(lendingId))).thenReturn(null);

    // When
    bookLendingController.downloadTicket(lendingId, httpServletResponse);

    // Then
    verify(httpServletResponse).sendError(eq(HttpServletResponse.SC_NOT_FOUND), anyString());
    verify(httpServletResponse, never()).getOutputStream();
  }
}