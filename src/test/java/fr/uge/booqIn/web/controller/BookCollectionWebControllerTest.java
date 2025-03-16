package fr.uge.booqIn.web.controller;

import fr.uge.booqIn.controller.api.BookCollectionController;
import fr.uge.booqIn.dto.bookCollection.BookCollectionShow;
import fr.uge.booqIn.dto.bookCollection.BookCollectionUpdate;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.factory.fixture.UserFactory;
import fr.uge.booqIn.mapper.BookCollectionMapper;
import fr.uge.booqIn.service.interfaces.BookCollectionServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class BookCollectionWebControllerTest {

  @InjectMocks
  private BookCollectionController bookCollectionController;

  @Mock
  private BookCollectionServiceInterface bookCollectionService;

  private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
  private static final Logger logger = Logger.getLogger(BookCollectionWebControllerTest.class.getName());
  private final BookCollectionMapper bookCollectionMapper = new BookCollectionMapper();

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Test de la création d'une collection.
   */
  /*@Test
  void testCreateCollection() {
    BookCollection collection = new BookCollection();
    collection.setTitle("My Collection");
    collection.setDescription("A test collection");

    User mockUser = UserFactory.createUser(passwordEncoder, UserRole.USER);
    mockUser.setId(1L);

    when(bookCollectionService.createBookCollection(any(BookCollection.class))).thenReturn(collection);

    // Appel du contrôleur
    ResponseEntity<ApiResponse<BookCollection>> response = bookCollectionController.createCollection(collection, mockUser);

    // Vérifications
    assertNotNull(response);
    assertEquals(HttpStatus.CREATED, response.getStatusCode());

    ApiResponse<BookCollection> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.success());
    assertEquals("Collection created successfully", body.message());
    assertNotNull(body.data());
    assertEquals("My Collection", body.data().getTitle());

    verify(bookCollectionService, times(1)).createBookCollection(collection);
  }*/

  /**
   * Test de la récupération d'une collection existante.
   */
  @Test
  void testGetCollectionFound() {
    BookCollectionShow bookCollectionShow = mock(BookCollectionShow.class);
    when(bookCollectionShow.title()).thenReturn("Test Collection");

    when(bookCollectionService.findById(1L)).thenReturn(bookCollectionShow);

    ResponseEntity<ApiResponse<BookCollectionShow>> response = bookCollectionController.getCollection(1L);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    ApiResponse<BookCollectionShow> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.success());
    assertEquals("Collection found", body.message());
    assertNotNull(body.data());
    assertEquals("Test Collection", body.data().title());

    verify(bookCollectionService, times(1)).findById(1L);
  }

  /**
   * Test de la récupération d'une collection inexistante.
   */
  @Test
  void testGetCollectionNotFound() {
    when(bookCollectionService.findById(999L)).thenThrow(new ResourceNotFoundException("BookCollection", "id", 999L));

    try {
      bookCollectionController.getCollection(999L);
      fail("Une exception ResourceNotFoundException aurait dû être levée");
    } catch (ResourceNotFoundException e) {
      // Test réussi : l'exception est bien capturée
      verify(bookCollectionService, times(1)).findById(999L);
    }
  }

  /**
   * Test de la suppression d'une collection.
   */
  @Test
  void testDeleteCollection() {
    User mockUser = UserFactory.createUser(passwordEncoder, UserRole.USER);
    mockUser.setId(1L);

    // Ne lève pas d'exception -> suppression réussie
    doNothing().when(bookCollectionService).deleteBookCollection(1L);

    ResponseEntity<ApiResponse<Void>> response = bookCollectionController.deleteCollection(1L, mockUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    ApiResponse<Void> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.success());
    assertEquals("Collection deleted successfully", body.message());
    assertNull(body.data());

    verify(bookCollectionService, times(1)).deleteBookCollection(1L);
  }

  /**
   * Test de la suppression d'une collection - erreur d'autorisation.
   */
  @Test
  void testDeleteCollectionUnauthorized() {
    User mockUser = UserFactory.createUser(passwordEncoder, UserRole.USER);
    mockUser.setId(1L);

    // Simule une erreur d'autorisation
    doThrow(new AccessDeniedException("You don't have permission to delete this collection"))
            .when(bookCollectionService).deleteBookCollection(1L);

    try {
      bookCollectionController.deleteCollection(1L, mockUser);
      fail("Une exception AccessDeniedException aurait dû être levée");
    } catch (AccessDeniedException e) {
      // Test réussi : l'exception est bien capturée
      assertEquals("You don't have permission to delete this collection", e.getMessage());
      verify(bookCollectionService, times(1)).deleteBookCollection(1L);
    }
  }

  /**
   * Test de l'ajout d'un livre à une collection.
   */
  /*@Test
  void testAddBookToCollection() {
    BookCollection collection = new BookCollection();
    collection.setId(1L);

    // Simule le retour final (collection mise à jour)
    when(bookCollectionService.addBookToCollection(1L, 2L)).thenReturn(collection);

    ResponseEntity<ApiResponse<BookCollection>> response =
            bookCollectionController.addBookToCollection(1L, 2L);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    ApiResponse<BookCollection> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.success());
    assertEquals("Book added to collection successfully", body.message());
    assertNotNull(body.data());
    assertEquals(1L, body.data().getId());

    verify(bookCollectionService, times(1)).addBookToCollection(1L, 2L);
  }*/

  /**
   * Test de l'ajout d'un livre à une collection - collection non trouvée.
   */
  @Test
  void testAddBookToCollectionNotFound() {
    // Simule une collection non trouvée
    when(bookCollectionService.addBookToCollection(999L, 2L))
            .thenThrow(new ResourceNotFoundException("BookCollection", "id", 999L));

    try {
      bookCollectionController.addBookToCollection(999L, 2L);
      fail("Une exception ResourceNotFoundException aurait dû être levée");
    } catch (ResourceNotFoundException e) {
      // Test réussi : l'exception est bien capturée
      assertTrue(e.getMessage().contains("not found"));
      verify(bookCollectionService, times(1)).addBookToCollection(999L, 2L);
    }
  }

  /**
   * Test du retrait d'un livre d'une collection.
   */
  /*@Test
  void testRemoveBookFromCollection() {
    BookCollection collection = new BookCollection();
    collection.setId(1L);
    User mockUser = UserFactory.createUser(passwordEncoder, UserRole.USER);
    mockUser.setId(1L);

    when(bookCollectionService.removeBookFromCollection(1L, 2L, mockUser)).thenReturn(collection);

    ResponseEntity<ApiResponse<BookCollection>> response =
            bookCollectionController.removeBookFromCollection(1L, 2L, mockUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    ApiResponse<BookCollection> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.success());
    assertEquals("Book removed from collection successfully", body.message());
    assertNotNull(body.data());
    assertEquals(1L, body.data().getId());

    verify(bookCollectionService, times(1)).removeBookFromCollection(1L, 2L, mockUser);
  }*/

  /**
   * Test de la récupération des collections publiques.
   */
  @Test
  void testGetPublicCollections() {
    logger.info("Fetching public collections via controller test...");

    BookCollection publicCollection1 = new BookCollection();
    publicCollection1.setId(1L);
    publicCollection1.setTitle("PublicCollection_1");
    publicCollection1.setDescription("DescriptionTest1");
    publicCollection1.setPublic(true);

    BookCollection publicCollection2 = new BookCollection();
    publicCollection2.setId(2L);
    publicCollection2.setTitle("PublicCollection_2");
    publicCollection2.setDescription("DescriptionTest2");
    publicCollection2.setPublic(true);

    List<BookCollection> publicCollections = List.of(publicCollection1, publicCollection2);
    // TODO: à corriger
    /*when(bookCollectionService.getPublicCollections()).thenReturn(publicCollections);

    // Maintenant le contrôleur renvoie ResponseEntity<ApiResponse<List<BookCollection>>>
    ResponseEntity<ApiResponse<List<BookCollection>>> response = bookCollectionController.getPublicCollections();

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<List<BookCollection>> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.success());
    assertEquals("Public collections fetched", body.message());
    assertNotNull(body.data());
    assertEquals(2, body.data().size());
    assertTrue(body.data().contains(publicCollection1));
    assertTrue(body.data().contains(publicCollection2));

    logger.info("✅ Public Collections Fetched Successfully (" + body.data().size() + " collections)");
    verify(bookCollectionService, times(1)).getPublicCollections();*/
  }

  /**
   * Test de la récupération des collections d'un utilisateur.
   */
  @Test
  void testGetUserCollections() {
    User mockUser = UserFactory.createUser(passwordEncoder, UserRole.USER);
    mockUser.setId(1L);

    BookCollection collection1 = new BookCollection();
    collection1.setId(1L);
    collection1.setTitle("User Collection 1");

    BookCollection collection2 = new BookCollection();
    collection2.setId(2L);
    collection2.setTitle("User Collection 2");

    List<BookCollection> userCollections = List.of(collection1, collection2);

    when(bookCollectionService.getUserCollections(mockUser)).thenReturn(userCollections);

    ResponseEntity<ApiResponse<List<BookCollectionShow>>> response = bookCollectionController.getUserCollections(mockUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<List<BookCollectionShow>> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.success());
    assertEquals("User collections fetched", body.message());
    assertEquals(2, body.data().size());

    verify(bookCollectionService, times(1)).getUserCollections(mockUser);
  }

  /**
   * Test de la récupération d'un livre dans une collection.
   */
  @Test
  void testGetBookFromCollection() {
    Book book = new Book();
    book.setId(2L);
    book.setTitle("Test Book");

    when(bookCollectionService.getBookFromCollection(1L, 2L)).thenReturn(book);

    ResponseEntity<ApiResponse<Book>> response = bookCollectionController.getBookFromCollection(1L, 2L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<Book> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.success());
    assertEquals("Book fetched", body.message());
    assertEquals("Test Book", body.data().getTitle());

    verify(bookCollectionService, times(1)).getBookFromCollection(1L, 2L);
  }

  /**
   * Test de la mise à jour d'une collection.
   */
  @Test
  void testPatchCollection() {
    logger.info("Testing collection update via controller");

    User mockUser = UserFactory.createUser(passwordEncoder, UserRole.USER);
    mockUser.setId(1L);

    BookCollectionShow bookCollectionShow = mock(BookCollectionShow.class);
    when(bookCollectionShow.title()).thenReturn("New Title");
    when(bookCollectionShow.description()).thenReturn("New Description");

    BookCollectionUpdate bookCollectionUpdate = mock(BookCollectionUpdate.class);

    // On simule le service qui renvoie la collection mise à jour
    when(bookCollectionService.patchBookCollection(eq(1L), any(BookCollectionUpdate.class)))
            .thenReturn(bookCollectionShow);

    ResponseEntity<ApiResponse<BookCollectionShow>> response =
            bookCollectionController.patchCollection(1L, mockUser, bookCollectionUpdate);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    ApiResponse<BookCollectionShow> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.success());
    assertEquals("Collection updated successfully", body.message());
    assertNotNull(body.data());
    assertEquals("New Title", body.data().title());
    assertEquals("New Description", body.data().description());

    logger.info("Collection successfully updated via controller test");
    verify(bookCollectionService, times(1)).patchBookCollection(eq(1L), any(BookCollectionUpdate.class));
  }

  /**
   * Test de la mise à jour complète d'une collection.
   */
  @Test
  void testUpdateCollection() {
    BookCollection updateRequest = new BookCollection();
    updateRequest.setId(1L);
    updateRequest.setTitle("Updated Title");
    updateRequest.setDescription("Updated Description");

    BookCollection updatedCollection = new BookCollection();
    updatedCollection.setId(1L);
    updatedCollection.setTitle("Updated Title");
    updatedCollection.setDescription("Updated Description");

    when(bookCollectionService.updateBookCollection(any(BookCollection.class))).thenReturn(updatedCollection);

    ResponseEntity<ApiResponse<BookCollection>> response = bookCollectionController.updateCollection(updateRequest);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<BookCollection> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.success());
    assertEquals("BookCollection updated successfully", body.message());
    assertEquals("Updated Title", body.data().getTitle());
    assertEquals("Updated Description", body.data().getDescription());

    verify(bookCollectionService, times(1)).updateBookCollection(any(BookCollection.class));
  }
}