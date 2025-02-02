package fr.uge.booqIn.controller;

import fr.uge.booqIn.controller.api.CollectionController;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.factory.BookFactory;
import fr.uge.booqIn.service.CollectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CollectionControllerTest {
  @InjectMocks
  private CollectionController collectionController;

  @Mock
  private CollectionService collectionService;

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

    assertNotNull(response); // Vérifie que la réponse n'est pas null
    assertEquals(HttpStatus.OK, response.getStatusCode()); // Vérifie le code HTTP
    assertNotNull(response.getBody());
    assertEquals("My Collection", response.getBody().getTitle());

    verify(collectionService, times(1)).createCollection(collection);
  }


  @Test
  void testGetCollection() {
    BookCollection collection = new BookCollection();
    collection.setId(1L);
    collection.setTitle("Test Collection");

    when(collectionService.getCollectionById(1L)).thenReturn(collection);

    ResponseEntity<BookCollection> response = collectionController.getCollection(1L);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals("Test Collection", response.getBody().getTitle());
    verify(collectionService, times(1)).getCollectionById(1L);
  }

  @Test
  void testDeleteCollection() {
    User mockUser = new User();
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

    User mockUser = new User();
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

    User mockUser = new User();
    mockUser.setId(1L);

    when(collectionService.removeBookFromCollection(1L, 2L, mockUser)).thenReturn(collection);

    ResponseEntity<BookCollection> response = collectionController.removeBookFromCollection(1L, 2L, mockUser);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1L, response.getBody().getId());
    verify(collectionService, times(1)).removeBookFromCollection(1L, 2L, mockUser);
  }
}
