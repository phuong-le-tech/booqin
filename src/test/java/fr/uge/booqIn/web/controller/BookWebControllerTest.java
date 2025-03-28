package fr.uge.booqIn.web.controller;

import fr.uge.booqIn.controller.web.BookWebController;
import fr.uge.booqIn.controller.web.HomeWebController;
import fr.uge.booqIn.dto.author.AuthorDto;
import fr.uge.booqIn.dto.book.BookCreate;
import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.vote.VoteCount;
import fr.uge.booqIn.dto.vote.VoteCreate;
import fr.uge.booqIn.entity.AbstractBookCollection;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.VoteType;
import fr.uge.booqIn.service.interfaces.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.ui.Model;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("BookWebController Tests")
class BookWebControllerTest {

  @Mock
  private BookServiceInterface bookService;

  @Mock
  private VoteServiceInterface voteServiceInterface;

  @Mock
  private UserServiceInterface userService;

  @Mock
  private BookCollectionServiceInterface bookCollectionService;

  @Mock
  private CommentServiceInterface commentService;

  @Mock
  private HomeWebController homeWebController;

  @Mock
  private BookLendingInterface bookLendingInterface;

  @Mock
  private Model model;

  @Mock
  private Principal principal;

  @Mock
  private User currentUser;

  @Mock
  private AbstractBookCollection mockCollection;

  @InjectMocks
  private BookWebController bookWebController;

  private BookShow testBookShow;
  private BookIndex testBookIndex;
  private List<BookIndex> testBookList;
  private Page<BookIndex> testBookPage;
  private Set<AuthorDto> testAuthors;
  private Author testAuthor;
  private Date testDate;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    // Setup test data
    testDate = new Date();

    testAuthor = new Author("John", "Doe");
    testAuthors = new HashSet<>();
    testAuthors.add(new AuthorDto(1L, "John", "Doe"));

    Set<String> testCategories = new HashSet<>();
    testCategories.add("Fiction");

    Set<String> testIsbns = new HashSet<>();
    testIsbns.add("1234567890");

    testBookShow = new BookShow(
            1L,
            testIsbns,
            "Test Book",
            testAuthors,
            "Test Description",
            testDate,
            LocalDateTime.now(),
            LocalDateTime.now(),
            testCategories,
            "",
            ""
    );

    testBookIndex = new BookIndex(
            1L,
            "Test Book",
            Collections.singleton("John Doe"),
            testDate,
            ""
    );

    testBookList = Collections.singletonList(testBookIndex);
    testBookPage = new PageImpl<>(testBookList);

    // Common mock setup
    when(currentUser.getId()).thenReturn(1L);
    doNothing().when(homeWebController).prepareModel(any(Model.class), any(Principal.class), anyString());
  }

  @Test
  @DisplayName("addBook - Should create book and redirect")
  void addBook_ShouldCreateBookAndRedirect() {
    // Given
    String title = "New Book";
    List<String> authors = Arrays.asList("John Doe", "Jane Smith");
    List<String> isbns = Arrays.asList("1234567890", "0987654321");
    Date publicationDate = new Date();
    String description = "Test description";

    BookCreate bookCreateDto = new BookCreate(
            title,
            authors,
            description,
            publicationDate,
            isbns
    );

    Book createdBook = new Book();
    createdBook.setId(1L);

    // Mocker la méthode createFromDto au lieu de createBook
    when(bookService.createFromDto(any(BookCreate.class))).thenReturn(createdBook);

    // When
    String viewName = bookWebController.addBook(title, authors, isbns, publicationDate, description);

    // Then
    assertEquals("redirect:/books", viewName);

    // Vérifier que createFromDto est appelé avec le bon DTO
    verify(bookService).createFromDto(any(BookCreate.class));

    ArgumentCaptor<BookCreate> dtoCaptor = ArgumentCaptor.forClass(BookCreate.class);
    verify(bookService).createFromDto(dtoCaptor.capture());
    BookCreate capturedDto = dtoCaptor.getValue();

    assertEquals(title, capturedDto.title());
    assertEquals(description, capturedDto.description());
    assertEquals(publicationDate, capturedDto.publicationDate());
    assertEquals(new ArrayList<>(isbns), capturedDto.isbn());
    assertEquals(new ArrayList<>(authors), capturedDto.authors());
  }

  @Test
  @DisplayName("vote - Should cast vote and redirect")
  void vote_ShouldCastVoteAndRedirect() {
    // Given
    Long bookId = 1L;
    VoteType voteType = VoteType.UPVOTE;

    when(voteServiceInterface.castVote(any(VoteCreate.class))).thenReturn(null);

    // When
    String viewName = bookWebController.vote(bookId, voteType, currentUser);

    // Then
    assertEquals("redirect:/books/" + bookId, viewName);

    verify(voteServiceInterface).castVote(any(VoteCreate.class));

    ArgumentCaptor<VoteCreate> voteCaptor = ArgumentCaptor.forClass(VoteCreate.class);
    verify(voteServiceInterface).castVote(voteCaptor.capture());
    VoteCreate capturedVote = voteCaptor.getValue();
    assertEquals(1L, capturedVote.userId());
    assertEquals(bookId, capturedVote.entityId());
    assertEquals("BOOK", capturedVote.entityType());
    assertEquals(voteType, capturedVote.voteType());
  }

  @Test
  @DisplayName("vote - Should not cast vote when user is not authenticated")
  void vote_WithoutAuthenticatedUser_ShouldNotCastVote() {
    // Given
    Long bookId = 1L;
    VoteType voteType = VoteType.UPVOTE;

    // When
    String viewName = bookWebController.vote(bookId, voteType, null);

    // Then
    assertEquals("redirect:/books/" + bookId, viewName);

    verifyNoInteractions(voteServiceInterface);
  }
}
