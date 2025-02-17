package fr.uge.booqIn.service;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.factory.BookFactory;
import fr.uge.booqIn.factory.UserFactory;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.repository.CollectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CollectionServiceTest {
  @Mock
  private CollectionRepository collectionRepository;

  @Mock
  private BookRepository bookRepository;

  @InjectMocks
  private CollectionService collectionService;

  private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
  private static final Logger logger = Logger.getLogger(CollectionServiceTest.class.getName());

  @Test
  void testBookInitialization() {
    Book book = BookFactory.createBook();
    assertNotNull(book.getCollections(), "The collections list should be initialized and not null");
  }

  @Test
  void testAddBookToCollection() {
    User user = UserFactory.createUser(passwordEncoder, UserRole.USER);
    BookCollection collection = new BookCollection("My Collection", "Description", user);
    Book book = BookFactory.createBook();
    book.setId(1L);

    when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
    when(collectionRepository.save(any(BookCollection.class))).thenReturn(collection);

    BookCollection updatedCollection = collectionService.addBookToCollection(1L, 1L, user);
    assertNotNull(updatedCollection, "The updated collection should not be null");
    assertTrue(updatedCollection.getBooks().contains(book));
  }

  @Test
  void testGetPublicCollections() {
    logger.info("Getting public collections...");
    BookCollection publicCollection1 = new BookCollection("PublicCollection_1", "DescriptionTest1", UserFactory.createUser(passwordEncoder, UserRole.USER));
    publicCollection1.setPublic(true);
    BookCollection publicCollection2 = new BookCollection("PublicCollection_2", "DescriptionTest2", UserFactory.createUser(passwordEncoder, UserRole.USER));
    publicCollection2.setPublic(true);

    List<BookCollection> publicCollections = List.of(publicCollection1, publicCollection2);
    when(collectionRepository.findByIsPublicTrue()).thenReturn(publicCollections);
    List<BookCollection> retrievedCollections = collectionService.getPublicCollections();

    assertNotNull(retrievedCollections);
    assertEquals(2, retrievedCollections.size());
    assertTrue(retrievedCollections.contains(publicCollection1));
    assertTrue(retrievedCollections.contains(publicCollection2));
    logger.info("✅ Public collections retrieved successfully (" + retrievedCollections.size() + " collections)");
    verify(collectionRepository, times(1)).findByIsPublicTrue();
  }

  @Test
  void testUpdateCollection() {
    logger.info("Testing collection update...");
    User owner = UserFactory.createUser(passwordEncoder, UserRole.USER);
    owner.setId(1L);
    BookCollection collection = new BookCollection("Title", "Description", owner);
    collection.setId(1L);
    collection.setPublic(false);

    BookCollection updatedData = new BookCollection();
    updatedData.setTitle("New Title");
    updatedData.setDescription("New Description");
    updatedData.setPublic(true);
    when(collectionRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(collection));
    when(collectionRepository.save(any(BookCollection.class))).thenReturn(collection);

    BookCollection updatedCollection = collectionService.updateCollection(1L, owner, updatedData);
    assertNotNull(updatedCollection);
    assertEquals("New Title", updatedCollection.getTitle());
    assertEquals("New Description", updatedCollection.getDescription());
    assertTrue(updatedCollection.isPublic());
    logger.info("✅ Collection " + updatedCollection.getId() + " successfully updated in service test");
    verify(collectionRepository, times(1)).save(collection);
  }

  @Test
  void testUpdateCollectionAccessDenied() {
    logger.info("Testing access denied for updating a collection...");
    User owner = UserFactory.createUser(passwordEncoder, UserRole.USER);
    owner.setId(1L);
    User otherUser = UserFactory.createUser(passwordEncoder, UserRole.USER);
    otherUser.setId(2L);
    BookCollection collection = new BookCollection("Title", "Description", owner);
    collection.setId(1L);

    when(collectionRepository.findByIdAndOwner(1L, otherUser)).thenReturn(Optional.empty());
    assertThrows(IllegalArgumentException.class, () -> {
      collectionService.updateCollection(1L, otherUser, new BookCollection());
    });

    logger.info("❌ Correctly handled access denial: Collection not found for non-owner");
    verify(collectionRepository, never()).save(any(BookCollection.class));
  }
}
