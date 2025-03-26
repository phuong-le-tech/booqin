package fr.uge.booqIn.web.controller;

import fr.uge.booqIn.controller.web.BookWebController;
import fr.uge.booqIn.controller.web.HomeWebController;
import fr.uge.booqIn.dto.author.AuthorDto;
import fr.uge.booqIn.dto.book.BookCreate;
import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.bookCollection.LiteCollection;
import fr.uge.booqIn.dto.lending.BookLendingShow;
import fr.uge.booqIn.dto.lending.LendingResponseDTO;
import fr.uge.booqIn.dto.vote.VoteCount;
import fr.uge.booqIn.dto.vote.VoteCreate;
import fr.uge.booqIn.entity.AbstractBookCollection;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.BookStatus;
import fr.uge.booqIn.enums.VoteType;
import fr.uge.booqIn.model.BookCollection;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

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
            testCategories
    );

    testBookIndex = new BookIndex(
            1L,
            "Test Book",
            Collections.singleton("John Doe"),
            testDate
    );

    testBookList = Collections.singletonList(testBookIndex);
    testBookPage = new PageImpl<>(testBookList);

    // Common mock setup
    when(currentUser.getId()).thenReturn(1L);
    doNothing().when(homeWebController).prepareModel(any(Model.class), any(Principal.class), anyString());
  }

  @Test
  @DisplayName("getAllBooks - Should add user's collections when user is authenticated")
  void getAllBooks_WithAuthenticatedUser_ShouldAddCollections() {
    // Given
    List<AbstractBookCollection> collections = new ArrayList<>();
    collections.add(mockCollection);

    when(bookService.findBooks(isNull(), isNull(), isNull(), any())).thenReturn(testBookPage);
    when(userService.findUserById(eq(1L))).thenReturn(currentUser);
    when(currentUser.getCollections()).thenReturn(collections);
    when(mockCollection.getCollectionType()).thenReturn(BookCollection.CollectionType.NORMAL);
    when(mockCollection.getId()).thenReturn(1L);
    when(mockCollection.getTitle()).thenReturn("My Collection");

    List<BookLendingShow> emptyLendings = Collections.emptyList();
    LendingResponseDTO lendingResponseDTO = new LendingResponseDTO(emptyLendings, emptyLendings, true);
    when(bookLendingInterface.findAllUserLendingsAsDTO(eq(1L), eq("all"))).thenReturn(lendingResponseDTO);

    // When
    String viewName = bookWebController.getAllBooks(model, 0, 20, "title", "asc", null, currentUser, principal);

    // Then
    assertEquals("layout", viewName);

    verify(userService).findUserById(eq(1L));
    verify(model).addAttribute(eq("userId"), eq(1L));
    verify(bookLendingInterface).findAllUserLendingsAsDTO(eq(1L), eq("all"));
    verify(model).addAttribute(eq("lentBooks"), anyList());
    verify(model).addAttribute(eq("borrowedBooks"), anyList());
  }

  @Test
  @DisplayName("getBookFromId - Should display book details")
  void getBookFromId_ShouldDisplayBookDetails() {
    // Given
    Long bookId = 1L;
    VoteCount voteCount = new VoteCount(bookId, "BOOK", 5, 2, 3);

    when(bookService.findById(eq(bookId))).thenReturn(testBookShow);
    when(voteServiceInterface.getVoteCounts(eq(bookId), eq("BOOK"))).thenReturn(voteCount);
    when(commentService.countByEntity(eq(bookId), eq("BOOK"))).thenReturn(3L);
    when(commentService.getCommentTreeByEntity(eq(bookId), eq("BOOK"))).thenReturn(Collections.emptyList());

    // When
    String viewName = bookWebController.getBookFromId(model, bookId, currentUser, principal);

    // Then
    assertEquals("layout", viewName);

    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("book-details"));
    verify(bookService).findById(eq(bookId));
    verify(voteServiceInterface).getVoteCounts(eq(bookId), eq("BOOK"));
    verify(commentService).countByEntity(eq(bookId), eq("BOOK"));
    verify(commentService).getCommentTreeByEntity(eq(bookId), eq("BOOK"));
    verify(model).addAttribute(eq("book"), eq(testBookShow));
    verify(model).addAttribute(eq("votes"), eq(voteCount));
    verify(model).addAttribute(eq("commentCount"), eq(3L));
    verify(model).addAttribute(eq("title"), eq("BooqIn - Test Book"));
    verify(model).addAttribute(eq("content"), eq("pages/book/bookDetails"));
  }

  @Test
  @DisplayName("getBookFromId - Should add user vote if authenticated")
  void getBookFromId_WithAuthenticatedUser_ShouldAddUserVote() {
    // Given
    Long bookId = 1L;
    VoteCount voteCount = new VoteCount(bookId, "BOOK", 5, 2, 3);

    when(bookService.findById(eq(bookId))).thenReturn(testBookShow);
    when(voteServiceInterface.getVoteCounts(eq(bookId), eq("BOOK"))).thenReturn(voteCount);
    when(commentService.countByEntity(eq(bookId), eq("BOOK"))).thenReturn(3L);
    when(commentService.getCommentTreeByEntity(eq(bookId), eq("BOOK"))).thenReturn(Collections.emptyList());
    when(voteServiceInterface.getUserVoteType(eq(1L), eq(bookId), eq("BOOK"))).thenReturn(VoteType.UPVOTE);

    // When
    String viewName = bookWebController.getBookFromId(model, bookId, currentUser, principal);

    // Then
    assertEquals("layout", viewName);

    verify(voteServiceInterface).getUserVoteType(eq(1L), eq(bookId), eq("BOOK"));
    verify(model).addAttribute(eq("userVote"), eq("UPVOTE"));
    verify(model).addAttribute(eq("user"), eq(currentUser));
  }

  @Test
  @DisplayName("showCreateForm - Should display book creation form")
  void showCreateForm_ShouldDisplayBookCreationForm() {
    // When
    String viewName = bookWebController.showCreateForm(model, principal);

    // Then
    assertEquals("layout", viewName);

    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("book-create"));
    verify(model).addAttribute(eq("book"), any(BookCreate.class));
    verify(model).addAttribute(eq("title"), eq("BooqIn - Add Book"));
    verify(model).addAttribute(eq("content"), eq("pages/book/bookCreate"));
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

    Book createdBook = new Book();
    when(bookService.createBook(any(Book.class))).thenReturn(createdBook);

    // When
    String viewName = bookWebController.addBook(title, authors, isbns, publicationDate, description);

    // Then
    assertEquals("redirect:/books", viewName);

    verify(bookService).createBook(any(Book.class));

    ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
    verify(bookService).createBook(bookCaptor.capture());
    Book capturedBook = bookCaptor.getValue();
    assertEquals(title, capturedBook.getTitle());
    assertEquals(description, capturedBook.getDescription());
    assertEquals(publicationDate, capturedBook.getPublicationDate());
    assertEquals(2, capturedBook.getIsbns().size());
    assertEquals(2, capturedBook.getAuthors().size());
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