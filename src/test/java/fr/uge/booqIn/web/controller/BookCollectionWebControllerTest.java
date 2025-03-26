package fr.uge.booqIn.web.controller;

import fr.uge.booqIn.controller.web.BookCollectionWebController;
import fr.uge.booqIn.controller.web.HomeWebController;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.bookCollection.BookCollectionCreate;
import fr.uge.booqIn.dto.bookCollection.BookCollectionIndex;
import fr.uge.booqIn.dto.bookCollection.BookCollectionShow;
import fr.uge.booqIn.dto.bookCollection.BookCollectionUpdate;
import fr.uge.booqIn.dto.lending.LendingResponseDTO;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.dto.vote.VoteCount;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.BookStatus;
import fr.uge.booqIn.enums.VoteType;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.mapper.BookCollectionMapper;
import fr.uge.booqIn.mapper.BookMapper;
import fr.uge.booqIn.model.BookCollection;
import fr.uge.booqIn.service.impl.BookServiceImpl;
import fr.uge.booqIn.service.interfaces.BookCollectionServiceInterface;
import fr.uge.booqIn.service.interfaces.BookLendingInterface;
import fr.uge.booqIn.service.interfaces.CommentServiceInterface;
import fr.uge.booqIn.service.interfaces.VoteServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookCollectionWebControllerTest {

  private MockMvc mockMvc;

  @Mock
  private BookCollectionServiceInterface bookCollectionService;

  @Mock
  private VoteServiceInterface voteService;

  @Mock
  private CommentServiceInterface commentService;

  @Mock
  private BookServiceImpl bookService;

  @Mock
  private HomeWebController homeWebController;

  @Mock
  private BookLendingInterface bookLendingInterface;

  @Mock
  private Model model;

  @Mock
  private Principal principal;

  @Mock
  private BindingResult bindingResult;

  @InjectMocks
  private BookCollectionWebController bookCollectionWebController;

  void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(bookCollectionWebController).build();
  }

  @Test
  void testShowPublicCollections() throws Exception {
    // Arrange
    List<BookCollectionIndex> collections = new ArrayList<>();
    BookCollectionIndex collection = mock(BookCollectionIndex.class);
    collections.add(collection);
    Page<BookCollectionIndex> page = new PageImpl<>(collections);

    when(bookCollectionService.getPublicCollections(any(PageRequest.class))).thenReturn(page);

    // Act & Assert
    mockMvc.perform(get("/collections/public")
                    .param("page", "0")
                    .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(view().name("layout"))
            .andExpect(model().attributeExists("publicCollections"))
            .andExpect(model().attribute("title", "BooqIn - Public Collections"))
            .andExpect(model().attribute("content", "pages/collections/public"));

    verify(bookCollectionService).getPublicCollections(any(PageRequest.class));
    verify(homeWebController).prepareModel(any(Model.class), any(Principal.class), eq("collections-public"));
  }
  @BeforeEach

  @Test
  void testShowUserCollections() throws Exception {
    // Arrange
    User currentUser = new User();
    currentUser.setId(1L);

    Page<BookCollectionShow> collections = new PageImpl<>(new ArrayList<>());
    Page<BookCollectionIndex> publicCollections = new PageImpl<>(new ArrayList<>());
    Page<BookCollectionIndex> privateCollections = new PageImpl<>(new ArrayList<>());

    when(bookCollectionService.getUserCollectionsWithBooks(eq(currentUser), any())).thenReturn(collections);
    when(bookCollectionService.getPublicUserCollection(eq(currentUser), any())).thenReturn(publicCollections);
    when(bookCollectionService.getPrivateUserCollection(eq(currentUser), any())).thenReturn(privateCollections);

    // Act & Assert
    mockMvc.perform(get("/collections")
                    .sessionAttr("user", currentUser))
            .andExpect(status().isOk())
            .andExpect(view().name("layout"))
            .andExpect(model().attributeExists("collections"))
            .andExpect(model().attributeExists("userPublicCollections"))
            .andExpect(model().attributeExists("userPrivateCollections"))
            .andExpect(model().attribute("title", "BooqIn - My Collections"))
            .andExpect(model().attribute("content", "pages/collections/userCollections"));

    verify(homeWebController).prepareModel(any(Model.class), any(Principal.class), eq("collections"));
  }

  @Test
  void testShowCollectionDetails_Success() throws Exception {
    // Arrange
    Long collectionId = 1L;
    User currentUser = new User();
    currentUser.setId(1L);

    UserIndex userIndex = new UserIndex(1L, "John", "Doe", "john@example.com", null, null);
    BookCollectionShow collection = new BookCollectionShow(
            collectionId, "Test Collection", "Description", true,
            new ArrayList<>(), userIndex, "NORMAL",
            LocalDateTime.now(), LocalDateTime.now());

    when(bookCollectionService.findBookCollectionById(collectionId)).thenReturn(collection);
    when(voteService.getVoteCounts(collectionId, "COLLECTION")).thenReturn(new VoteCount(collectionId, "COLLECTION", 0L, 0L, 0));
    when(commentService.countByEntity(collectionId, "COLLECTION")).thenReturn(0L);
    when(commentService.getCommentTreeByEntity(collectionId, "COLLECTION")).thenReturn(new ArrayList<>());

    LendingResponseDTO lendingData = mock(LendingResponseDTO.class);
    when(lendingData.lentBooks()).thenReturn(Collections.emptyList());
    when(bookLendingInterface.findAllUserLendingsAsDTO(currentUser.getId(), "all")).thenReturn(lendingData);

    // Act & Assert
    mockMvc.perform(get("/collections/{id}", collectionId)
                    .sessionAttr("user", currentUser))
            .andExpect(status().isOk())
            .andExpect(view().name("layout"))
            .andExpect(model().attributeExists("collection"))
            .andExpect(model().attributeExists("votes"))
            .andExpect(model().attributeExists("comments"))
            .andExpect(model().attribute("title", "BooqIn - Test Collection"))
            .andExpect(model().attribute("content", "pages/collections/details"));

    verify(bookCollectionService).findBookCollectionById(collectionId);
    verify(homeWebController).prepareModel(any(Model.class), any(Principal.class), eq("collection-details"));
  }

  @Test
  void testShowCollectionDetails_NotFound() throws Exception {
    // Arrange
    Long collectionId = 999L;
    when(bookCollectionService.findBookCollectionById(collectionId))
            .thenThrow(new ResourceNotFoundException("Collection not found"));

    // Act & Assert
    mockMvc.perform(get("/collections/{id}", collectionId))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/collections"));
  }

  @Test
  void testShowAddBookForm() throws Exception {
    // Arrange
    Long collectionId = 1L;
    BookCollectionShow collection = mock(BookCollectionShow.class);
    List<Book> books = new ArrayList<>();

    when(bookCollectionService.findBookCollectionById(collectionId)).thenReturn(collection);
    when(bookService.findAll()).thenReturn(books);

    // Act & Assert
    mockMvc.perform(get("/collections/{collectionId}/books/add", collectionId))
            .andExpect(status().isOk())
            .andExpect(view().name("layout"))
            .andExpect(model().attributeExists("collection"))
            .andExpect(model().attributeExists("books"))
            .andExpect(model().attribute("title", "BooqIn - Add Book to Collection"))
            .andExpect(model().attribute("content", "pages/collections/addBook"));

    verify(bookCollectionService).findBookCollectionById(collectionId);
    verify(homeWebController).prepareModel(any(Model.class), any(Principal.class), eq("collection-add-book"));
  }

  @Test
  void testAddBookToCollection_Success() {
    // Arrange
    Long collectionId = 1L;
    Long bookId = 2L;
    User currentUser = new User();
    currentUser.setId(1L);

    BookCollectionShow collection = mock(BookCollectionShow.class);
    when(collection.title()).thenReturn("Test Collection");

    BookShow bookShow = mock(BookShow.class);
    when(bookShow.id()).thenReturn(bookId);
    when(bookShow.title()).thenReturn("Test Book");
    when(bookShow.authors()).thenReturn(Set.of());

    when(bookCollectionService.findBookCollectionById(collectionId)).thenReturn(collection);
    when(bookService.findById(bookId)).thenReturn(bookShow);
    doNothing().when(bookCollectionService).addBookToCollection(collectionId, bookId);

    // Act
    String result = bookCollectionWebController.addBookToCollection(collectionId, bookId, currentUser, model, principal);

    // Assert
    assertEquals("redirect:/collections/" + collectionId, result);
    verify(bookCollectionService).findBookCollectionById(collectionId);
    verify(bookService).findById(bookId);
    verify(bookCollectionService).addBookToCollection(collectionId, bookId);
  }

  @Test
  void testAddBookToCollection_NotFound() {
    // Arrange
    Long collectionId = 1L;
    Long bookId = 2L;
    User currentUser = new User();

    when(bookCollectionService.findBookCollectionById(collectionId))
            .thenThrow(new ResourceNotFoundException("Collection not found"));

    // Act
    String result = bookCollectionWebController.addBookToCollection(collectionId, bookId, currentUser, model, principal);

    // Assert
    assertEquals("layout", result);
    verify(model).addAttribute(eq("error"), anyString());
    verify(model).addAttribute("title", "BooqIn - Error");
    verify(model).addAttribute("content", "pages/collections/addBook");
    verify(homeWebController).prepareModel(any(Model.class), any(Principal.class), eq("collection-add-book"));
  }

  @Test
  void testRemoveBookFromCollection_Success() {
    // Arrange
    Long collectionId = 1L;
    Long bookId = 2L;
    User currentUser = new User();
    currentUser.setId(1L);

    UserIndex userIndex = new UserIndex(1L, "John", "Doe", "john@example.com", null, null);
    BookCollectionShow collection = new BookCollectionShow(
            collectionId, "Test Collection", "Description", true,
            new ArrayList<>(), userIndex, "NORMAL",
            LocalDateTime.now(), LocalDateTime.now());

    when(bookCollectionService.findBookCollectionById(collectionId)).thenReturn(collection);
    doNothing().when(bookCollectionService).removeBookFromCollection(collectionId, bookId);

    // Act
    String result = bookCollectionWebController.removeBookFromCollection(collectionId, bookId, currentUser, model);

    // Assert
    assertEquals("redirect:/collections/" + collectionId + "?success=bookRemoved", result);
    verify(bookCollectionService).findBookCollectionById(collectionId);
    verify(bookCollectionService).removeBookFromCollection(collectionId, bookId);
  }

  @Test
  void testRemoveBookFromCollection_NotOwner() {
    // Arrange
    Long collectionId = 1L;
    Long bookId = 2L;
    User currentUser = new User();
    currentUser.setId(2L); // Different from collection owner ID

    UserIndex userIndex = new UserIndex(1L, "John", "Doe", "john@example.com", null, null);
    BookCollectionShow collection = new BookCollectionShow(
            collectionId, "Test Collection", "Description", true,
            new ArrayList<>(), userIndex, "NORMAL",
            LocalDateTime.now(), LocalDateTime.now());

    when(bookCollectionService.findBookCollectionById(collectionId)).thenReturn(collection);

    // Act
    String result = bookCollectionWebController.removeBookFromCollection(collectionId, bookId, currentUser, model);

    // Assert
    assertEquals("redirect:/collections?error=notOwner", result);
    verify(bookCollectionService).findBookCollectionById(collectionId);
    verify(bookCollectionService, never()).removeBookFromCollection(anyLong(), anyLong());
  }

  @Test
  void testShowCreateCollectionForm() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/collections/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("layout"))
            .andExpect(model().attributeExists("bookCollection"))
            .andExpect(model().attribute("title", "BooqIn - Create Collection"))
            .andExpect(model().attribute("content", "pages/collections/create"));

    verify(homeWebController).prepareModel(any(Model.class), any(Principal.class), eq("collection-create"));
  }

  @Test
  void testCreateCollection_Success() {
    // Arrange
    User currentUser = new User();
    currentUser.setId(1L);

    BookCollectionCreate bookCollectionCreate = new BookCollectionCreate("Test Collection", "Description", true, Optional.empty());
    BookCollectionShow createdCollection = mock(BookCollectionShow.class);

    when(bookCollectionService.createBookCollection(eq(bookCollectionCreate), eq(currentUser))).thenReturn(createdCollection);

    // Act
    String result = bookCollectionWebController.createCollection(bookCollectionCreate, model, principal, currentUser);

    // Assert
    assertEquals("redirect:/collections", result);
    verify(bookCollectionService).createBookCollection(eq(bookCollectionCreate), eq(currentUser));
  }

  @Test
  void testCreateCollection_Error() {
    // Arrange
    User currentUser = new User();
    currentUser.setId(1L);

    BookCollectionCreate bookCollectionCreate = new BookCollectionCreate("Test Collection", "Description", true, Optional.empty());

    when(bookCollectionService.createBookCollection(eq(bookCollectionCreate), eq(currentUser)))
            .thenThrow(new ResourceNotFoundException("Error creating collection"));

    // Act
    String result = bookCollectionWebController.createCollection(bookCollectionCreate, model, principal, currentUser);

    // Assert
    assertEquals("layout", result);
    verify(model).addAttribute(eq("error"), anyString());
    verify(model).addAttribute("title", "BooqIn - Error");
    verify(model).addAttribute("content", "pages/collections/create");
    verify(homeWebController).prepareModel(any(Model.class), any(Principal.class), eq("collection-create"));
  }

  @Test
  void testShowEditCollectionForm_Success() throws Exception {
    // Arrange
    Long collectionId = 1L;
    User currentUser = new User();
    currentUser.setId(1L);

    UserIndex userIndex = new UserIndex(1L, "John", "Doe", "john@example.com", null, null);
    BookCollectionShow collection = new BookCollectionShow(
            collectionId, "Test Collection", "Description", true,
            new ArrayList<>(), userIndex, "NORMAL",
            LocalDateTime.now(), LocalDateTime.now());

    when(bookCollectionService.findBookCollectionById(collectionId)).thenReturn(collection);

    // Act & Assert
    mockMvc.perform(get("/collections/{id}/edit", collectionId)
                    .sessionAttr("user", currentUser))
            .andExpect(status().isOk())
            .andExpect(view().name("layout"))
            .andExpect(model().attributeExists("bookCollection"))
            .andExpect(model().attribute("title", "BooqIn - Edit Collection"))
            .andExpect(model().attribute("content", "pages/collections/edit"));

    verify(bookCollectionService).findBookCollectionById(collectionId);
    verify(homeWebController).prepareModel(any(Model.class), any(Principal.class), eq("collection-edit"));
  }

  @Test
  void testShowEditCollectionForm_NotOwner() throws Exception {
    // Arrange
    Long collectionId = 1L;
    User currentUser = new User();
    currentUser.setId(2L); // Different from collection owner ID

    UserIndex userIndex = new UserIndex(1L, "John", "Doe", "john@example.com", null, null);
    BookCollectionShow collection = new BookCollectionShow(
            collectionId, "Test Collection", "Description", true,
            new ArrayList<>(), userIndex, "NORMAL",
            LocalDateTime.now(), LocalDateTime.now());

    when(bookCollectionService.findBookCollectionById(collectionId)).thenReturn(collection);

    // Act & Assert
    mockMvc.perform(get("/collections/{id}/edit", collectionId)
                    .sessionAttr("user", currentUser))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/collections?error=notOwner"));

    verify(bookCollectionService).findBookCollectionById(collectionId);
  }

  @Test
  void testUpdateCollection_Success() {
    // Arrange
    Long collectionId = 1L;
    User currentUser = new User();
    currentUser.setId(1L);
    Boolean isPublic = true;

    BookCollection bookCollection = mock(BookCollection.class);
    when(bookCollection.getId()).thenReturn(collectionId);
    when(bookCollection.getTitle()).thenReturn("Updated Title");
    when(bookCollection.getDescription()).thenReturn("Updated Description");

    UserIndex userIndex = new UserIndex(1L, "John", "Doe", "john@example.com", null, null);
    BookCollectionShow existingCollection = new BookCollectionShow(
            collectionId, "Old Title", "Old Description", false,
            new ArrayList<>(), userIndex, "NORMAL",
            LocalDateTime.now(), LocalDateTime.now());

    BookCollectionShow updatedCollection = mock(BookCollectionShow.class);

    when(bookCollectionService.findBookCollectionById(collectionId)).thenReturn(existingCollection);
    when(bindingResult.hasErrors()).thenReturn(false);
    when(bookCollectionService.updateBookCollection(eq(collectionId), any(BookCollectionUpdate.class)))
            .thenReturn(updatedCollection);

    // Act
    String result = bookCollectionWebController.updateCollection(
            collectionId, bookCollection, bindingResult, isPublic, currentUser, model, principal);

    // Assert
    assertEquals("redirect:/collections", result);
    verify(bookCollectionService).findBookCollectionById(collectionId);
    verify(bookCollectionService).updateBookCollection(eq(collectionId), any(BookCollectionUpdate.class));
  }

  @Test
  void testUpdateCollection_BindingErrors() {
    // Arrange
    Long collectionId = 1L;
    User currentUser = new User();
    currentUser.setId(1L);
    Boolean isPublic = true;

    BookCollection bookCollection = mock(BookCollection.class);
    when(bookCollection.getId()).thenReturn(collectionId);
    when(bookCollection.getTitle()).thenReturn("Updated Title");
    when(bookCollection.getDescription()).thenReturn("Updated Description");
    when(bindingResult.hasErrors()).thenReturn(true);

    // Act
    String result = bookCollectionWebController.updateCollection(
            collectionId, bookCollection, bindingResult, isPublic, currentUser, model, principal);

    // Assert
    assertEquals("redirect:/collections?error=notFound", result);
    verify(bindingResult).hasErrors();
    verify(bookCollectionService, never()).findBookCollectionById(anyLong());
  }

  @Test
  void testDeleteCollection_Success() {
    // Arrange
    Long collectionId = 1L;
    User currentUser = new User();
    currentUser.setId(1L);

    UserIndex userIndex = new UserIndex(1L, "John", "Doe", "john@example.com", null, null);
    BookCollectionShow collection = new BookCollectionShow(
            collectionId, "Test Collection", "Description", true,
            new ArrayList<>(), userIndex, "NORMAL",
            LocalDateTime.now(), LocalDateTime.now());

    when(bookCollectionService.findBookCollectionById(collectionId)).thenReturn(collection);
    doNothing().when(bookCollectionService).deleteBookCollection(collectionId);

    // Act
    String result = bookCollectionWebController.deleteCollection(collectionId, currentUser);

    // Assert
    assertEquals("redirect:/collections?success=deleted", result);
    verify(bookCollectionService).findBookCollectionById(collectionId);
    verify(bookCollectionService).deleteBookCollection(collectionId);
  }

  @Test
  void testDeleteCollection_NotOwner() {
    // Arrange
    Long collectionId = 1L;
    User currentUser = new User();
    currentUser.setId(2L); // Different from collection owner ID

    UserIndex userIndex = new UserIndex(1L, "John", "Doe", "john@example.com", null, null);
    BookCollectionShow collection = new BookCollectionShow(
            collectionId, "Test Collection", "Description", true,
            new ArrayList<>(), userIndex, "NORMAL",
            LocalDateTime.now(), LocalDateTime.now());

    when(bookCollectionService.findBookCollectionById(collectionId)).thenReturn(collection);

    // Act
    String result = bookCollectionWebController.deleteCollection(collectionId, currentUser);

    // Assert
    assertEquals("redirect:/collections?error=notOwner", result);
    verify(bookCollectionService).findBookCollectionById(collectionId);
    verify(bookCollectionService, never()).deleteBookCollection(anyLong());
  }

  @Test
  void testVote() {
    // Arrange
    Long collectionId = 1L;
    User currentUser = new User();
    currentUser.setId(1L);
    VoteType voteType = VoteType.UPVOTE;

    doNothing().when(voteService).castVote(any());

    // Act
    String result = bookCollectionWebController.vote(collectionId, voteType, currentUser, model);

    // Assert
    assertEquals("redirect:/collections/" + collectionId, result);
    verify(voteService).castVote(any());
  }
}