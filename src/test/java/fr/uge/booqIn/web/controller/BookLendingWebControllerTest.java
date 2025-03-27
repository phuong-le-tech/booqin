package fr.uge.booqIn.web.controller;

import fr.uge.booqIn.controller.web.BookLendingWebController;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.lending.BookLendingShow;
import fr.uge.booqIn.dto.lending.LendingResponseDTO;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.BookStatus;
import fr.uge.booqIn.enums.LendStatus;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.factory.fixture.AddressFactory;
import fr.uge.booqIn.service.interfaces.BookLendingInterface;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("BookLendingWebController Tests")
class BookLendingWebControllerTest {

  @Mock
  private UserServiceInterface userService;

  @Mock
  private BookServiceInterface bookService;

  @Mock
  private BookLendingInterface bookLendingInterface;

  @Mock
  private Model model;

  @Mock
  private RedirectAttributes redirectAttributes;

  @Mock
  private User currentUser;

  @InjectMocks
  private BookLendingWebController bookLendingWebController;

  private BookLendingShow testBookLendingShow;
  private List<BookLendingShow> testBookLendingList;
  private LendingResponseDTO testLendingResponseDTO;
  private BookShow testBookShow;
  private UserIndex testUserIndex;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    // Setup test data
    testUserIndex = new UserIndex(
            1L,
            "John",
            "Doe",
            "john.doe@example.com",
            AddressFactory.createAddress(),
            UserRole.USER
    );

    testBookShow = new BookShow(
            1L,
            Collections.singleton("1234567890"),
            "Test Book",
            Collections.emptySet(),
            "Test Description",
            new Date(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            Collections.emptySet(),
            "",
            ""
    );

    testBookLendingShow = new BookLendingShow(
            1L,
            testBookShow,
            testUserIndex,
            testUserIndex,
            BookStatus.AVAILABLE,
            LocalDateTime.now(),
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(14),
            null
    );

    testBookLendingList = Collections.singletonList(testBookLendingShow);
    testLendingResponseDTO = new LendingResponseDTO(testBookLendingList, testBookLendingList, true);

    // Common mock setup
    when(currentUser.getId()).thenReturn(1L);
  }

  @Test
  @DisplayName("confirmLoan - Should confirm loan and redirect")
  void confirmLoan_ShouldConfirmLoanAndRedirect() {
    // Given
    Long lendingId = 1L;
    when(bookLendingInterface.confirmLoan(eq(lendingId))).thenReturn(true);

    // When
    String viewName = bookLendingWebController.confirmLoan(lendingId, redirectAttributes);

    // Then
    assertEquals("redirect:/loans?userId=ALL", viewName);

    verify(bookLendingInterface).confirmLoan(eq(lendingId));
    verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
  }

  @Test
  @DisplayName("confirmLoan - Should handle failure and redirect")
  void confirmLoan_Failure_ShouldRedirectWithError() {
    // Given
    Long lendingId = 1L;
    when(bookLendingInterface.confirmLoan(eq(lendingId))).thenReturn(false);

    // When
    String viewName = bookLendingWebController.confirmLoan(lendingId, redirectAttributes);

    // Then
    assertEquals("redirect:/loans?userId=ALL", viewName);

    verify(bookLendingInterface).confirmLoan(eq(lendingId));
    verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
  }

  @Test
  @DisplayName("shipBook - Should mark book as shipped and redirect")
  void shipBook_ShouldMarkAsShippedAndRedirect() {
    // Given
    Long lendingId = 1L;
    when(bookLendingInterface.markAsShipped(eq(lendingId))).thenReturn(true);

    // When
    String viewName = bookLendingWebController.shipBook(lendingId, redirectAttributes);

    // Then
    assertEquals("redirect:/loans?type=BORROWED", viewName);

    verify(bookLendingInterface).markAsShipped(eq(lendingId));
    verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
  }

  @Test
  @DisplayName("shipBook - Should handle failure and redirect")
  void shipBook_Failure_ShouldRedirectWithError() {
    // Given
    Long lendingId = 1L;
    when(bookLendingInterface.markAsShipped(eq(lendingId))).thenReturn(false);

    // When
    String viewName = bookLendingWebController.shipBook(lendingId, redirectAttributes);

    // Then
    assertEquals("redirect:/loans?type=BORROWED", viewName);

    verify(bookLendingInterface).markAsShipped(eq(lendingId));
    verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
  }

  @Test
  @DisplayName("returnBook - Should mark book as returned and redirect")
  void returnBook_ShouldMarkAsReturnedAndRedirect() {
    // Given
    Long lendingId = 1L;
    when(bookLendingInterface.returnBook(eq(lendingId))).thenReturn(true);

    // When
    String viewName = bookLendingWebController.returnBook(lendingId, redirectAttributes);

    // Then
    assertEquals("redirect:/loans?type=BORROWED", viewName);

    verify(bookLendingInterface).returnBook(eq(lendingId));
    verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
  }

  @Test
  @DisplayName("returnBook - Should handle failure and redirect")
  void returnBook_Failure_ShouldRedirectWithError() {
    // Given
    Long lendingId = 1L;
    when(bookLendingInterface.returnBook(eq(lendingId))).thenReturn(false);

    // When
    String viewName = bookLendingWebController.returnBook(lendingId, redirectAttributes);

    // Then
    assertEquals("redirect:/loans?type=BORROWED", viewName);

    verify(bookLendingInterface).returnBook(eq(lendingId));
    verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
  }

  @Test
  @DisplayName("makeBookLoanableForm - Should display make loanable form")
  void makeBookLoanableForm_ShouldDisplayMakeLoanableForm() {
    // Given
    Long bookId = 1L;

    // When
    String viewName = bookLendingWebController.makeBookLoanableForm(bookId, model);

    // Then
    assertEquals("layout", viewName);

    verify(model).addAttribute(eq("bookId"), eq(bookId));
    verify(model).addAttribute(eq("title"), eq("Ajouter un livre à prêter"));
    verify(model).addAttribute(eq("content"), eq("pages/bookLending/make-loanable"));
  }

  @Test
  @DisplayName("makeBookLoanable - Should make book loanable and redirect")
  void makeBookLoanable_ShouldMakeBookLoanableAndRedirect() {
    // Given
    Long bookId = 1L;
    when(bookLendingInterface.loanable(eq(bookId), eq(1L))).thenReturn(true);

    // When
    String viewName = bookLendingWebController.makeBookLoanable(bookId, currentUser, redirectAttributes);

    // Then
    assertEquals("redirect:/loans?type=LENT", viewName);

    verify(bookLendingInterface).loanable(eq(bookId), eq(1L));
    verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
  }

  @Test
  @DisplayName("makeBookLoanable - Should handle failure and redirect")
  void makeBookLoanable_Failure_ShouldRedirectWithError() {
    // Given
    Long bookId = 1L;
    when(bookLendingInterface.loanable(eq(bookId), eq(1L))).thenReturn(false);

    // When
    String viewName = bookLendingWebController.makeBookLoanable(bookId, currentUser, redirectAttributes);

    // Then
    assertEquals("redirect:/loans?type=LENT", viewName);

    verify(bookLendingInterface).loanable(eq(bookId), eq(1L));
    verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
  }

  @Test
  @DisplayName("removeBookFromLoanable - Should remove book from loanable and redirect")
  void removeBookFromLoanable_ShouldRemoveBookAndRedirect() {
    // Given
    Long bookId = 1L;
    when(bookLendingInterface.isNoMoreLoanable(eq(bookId), eq(1L))).thenReturn(true);

    // When
    String viewName = bookLendingWebController.removeBookFromLoanable(bookId, currentUser, redirectAttributes);

    // Then
    assertEquals("redirect:/loans?type=LENT", viewName);

    verify(bookLendingInterface).isNoMoreLoanable(eq(bookId), eq(1L));
    verify(redirectAttributes).addFlashAttribute(eq("successMessage"), anyString());
  }

  @Test
  @DisplayName("removeBookFromLoanable - Should handle failure and redirect")
  void removeBookFromLoanable_Failure_ShouldRedirectWithError() {
    // Given
    Long bookId = 1L;
    when(bookLendingInterface.isNoMoreLoanable(eq(bookId), eq(1L))).thenReturn(false);

    // When
    String viewName = bookLendingWebController.removeBookFromLoanable(bookId, currentUser, redirectAttributes);

    // Then
    assertEquals("redirect:/loans?type=LENT", viewName);

    verify(bookLendingInterface).isNoMoreLoanable(eq(bookId), eq(1L));
    verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), anyString());
  }
}