package fr.uge.booqIn.controller;

import fr.uge.booqIn.controller.api.BookCollectionController;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.factory.fixture.UserFactory;
import fr.uge.booqIn.service.BookCollectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BookCollectionControllerTest {

  @InjectMocks
  private BookCollectionController bookCollectionController;

  @Mock
  private BookCollectionService bookCollectionService;

  private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
  private static final Logger logger = Logger.getLogger(BookCollectionControllerTest.class.getName());

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  /**
   * Test de la création d'une collection.
   */
  @Test
  void testCreateCollection() {
    BookCollection collection = new BookCollection();
    collection.setTitle("My Collection");
    collection.setDescription("A test collection");

    when(bookCollectionService.createCollection(any(BookCollection.class))).thenReturn(collection);

    // Appel du contrôleur
    ResponseEntity<ApiResponse<BookCollection>> response = bookCollectionController.createCollection(collection);

    // Vérifications
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());

    ApiResponse<BookCollection> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.success());
    assertEquals("Collection created successfully", body.message());
    assertNotNull(body.data());
    assertEquals("My Collection", body.data().getTitle());

    verify(bookCollectionService, times(1)).createCollection(collection);
  }

  /**
   * Test de la récupération d'une collection existante.
   */
  @Test
  void testGetCollectionFound() {
    BookCollection collection = new BookCollection();
    collection.setId(1L);
    collection.setTitle("Test Collection");

    when(bookCollectionService.findCollectionById(1L)).thenReturn(Optional.of(collection));

    ResponseEntity<ApiResponse<BookCollection>> response = bookCollectionController.getCollection(1L);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    ApiResponse<BookCollection> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.success());
    assertEquals("Collection found", body.message());
    assertNotNull(body.data());
    assertEquals("Test Collection", body.data().getTitle());

    verify(bookCollectionService, times(1)).findCollectionById(1L);
  }

  /**
   * Test de la récupération d'une collection inexistante.
   */
  @Test
  void testGetCollectionNotFound() {
    when(bookCollectionService.findCollectionById(999L)).thenReturn(Optional.empty());

    ResponseEntity<ApiResponse<BookCollection>> response = bookCollectionController.getCollection(999L);

    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

    ApiResponse<BookCollection> body = response.getBody();
    assertNotNull(body);
    assertFalse(body.success());
    assertEquals("Collection not found", body.message());
    assertNull(body.data());

    verify(bookCollectionService, times(1)).findCollectionById(999L);
  }

  /**
   * Test de la suppression d'une collection.
   */
  @Test
  void testDeleteCollection() {
    User mockUser = UserFactory.createUser(passwordEncoder, UserRole.USER);
    mockUser.setId(1L);

    doReturn(Optional.of(new BookCollection())).when(bookCollectionService).findCollectionById(1L);
    when(bookCollectionService.deleteCollection(1L, mockUser)).thenReturn(Optional.of(new BookCollection()));

    ResponseEntity<ApiResponse<Void>> response = bookCollectionController.deleteCollection(1L, mockUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    ApiResponse<Void> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.success());
    assertEquals("Collection deleted successfully", body.message());
    assertNull(body.data());

    verify(bookCollectionService, times(1)).findCollectionById(1L);
    verify(bookCollectionService, times(1)).deleteCollection(1L, mockUser);
  }

  /**
   * Test de l'ajout d'un livre à une collection.
   */
  @Test
  void testAddBookToCollection() {
    BookCollection collection = new BookCollection();
    collection.setId(1L);
    User mockUser = UserFactory.createUser(passwordEncoder, UserRole.USER);
    mockUser.setId(1L);

    // On simule la collection trouvée
    doReturn(Optional.of(collection)).when(bookCollectionService).findCollectionById(1L);
    // On simule le retour final (collection mise à jour)
    when(bookCollectionService.addBookToCollection(1L, 2L, mockUser)).thenReturn(Optional.of(collection));

    ResponseEntity<ApiResponse<BookCollection>> response =
            bookCollectionController.addBookToCollection(1L, 2L, mockUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    ApiResponse<BookCollection> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.success());
    assertEquals("Book added to collection successfully", body.message());
    assertNotNull(body.data());
    assertEquals(1L, body.data().getId());

    verify(bookCollectionService, times(1)).findCollectionById(1L);
    verify(bookCollectionService, times(1)).addBookToCollection(1L, 2L, mockUser);
  }

  /**
   * Test du retrait d'un livre d'une collection.
   */
  @Test
  void testRemoveBookFromCollection() {
    BookCollection collection = new BookCollection();
    collection.setId(1L);
    User mockUser = UserFactory.createUser(passwordEncoder, UserRole.USER);
    mockUser.setId(1L);

    doReturn(Optional.of(collection)).when(bookCollectionService).findCollectionById(1L);
    when(bookCollectionService.removeBookFromCollection(1L, 2L, mockUser)).thenReturn(Optional.of(collection));

    ResponseEntity<ApiResponse<BookCollection>> response =
            bookCollectionController.removeBookFromCollection(1L, 2L, mockUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    ApiResponse<BookCollection> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.success());
    assertEquals("Book removed from collection successfully", body.message());
    assertNotNull(body.data());
    assertEquals(1L, body.data().getId());

    verify(bookCollectionService, times(1)).findCollectionById(1L);
    verify(bookCollectionService, times(1)).removeBookFromCollection(1L, 2L, mockUser);
  }

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

    when(bookCollectionService.getPublicCollections()).thenReturn(publicCollections);

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
    verify(bookCollectionService, times(1)).getPublicCollections();
  }

  /**
   * Test de la mise à jour d'une collection.
   */
  @Test
  void testUpdateCollection() {
    logger.info("Testing collection update via controller");

    BookCollection existing = new BookCollection();
    existing.setId(1L);
    existing.setTitle("Old Title");
    existing.setDescription("Old Description");

    User mockUser = UserFactory.createUser(passwordEncoder, UserRole.USER);
    mockUser.setId(1L);

    BookCollection updatedCollection = new BookCollection();
    updatedCollection.setTitle("New Title");
    updatedCollection.setDescription("New Description");
    updatedCollection.setPublic(true);

    // On simule que la collection existe
    doReturn(Optional.of(existing)).when(bookCollectionService).findCollectionById(1L);
    // On simule le service qui renvoie la collection mise à jour
    when(bookCollectionService.updateCollection(1L, mockUser, updatedCollection))
            .thenReturn(Optional.of(updatedCollection));

    ResponseEntity<ApiResponse<BookCollection>> response =
            bookCollectionController.updateCollection(1L, mockUser, updatedCollection);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    ApiResponse<BookCollection> body = response.getBody();
    assertNotNull(body);
    assertTrue(body.success());
    assertEquals("Collection updated successfully", body.message());
    assertNotNull(body.data());
    assertEquals("New Title", body.data().getTitle());
    assertEquals("New Description", body.data().getDescription());
    assertTrue(body.data().isPublic());

    logger.info("Collection successfully updated via controller test");
    verify(bookCollectionService, times(1)).findCollectionById(1L);
    verify(bookCollectionService, times(1)).updateCollection(1L, mockUser, updatedCollection);
  }
}
