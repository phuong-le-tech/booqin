package fr.uge.booqIn.web.controller;

import fr.uge.booqIn.controller.web.BookWebController;
import fr.uge.booqIn.controller.web.HomeWebController;
import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.vote.VoteCount;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.BookStatus;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.enums.VoteType;
import fr.uge.booqIn.service.interfaces.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("BookWebController Tests")
class BookWebControllerTest {

  private MockMvc mockMvc;

  @Mock
  private BookServiceInterface bookService;

  @Mock
  private VoteServiceInterface voteService;

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

  @InjectMocks
  private BookWebController bookWebController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
    viewResolver.setPrefix("/WEB-INF/templates/");
    viewResolver.setSuffix(".html");

    mockMvc = MockMvcBuilders.standaloneSetup(bookWebController)
            .setViewResolvers(viewResolver)
            .build();

    // Setup the behavior for homeWebController.prepareModel
    doNothing().when(homeWebController).prepareModel(any(Model.class), any(Principal.class), anyString());
  }

  @Test
  @DisplayName("GET /books - Should show books list for anonymous user")
  void getAllBooks_ShouldReturnBooksList_ForAnonymousUser() throws Exception {
    // Given
    List<BookIndex> books = createMockBookIndexList();
    Page<BookIndex> bookPage = new PageImpl<>(books);

    when(bookService.findBooks(isNull(), isNull(), isNull(), any(PageRequest.class))).thenReturn(bookPage);

    // When & Then
    mockMvc.perform(get("/books"))
            .andExpect(status().isOk())
            .andExpect(view().name("layout"))
            .andExpect(model().attributeExists("books"))
            .andExpect(model().attributeExists("title"))
            .andExpect(model().attribute("title", "BooqIn - Books"))
            .andExpect(model().attribute("content", "pages/book/list"));

    verify(homeWebController).prepareModel(any(Model.class), isNull(), eq("books"));
  }

  @Test
  @DisplayName("GET /books - Should show books list with search query")
  void getAllBooks_ShouldReturnBooksListWithSearchQuery() throws Exception {
    // Given
    List<BookIndex> books = createMockBookIndexList();
    Page<BookIndex> bookPage = new PageImpl<>(books);
    String query = "test";

    when(bookService.findBooks(eq(query), eq(query), eq(query), any(PageRequest.class))).thenReturn(bookPage);

    // When & Then
    mockMvc.perform(get("/books")
                    .param("query", query))
            .andExpect(status().isOk())
            .andExpect(view().name("layout"))
            .andExpect(model().attributeExists("books"))
            .andExpect(model().attributeExists("query"))
            .andExpect(model().attribute("query", query));

    verify(homeWebController).prepareModel(any(Model.class), isNull(), eq("books"));
  }

  @Test
  @DisplayName("GET /books/{id} - Should show book details for anonymous user")
  void getBookFromId_ShouldReturnBookDetails_ForAnonymousUser() throws Exception {
    // Given
    Long bookId = 1L;
    BookShow bookShow = createMockBookShow(bookId);
    VoteCount voteCount = new VoteCount(bookId, "BOOK", 5L, 2L, 3);

    when(bookService.findById(eq(bookId))).thenReturn(bookShow);
    when(voteService.getVoteCounts(eq(bookId), eq("BOOK"))).thenReturn(voteCount);
    when(commentService.countByEntity(eq(bookId), eq("BOOK"))).thenReturn(3L);
    when(commentService.getCommentTreeByEntity(eq(bookId), eq("BOOK"))).thenReturn(new ArrayList<>());

    // When & Then
    mockMvc.perform(get("/books/{id}", bookId))
            .andExpect(status().isOk())
            .andExpect(view().name("layout"))
            .andExpect(model().attributeExists("book"))
            .andExpect(model().attributeExists("votes"))
            .andExpect(model().attributeExists("comments"))
            .andExpect(model().attributeExists("commentCount"))
            .andExpect(model().attributeExists("entityType"))
            .andExpect(model().attributeExists("entityId"))
            .andExpect(model().attribute("title", "BooqIn - " + bookShow.title()))
            .andExpect(model().attribute("content", "pages/book/bookDetails"));

    verify(homeWebController).prepareModel(any(Model.class), isNull(), eq("book-details"));
  }

  @Test
  @DisplayName("GET /books/{id} - Should show book details with user vote for authenticated user")
  void getBookFromId_ShouldReturnBookDetailsWithUserVote_ForAuthenticatedUser() throws Exception {
    // Given
    Long bookId = 1L;
    BookShow bookShow = createMockBookShow(bookId);
    VoteCount voteCount = new VoteCount(bookId, "BOOK", 5L, 2L, 3);

    User user = new User("John", "Doe", "john.doe@example.com", "password", null, UserRole.USER);
    user.setId(1L);

    when(bookService.findById(eq(bookId))).thenReturn(bookShow);
    when(voteService.getVoteCounts(eq(bookId), eq("BOOK"))).thenReturn(voteCount);
    when(commentService.countByEntity(eq(bookId), eq("BOOK"))).thenReturn(3L);
    when(commentService.getCommentTreeByEntity(eq(bookId), eq("BOOK"))).thenReturn(new ArrayList<>());
    when(voteService.getUserVoteType(eq(user.getId()), eq(bookId), eq("BOOK"))).thenReturn(VoteType.UPVOTE);

    // When & Then
    mockMvc.perform(get("/books/{id}", bookId)
                    .with(SecurityMockMvcRequestPostProcessors.user(user)))
            .andExpect(status().isOk())
            .andExpect(view().name("layout"))
            .andExpect(model().attributeExists("userVote"))
            .andExpect(model().attributeExists("user"))
            .andExpect(model().attribute("userVote", "UPVOTE"));
  }

  @Test
  @DisplayName("GET /books/new - Should show book creation form")
  void showCreateForm_ShouldReturnNewBookForm() throws Exception {
    // When & Then
    mockMvc.perform(get("/books/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("layout"))
            .andExpect(model().attributeExists("book"))
            .andExpect(model().attributeExists("title"))
            .andExpect(model().attribute("title", "BooqIn - Add Book"))
            .andExpect(model().attribute("content", "pages/book/bookCreate"));

    verify(homeWebController).prepareModel(any(Model.class), isNull(), eq("book-create"));
  }

  @Test
  @DisplayName("POST /books - Should add new book and redirect to books list")
  void addBook_ShouldCreateAndRedirect() throws Exception {
    // Given
    Book book = createMockBook();
    when(bookService.createBook(any(Book.class))).thenReturn(book);

    // When & Then
    mockMvc.perform(post("/books")
                    .param("title", "Test Book")
                    .param("authors", "John Doe")
                    .param("isbn", "1234567890")
                    .param("publicationDate", "2023-01-01")
                    .param("description", "Test description"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/books"));
  }

  @Test
  @DisplayName("POST /books/vote - Should process vote for authenticated user")
  void vote_ShouldProcessVote() throws Exception {
    // Given
    Long bookId = 1L;
    User user = new User("John", "Doe", "john.doe@example.com", "password", null, UserRole.USER);
    user.setId(1L);

    // When & Then
    mockMvc.perform(post("/books/vote")
                    .with(SecurityMockMvcRequestPostProcessors.user(user))
                    .param("bookId", bookId.toString())
                    .param("voteType", "UPVOTE"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/books/" + bookId));
  }

  // Helper methods for creating mock data
  private List<BookIndex> createMockBookIndexList() {
    List<BookIndex> books = new ArrayList<>();

    Set<String> authorNames1 = new HashSet<>(Collections.singleton("John Doe"));
    Set<String> authorNames2 = new HashSet<>(Collections.singleton("Jane Smith"));

    books.add(new BookIndex(1L, "Test Book 1", authorNames1, new Date()));
    books.add(new BookIndex(2L, "Test Book 2", authorNames2, new Date()));

    return books;
  }

  private BookShow createMockBookShow(Long id) {
    Set<String> isbns = new HashSet<>(Collections.singleton("1234567890"));
    Set<String> categories = new HashSet<>(Collections.singleton("Fiction"));

    return new BookShow(
            id,
            isbns,
            "Test Book",
            new HashSet<>(),
            "Test description",
            new Date(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            categories
    );
  }

  private Book createMockBook() {
    Set<String> isbns = new HashSet<>(Collections.singleton("1234567890"));
    Set<Author> authors = new HashSet<>(Collections.singleton(new Author("John", "Doe")));
    Set<String> categories = new HashSet<>(Collections.singleton("Fiction"));

    return new Book.Builder()
            .id(1L)
            .title("Test Book")
            .description("Test description")
            .authors(authors)
            .isbns(isbns)
            .categories(categories)
            .publicationDate(new Date())
            .build();
  }
}