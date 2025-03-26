package fr.uge.booqIn.web.controller;

import fr.uge.booqIn.controller.web.BookCollectionWebController;
import fr.uge.booqIn.controller.web.HomeWebController;
import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.bookCollection.BookCollectionCreate;
import fr.uge.booqIn.dto.bookCollection.BookCollectionIndex;
import fr.uge.booqIn.dto.bookCollection.BookCollectionShow;
import fr.uge.booqIn.dto.bookCollection.BookCollectionUpdate;
import fr.uge.booqIn.dto.lending.BookLendingShow;
import fr.uge.booqIn.dto.lending.LendingResponseDTO;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.dto.vote.VoteCount;
import fr.uge.booqIn.dto.vote.VoteCreate;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.BookStatus;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.enums.VoteType;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.factory.BookCollectionFactory;
import fr.uge.booqIn.factory.fixture.AddressFactory;
import fr.uge.booqIn.factory.fixture.UserFactory;
import fr.uge.booqIn.mapper.BookCollectionMapper;
import fr.uge.booqIn.mapper.BookMapper;
import fr.uge.booqIn.model.BookCollection;
import fr.uge.booqIn.service.impl.BookServiceImpl;
import fr.uge.booqIn.service.interfaces.BookCollectionServiceInterface;
import fr.uge.booqIn.service.interfaces.BookLendingInterface;
import fr.uge.booqIn.service.interfaces.CommentServiceInterface;
import fr.uge.booqIn.service.interfaces.VoteServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("BookCollectionWebController Tests")
class BookCollectionWebControllerTest {

  @Mock
  private BookCollectionServiceInterface bookCollectionService;

  @Mock
  private VoteServiceInterface voteService;

  @Mock
  private CommentServiceInterface commentServiceInterface;

  @Mock
  private BookServiceImpl bookService;

  @Mock
  private HomeWebController homeWebController;

  @Mock
  private BookLendingInterface bookLendingInterface;

  @Mock
  private BookMapper bookMapper;

  @Mock
  private BookCollectionMapper bookCollectionMapper;

  @Mock
  private Model model;

  @Mock
  private Principal principal;

  @Mock
  private User currentUser;

  @Mock
  private BindingResult bindingResult;
  
  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private BookCollectionWebController bookCollectionWebController;

  private BookCollectionShow testCollectionShow;
  private BookCollectionIndex testCollectionIndex;
  private List<BookCollectionIndex> testCollectionList;
  private Page<BookCollectionIndex> testCollectionPage;
  private Page<BookCollectionShow> testCollectionShowPage;
  private BookShow testBookShow;
  private BookIndex testBookIndex;
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

    testBookIndex = new BookIndex(
            1L,
            "Test Book",
            Collections.singleton("John Doe"),
            new Date()
    );

    List<BookIndex> books = Collections.singletonList(testBookIndex);

    testCollectionShow = new BookCollectionShow(
            1L,
            "Test Collection",
            "Test Description",
            true,
            books,
            testUserIndex,
            "NORMAL",
            LocalDateTime.now(),
            LocalDateTime.now()
    );

    testCollectionIndex = new BookCollectionIndex(
            1L,
            "Test Collection",
            "Test Description",
            "John Doe",
            1L,
            1,
            true,
            BookCollection.CollectionType.NORMAL
    );

    testCollectionList = Collections.singletonList(testCollectionIndex);
    testCollectionPage = new PageImpl<>(testCollectionList);
    testCollectionShowPage = new PageImpl<>(Collections.singletonList(testCollectionShow));

    testBookShow = new BookShow(
            1L,
            Collections.singleton("1234567890"),
            "Test Book",
            Collections.emptySet(),
            "Test Description",
            new Date(),
            LocalDateTime.now(),
            LocalDateTime.now(),
            Collections.emptySet()
    );

    // Common mock setup
    when(currentUser.getId()).thenReturn(1L);
    doNothing().when(homeWebController).prepareModel(any(Model.class), any(Principal.class), anyString());
  }

  @Test
  @DisplayName("showPublicCollections - Should display public collections")
  void showPublicCollections_ShouldDisplayPublicCollections() {
    // Given
    int page = 0;
    int size = 10;

    when(bookCollectionService.getPublicCollections(any(PageRequest.class))).thenReturn(testCollectionPage);

    // When
    String viewName = bookCollectionWebController.showPublicCollections(model, principal, page, size);

    // Then
    assertEquals("layout", viewName);

    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("collections-public"));
    verify(bookCollectionService).getPublicCollections(any(PageRequest.class));
    verify(model).addAttribute(eq("publicCollections"), eq(testCollectionPage));
    verify(model).addAttribute(eq("title"), eq("BooqIn - Public Collections"));
    verify(model).addAttribute(eq("content"), eq("pages/collections/public"));
  }

  @Test
  @DisplayName("showUserCollections - Should display user collections")
  void showUserCollections_ShouldDisplayUserCollections() {
    // Given
    int page = 0;
    int size = 20;
    String sort = "title";
    String direction = "asc";

    when(bookCollectionService.getUserCollectionsWithBooks(eq(currentUser), any())).thenReturn(testCollectionShowPage);
    when(bookCollectionService.getPublicUserCollection(eq(currentUser), any())).thenReturn(testCollectionPage);
    when(bookCollectionService.getPrivateUserCollection(eq(currentUser), any())).thenReturn(testCollectionPage);

    // When
    String viewName = bookCollectionWebController.showUserCollections(model, currentUser, principal, page, size, sort, direction);

    // Then
    assertEquals("layout", viewName);

    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("collections"));
    verify(bookCollectionService).getUserCollectionsWithBooks(eq(currentUser), any());
    verify(bookCollectionService).getPublicUserCollection(eq(currentUser), any());
    verify(bookCollectionService).getPrivateUserCollection(eq(currentUser), any());
    verify(model).addAttribute(eq("collections"), eq(testCollectionShowPage));
    verify(model).addAttribute(eq("userPublicCollections"), eq(testCollectionPage));
    verify(model).addAttribute(eq("userPrivateCollections"), eq(testCollectionPage));
    verify(model).addAttribute(eq("title"), eq("BooqIn - My Collections"));
    verify(model).addAttribute(eq("content"), eq("pages/collections/userCollections"));
  }

  @Test
  @DisplayName("showCollectionDetails - Should display collection details")
  void showCollectionDetails_ShouldDisplayCollectionDetails() {
    // Given
    Long collectionId = 1L;
    VoteCount voteCount = new VoteCount(collectionId, "COLLECTION", 5, 2, 3);

    when(bookCollectionService.findBookCollectionById(eq(collectionId))).thenReturn(testCollectionShow);
    when(voteService.getVoteCounts(eq(collectionId), eq("COLLECTION"))).thenReturn(voteCount);
    when(commentServiceInterface.countByEntity(eq(collectionId), eq("COLLECTION"))).thenReturn(2L);
    when(commentServiceInterface.getCommentTreeByEntity(eq(collectionId), eq("COLLECTION"))).thenReturn(Collections.emptyList());

    // For lending data
    List<BookLendingShow> emptyLendings = Collections.emptyList();
    LendingResponseDTO lendingResponseDTO = new LendingResponseDTO(emptyLendings, emptyLendings, true);
    when(bookLendingInterface.findAllUserLendingsAsDTO(eq(1L), eq("all"))).thenReturn(lendingResponseDTO);

    // When
    String viewName = bookCollectionWebController.showCollectionDetails(collectionId, model, principal, currentUser);

    // Then
    assertEquals("layout", viewName);

    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("collection-details"));
    verify(bookCollectionService).findBookCollectionById(eq(collectionId));
    verify(voteService).getVoteCounts(eq(collectionId), eq("COLLECTION"));
    verify(commentServiceInterface).countByEntity(eq(collectionId), eq("COLLECTION"));
    verify(commentServiceInterface).getCommentTreeByEntity(eq(collectionId), eq("COLLECTION"));
    verify(model).addAttribute(eq("collection"), eq(testCollectionShow));
    verify(model).addAttribute(eq("votes"), eq(voteCount));
    verify(model).addAttribute(eq("commentCount"), eq(2L));
    verify(model).addAttribute(eq("title"), eq("BooqIn - Test Collection"));
    verify(model).addAttribute(eq("content"), eq("pages/collections/details"));
  }

  @Test
  @DisplayName("showCollectionDetails - Should handle not found exception")
  void showCollectionDetails_WhenNotFound_ShouldRedirect() {
    // Given
    Long collectionId = 999L;

    when(bookCollectionService.findBookCollectionById(eq(collectionId)))
            .thenThrow(new ResourceNotFoundException("Collection not found"));

    // When
    String viewName = bookCollectionWebController.showCollectionDetails(collectionId, model, principal, currentUser);

    // Then
    assertEquals("redirect:/collections", viewName);

    verify(bookCollectionService).findBookCollectionById(eq(collectionId));
  }

  @Test
  @DisplayName("showAddBookForm - Should display add book form")
  void showAddBookForm_ShouldDisplayAddBookForm() {
    // Given
    Long collectionId = 1L;
    List<Book> books = Collections.singletonList(new Book());

    when(bookCollectionService.findBookCollectionById(eq(collectionId))).thenReturn(testCollectionShow);
    when(bookService.findAll()).thenReturn(books);

    // When
    String viewName = bookCollectionWebController.showAddBookForm(collectionId, model, principal);

    // Then
    assertEquals("layout", viewName);

    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("collection-add-book"));
    verify(bookCollectionService).findBookCollectionById(eq(collectionId));
    verify(bookService).findAll();
    verify(model).addAttribute(eq("collectionId"), eq(collectionId));
    verify(model).addAttribute(eq("collection"), eq(testCollectionShow));
    verify(model).addAttribute(eq("books"), anyList());
    verify(model).addAttribute(eq("title"), eq("BooqIn - Add Book to Collection"));
    verify(model).addAttribute(eq("content"), eq("pages/collections/addBook"));
  }

  @Test
  @DisplayName("addBookToCollection - Should add book to collection")
  void addBookToCollection_ShouldAddBookToCollection() {
    // Given
    Long collectionId = 1L;
    Long bookId = 1L;

    when(bookCollectionService.findBookCollectionById(eq(collectionId))).thenReturn(testCollectionShow);
    when(bookService.findById(eq(bookId))).thenReturn(testBookShow);
    when(bookMapper.toAuthor(any())).thenReturn(new Author());
    doNothing().when(bookCollectionService).addBookToCollection(eq(collectionId), eq(bookId));

    // When
    String viewName = bookCollectionWebController.addBookToCollection(collectionId, bookId, currentUser, model, principal);

    // Then
    assertEquals("redirect:/collections/" + collectionId, viewName);

    verify(bookCollectionService).findBookCollectionById(eq(collectionId));
    verify(bookService).findById(eq(bookId));
    verify(bookCollectionService).addBookToCollection(eq(collectionId), eq(bookId));
  }

  @Test
  @DisplayName("removeBookFromCollection - Should remove book from collection")
  void removeBookFromCollection_ShouldRemoveBookFromCollection() {
    // Given
    Long collectionId = 1L;
    Long bookId = 1L;

    when(bookCollectionService.findBookCollectionById(eq(collectionId))).thenReturn(testCollectionShow);
    doNothing().when(bookCollectionService).removeBookFromCollection(eq(collectionId), eq(bookId));

    // When
    String viewName = bookCollectionWebController.removeBookFromCollection(collectionId, bookId, currentUser, model);

    // Then
    assertEquals("redirect:/collections/" + collectionId + "?success=bookRemoved", viewName);

    verify(bookCollectionService).findBookCollectionById(eq(collectionId));
    verify(bookCollectionService).removeBookFromCollection(eq(collectionId), eq(bookId));
  }

  @Test
  @DisplayName("showCreateCollectionForm - Should display create collection form")
  void showCreateCollectionForm_ShouldDisplayCreateCollectionForm() {
    // When
    String viewName = bookCollectionWebController.showCreateCollectionForm(model, principal);

    // Then
    assertEquals("layout", viewName);

    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("collection-create"));
    verify(model).addAttribute(eq("bookCollection"), any(BookCollectionCreate.class));
    verify(model).addAttribute(eq("title"), eq("BooqIn - Create Collection"));
    verify(model).addAttribute(eq("content"), eq("pages/collections/create"));
  }

  @Test
  @DisplayName("createCollection - Should create collection and redirect")
  void createCollection_ShouldCreateCollectionAndRedirect() {
    // Given
    BookCollectionCreate bookCollectionCreate = new BookCollectionCreate(
            "New Collection",
            "New Description",
            true,
            Optional.empty()
    );

    when(bookCollectionService.createBookCollection(eq(bookCollectionCreate), eq(currentUser))).thenReturn(testCollectionShow);

    // When
    String viewName = bookCollectionWebController.createCollection(bookCollectionCreate, model, principal, currentUser);

    // Then
    assertEquals("redirect:/collections", viewName);

    verify(bookCollectionService).createBookCollection(eq(bookCollectionCreate), eq(currentUser));
  }

  @Test
  @DisplayName("createCollection - Should handle exception")
  void createCollection_WithException_ShouldShowError() {
    // Given
    BookCollectionCreate bookCollectionCreate = new BookCollectionCreate(
            "New Collection",
            "New Description",
            true,
            Optional.empty()
    );

    when(bookCollectionService.createBookCollection(eq(bookCollectionCreate), eq(currentUser)))
            .thenThrow(new ResourceNotFoundException("Error creating collection"));

    // When
    String viewName = bookCollectionWebController.createCollection(bookCollectionCreate, model, principal, currentUser);

    // Then
    assertEquals("layout", viewName);

    verify(bookCollectionService).createBookCollection(eq(bookCollectionCreate), eq(currentUser));
    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("collection-create"));
    verify(model).addAttribute(eq("error"), anyString());
    verify(model).addAttribute(eq("title"), eq("BooqIn - Error"));
    verify(model).addAttribute(eq("content"), eq("pages/collections/create"));
  }

  @Test
  @DisplayName("showEditCollectionForm - Should display edit collection form when user is owner")
  void showEditCollectionForm_UserIsOwner_ShouldDisplayEditForm() {
    // Given
    Long collectionId = 1L;

    when(bookCollectionService.findBookCollectionById(eq(collectionId))).thenReturn(testCollectionShow);

    // When
    String viewName = bookCollectionWebController.showEditCollectionForm(collectionId, model, currentUser, principal);

    // Then
    assertEquals("layout", viewName);

    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("collection-edit"));
    verify(bookCollectionService).findBookCollectionById(eq(collectionId));
    verify(model).addAttribute(eq("bookCollection"), eq(testCollectionShow));
    verify(model).addAttribute(eq("title"), eq("BooqIn - Edit Collection"));
    verify(model).addAttribute(eq("content"), eq("pages/collections/edit"));
  }

  @Test
  @DisplayName("showEditCollectionForm - Should redirect when user is not owner")
  void showEditCollectionForm_UserIsNotOwner_ShouldRedirect() {
    // Given
    Long collectionId = 1L;
    User otherUser = mock(User.class);
    when(otherUser.getId()).thenReturn(2L); // Different user ID

    when(bookCollectionService.findBookCollectionById(eq(collectionId))).thenReturn(testCollectionShow);

    // When
    String viewName = bookCollectionWebController.showEditCollectionForm(collectionId, model, otherUser, principal);

    // Then
    assertEquals("redirect:/collections?error=notOwner", viewName);

    verify(bookCollectionService).findBookCollectionById(eq(collectionId));
  }

  @Test
  @DisplayName("showEditCollectionForm - Should handle collection not found")
  void showEditCollectionForm_CollectionNotFound_ShouldRedirect() {
    // Given
    Long collectionId = 999L;

    when(bookCollectionService.findBookCollectionById(eq(collectionId)))
            .thenThrow(new ResourceNotFoundException("Collection not found"));

    // When
    String viewName = bookCollectionWebController.showEditCollectionForm(collectionId, model, currentUser, principal);

    // Then
    assertEquals("redirect:/collections?error=notFound", viewName);

    verify(bookCollectionService).findBookCollectionById(eq(collectionId));
    verify(model).addAttribute(eq("error"), anyString());
  }

  @Test
  @DisplayName("updateCollection - Should update collection when binding has no errors and user is owner")
  void updateCollection_NoBindingErrorsAndUserIsOwner_ShouldUpdateAndRedirect() {
    // Given
    Long collectionId = 1L;
    boolean isPublic = true;
    BookCollection bookCollection = BookCollectionFactory.createNormalCollection(
            "Updated Title", "Updated Description", UserFactory.createUser(passwordEncoder, UserRole.USER), isPublic
    );
    bookCollection.setTitle("Updated Title");
    bookCollection.setDescription("Updated Description");

    when(bindingResult.hasErrors()).thenReturn(false);
    when(bookCollectionService.findBookCollectionById(eq(collectionId))).thenReturn(testCollectionShow);
    when(bookCollectionService.updateBookCollection(eq(collectionId), any(BookCollectionUpdate.class)))
            .thenReturn(testCollectionShow);

    // When
    String viewName = bookCollectionWebController.updateCollection(
            collectionId, bookCollection, bindingResult, isPublic, currentUser, model, principal);

    // Then
    assertEquals("redirect:/collections", viewName);

    verify(bindingResult).hasErrors();
    verify(bookCollectionService).findBookCollectionById(eq(collectionId));
    verify(bookCollectionService).updateBookCollection(eq(collectionId), any(BookCollectionUpdate.class));
  }

  @Test
  @DisplayName("updateCollection - Should handle binding errors")
  void updateCollection_BindingErrors_ShouldRedirect() {
    // Given
    Long collectionId = 1L;
    boolean isPublic = true;
    BookCollection bookCollection = BookCollectionFactory.createNormalCollection(
            "Updated Title", "Updated Description", UserFactory.createUser(passwordEncoder, UserRole.USER), isPublic
    );

    when(bindingResult.hasErrors()).thenReturn(true);
    when(bindingResult.getAllErrors()).thenReturn(Collections.emptyList());

    // When
    String viewName = bookCollectionWebController.updateCollection(
            collectionId, bookCollection, bindingResult, isPublic, currentUser, model, principal);

    // Then
    assertEquals("redirect:/collections?error=notFound", viewName);

    verify(bindingResult).hasErrors();
    verifyNoMoreInteractions(bookCollectionService);
  }

  @Test
  @DisplayName("updateCollection - Should handle exception")
  void updateCollection_WithException_ShouldShowError() {
    // Given
    Long collectionId = 1L;
    boolean isPublic = true;
    BookCollection bookCollection = BookCollectionFactory.createNormalCollection(
            "Updated Title", "Updated Description", UserFactory.createUser(passwordEncoder, UserRole.USER), isPublic
    );
    bookCollection.setTitle("Updated Title");
    bookCollection.setDescription("Updated Description");

    when(bindingResult.hasErrors()).thenReturn(false);
    when(bookCollectionService.findBookCollectionById(eq(collectionId))).thenReturn(testCollectionShow);
    when(bookCollectionService.updateBookCollection(eq(collectionId), any(BookCollectionUpdate.class)))
            .thenThrow(new ResourceNotFoundException("Error updating collection"));

    // When
    String viewName = bookCollectionWebController.updateCollection(
            collectionId, bookCollection, bindingResult, isPublic, currentUser, model, principal);

    // Then
    assertEquals("layout", viewName);

    verify(bindingResult).hasErrors();
    verify(bookCollectionService).findBookCollectionById(eq(collectionId));
    verify(bookCollectionService).updateBookCollection(eq(collectionId), any(BookCollectionUpdate.class));
    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("collection-edit"));
    verify(model).addAttribute(eq("error"), anyString());
    verify(model).addAttribute(eq("title"), eq("BooqIn - Error"));
    verify(model).addAttribute(eq("content"), eq("pages/collections/edit"));
  }

  @Test
  @DisplayName("deleteCollection - Should delete collection when user is owner")
  void deleteCollection_UserIsOwner_ShouldDeleteAndRedirect() {
    // Given
    Long collectionId = 1L;

    when(bookCollectionService.findBookCollectionById(eq(collectionId))).thenReturn(testCollectionShow);
    doNothing().when(bookCollectionService).deleteBookCollection(eq(collectionId));

    // When
    String viewName = bookCollectionWebController.deleteCollection(collectionId, currentUser);

    // Then
    assertEquals("redirect:/collections?success=deleted", viewName);

    verify(bookCollectionService).findBookCollectionById(eq(collectionId));
    verify(bookCollectionService).deleteBookCollection(eq(collectionId));
  }

  @Test
  @DisplayName("deleteCollection - Should redirect when user is not owner")
  void deleteCollection_UserIsNotOwner_ShouldRedirect() {
    // Given
    Long collectionId = 1L;
    User otherUser = mock(User.class);
    when(otherUser.getId()).thenReturn(2L); // Different user ID

    when(bookCollectionService.findBookCollectionById(eq(collectionId))).thenReturn(testCollectionShow);

    // When
    String viewName = bookCollectionWebController.deleteCollection(collectionId, otherUser);

    // Then
    assertEquals("redirect:/collections?error=notOwner", viewName);

    verify(bookCollectionService).findBookCollectionById(eq(collectionId));
    verify(bookCollectionService, never()).deleteBookCollection(anyLong());
  }

  @Test
  @DisplayName("deleteCollection - Should handle collection not found")
  void deleteCollection_CollectionNotFound_ShouldRedirect() {
    // Given
    Long collectionId = 999L;

    when(bookCollectionService.findBookCollectionById(eq(collectionId)))
            .thenThrow(new ResourceNotFoundException("Collection not found"));

    // When
    String viewName = bookCollectionWebController.deleteCollection(collectionId, currentUser);

    // Then
    assertEquals("redirect:/collections?error=notFound", viewName);

    verify(bookCollectionService).findBookCollectionById(eq(collectionId));
    verify(bookCollectionService, never()).deleteBookCollection(anyLong());
  }

  @Test
  @DisplayName("vote - Should cast vote and redirect for authenticated user")
  void vote_AuthenticatedUser_ShouldCastVoteAndRedirect() {
    // Given
    Long collectionId = 1L;
    VoteType voteType = VoteType.UPVOTE;

    // When
    String viewName = bookCollectionWebController.vote(collectionId, voteType, currentUser, model);

    // Then
    assertEquals("redirect:/collections/" + collectionId, viewName);

    verify(voteService).castVote(any(VoteCreate.class));
  }

  @Test
  @DisplayName("vote - Should redirect without casting vote for unauthenticated user")
  void vote_UnauthenticatedUser_ShouldRedirectWithoutCastingVote() {
    // Given
    Long collectionId = 1L;
    VoteType voteType = VoteType.UPVOTE;

    // When
    String viewName = bookCollectionWebController.vote(collectionId, voteType, null, model);

    // Then
    assertEquals("redirect:/", viewName);

    verifyNoInteractions(voteService);
  }

  @Test
  @DisplayName("removeBookFromCollection - Should redirect when collection not found")
  void removeBookFromCollection_CollectionNotFound_ShouldRedirect() {
    // Given
    Long collectionId = 999L;
    Long bookId = 1L;

    when(bookCollectionService.findBookCollectionById(eq(collectionId)))
            .thenThrow(new ResourceNotFoundException("Collection not found"));

    // When
    String viewName = bookCollectionWebController.removeBookFromCollection(collectionId, bookId, currentUser, model);

    // Then
    assertEquals("redirect:/collections/" + collectionId + "?error=notFound", viewName);

    verify(bookCollectionService).findBookCollectionById(eq(collectionId));
    verify(bookCollectionService, never()).removeBookFromCollection(anyLong(), anyLong());
  }

  @Test
  @DisplayName("addBookToCollection - Should handle collection or book not found")
  void addBookToCollection_ResourceNotFound_ShouldShowError() {
    // Given
    Long collectionId = 1L;
    Long bookId = 999L;

    when(bookCollectionService.findBookCollectionById(eq(collectionId))).thenReturn(testCollectionShow);
    when(bookService.findById(eq(bookId))).thenThrow(new ResourceNotFoundException("Book not found"));

    // When
    String viewName = bookCollectionWebController.addBookToCollection(collectionId, bookId, currentUser, model, principal);

    // Then
    assertEquals("layout", viewName);

    verify(bookCollectionService).findBookCollectionById(eq(collectionId));
    verify(bookService).findById(eq(bookId));
    verify(homeWebController).prepareModel(eq(model), eq(principal), eq("collection-add-book"));
    verify(model).addAttribute(eq("error"), anyString());
    verify(model).addAttribute(eq("title"), eq("BooqIn - Error"));
    verify(model).addAttribute(eq("content"), eq("pages/collections/addBook"));
  }
}