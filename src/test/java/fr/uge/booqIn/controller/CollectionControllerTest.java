package fr.uge.booqIn.controller;

import fr.uge.booqIn.controller.api.CollectionController;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.factory.BookFactory;
import fr.uge.booqIn.factory.UserFactory;
import fr.uge.booqIn.service.CollectionService;
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

public class CollectionControllerTest {
  @InjectMocks
  private CollectionController collectionController;

  @Mock
  private CollectionService collectionService;

  private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
  private static final Logger logger = Logger.getLogger(CollectionControllerTest.class.getName());

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testCreateCollection() {
    BookCollection collection = new BookCollection();
    collection.setTitle("My Collection");
    collection.setDescription("A test collection");

    when(collectionService.createCollection(any(BookCollection.class))).thenReturn(collection);
    ResponseEntity<BookCollection> response = collectionController.createCollection(collection);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("My Collection", response.getBody().getTitle());
    verify(collectionService, times(1)).createCollection(collection);
  }

  @Test
  void testGetCollection() {
    BookCollection collection = new BookCollection();
    collection.setId(1L);
    collection.setTitle("Test Collection");

    when(collectionService.getCollectionById(1L)).thenReturn(Optional.of(collection));
    ResponseEntity<BookCollection> response = collectionController.getCollection(1L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Test Collection", response.getBody().getTitle());
    verify(collectionService, times(1)).getCollectionById(1L);
  }

  @Test
  void testDeleteCollection() {
    User mockUser = UserFactory.createUser(passwordEncoder, UserRole.USER);
    mockUser.setId(1L);

    doNothing().when(collectionService).deleteCollection(1L, mockUser);
    ResponseEntity<Void> response = collectionController.deleteCollection(1L, mockUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(collectionService, times(1)).deleteCollection(1L, mockUser);
  }

  @Test
  void testAddBookToCollection() {
    BookCollection collection = new BookCollection();
    collection.setId(1L);
    User mockUser = UserFactory.createUser(passwordEncoder, UserRole.USER);
    mockUser.setId(1L);
    Book book = BookFactory.createBook();
    book.setId(2L);

    when(collectionService.addBookToCollection(1L, 2L, mockUser)).thenReturn(collection);
    ResponseEntity<BookCollection> response = collectionController.addBookToCollection(1L, 2L, mockUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1L, response.getBody().getId());
    verify(collectionService, times(1)).addBookToCollection(1L, 2L, mockUser);
  }

  @Test
  void testRemoveBookFromCollection() {
    BookCollection collection = new BookCollection();
    collection.setId(1L);
    User mockUser = UserFactory.createUser(passwordEncoder, UserRole.USER);
    mockUser.setId(1L);

    when(collectionService.removeBookFromCollection(1L, 2L, mockUser)).thenReturn(collection);
    ResponseEntity<BookCollection> response = collectionController.removeBookFromCollection(1L, 2L, mockUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1L, response.getBody().getId());
    verify(collectionService, times(1)).removeBookFromCollection(1L, 2L, mockUser);
  }

  @Test
  void testGetPublicCollections() {
    logger.info("Récupération des collections publiques via le contrôleur");
    logger.info("Fetching public collections. via controller...");
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
    when(collectionService.getPublicCollections()).thenReturn(publicCollections);
    ResponseEntity<List<BookCollection>> response = collectionController.getPublicCollections();

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(2, response.getBody().size());
    assertTrue(response.getBody().contains(publicCollection1));
    assertTrue(response.getBody().contains(publicCollection2));
    logger.info("✅ Public Collections Fetched Successfully (" + response.getBody().size() + " collections)");
    verify(collectionService, times(1)).getPublicCollections();
  }

  @Test
  void testUpdateCollection() {
    logger.info("Testing collection update via controller");
    BookCollection collection = new BookCollection();
    collection.setId(1L);
    collection.setTitle("Old Title");
    collection.setDescription("Old Description");
    User mockUser = UserFactory.createUser(passwordEncoder, UserRole.USER);
    mockUser.setId(1L);

    BookCollection updatedCollection = new BookCollection();
    updatedCollection.setTitle("New Title");
    updatedCollection.setDescription("New Description");
    updatedCollection.setPublic(true);
    when(collectionService.updateCollection(1L, mockUser, updatedCollection)).thenReturn(updatedCollection);
    ResponseEntity<BookCollection> response = collectionController.updateCollection(1L, mockUser, updatedCollection);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("New Title", response.getBody().getTitle());
    assertEquals("New Description", response.getBody().getDescription());
    assertTrue(response.getBody().isPublic());

    logger.info("Collection successfully updated via controller test");
    verify(collectionService, times(1)).updateCollection(1L, mockUser, updatedCollection);
  }
}
