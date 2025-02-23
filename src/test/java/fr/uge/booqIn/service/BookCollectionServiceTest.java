package fr.uge.booqIn.service;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.factory.fixture.BookFactory;
import fr.uge.booqIn.factory.fixture.UserFactory;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.repository.CollectionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookCollectionServiceTest {

  @Mock
  private CollectionRepository collectionRepository;

  @Mock
  private BookRepository bookRepository;

  @InjectMocks
  private BookCollectionService bookCollectionService;

  private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
  private static final Logger logger = Logger.getLogger(BookCollectionServiceTest.class.getName());

  /**
   * Vérifie que le Book créé par BookFactory a bien une liste de collections initialisée.
   */
  @Test
  void testBookInitialization() {
    Book book = BookFactory.createBook();
    assertNotNull(book.getCollections(),
            "The collections list should be initialized and not null");
  }

  /**
   * Test de l'ajout d'un livre à une collection existante (et ownership OK).
   */
  @Test
  void testAddBookToCollection_Success() {
    User user = UserFactory.createUser(passwordEncoder, UserRole.USER);
    BookCollection collection = new BookCollection("My Collection", "Description", user);
    Book book = BookFactory.createBook();

    when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
    when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
    when(collectionRepository.save(any(BookCollection.class))).thenReturn(collection);

    // Appel du service
    Optional<BookCollection> updatedCollectionOpt = bookCollectionService.addBookToCollection(1L, 1L, user);

    // Vérifications
    assertTrue(updatedCollectionOpt.isPresent(), "Expected non-empty Optional for success");
    BookCollection updatedCollection = updatedCollectionOpt.get();
    assertTrue(updatedCollection.getBooks().contains(book),
            "The updated collection should contain the new book");
  }

  /**
   * Test de l'ajout d'un livre à une collection introuvable (Optional.empty()).
   */
  @Test
  void testAddBookToCollection_CollectionNotFound() {
    User user = UserFactory.createUser(passwordEncoder, UserRole.USER);

    // On simule qu'on ne trouve pas la collection
    when(collectionRepository.findById(999L)).thenReturn(Optional.empty());

    Optional<BookCollection> result = bookCollectionService.addBookToCollection(999L, 1L, user);
    assertTrue(result.isEmpty(), "Should return an empty Optional if collection not found");
    verify(bookRepository, never()).findById(anyLong());  // aucune recherche de livre
    verify(collectionRepository, never()).save(any(BookCollection.class));
  }

  /**
   * Test de l'ajout d'un livre à une collection où le livre n'existe pas => Optional.empty().
   */
  @Test
  void testAddBookToCollection_BookNotFound() {
    User user = UserFactory.createUser(passwordEncoder, UserRole.USER);
    BookCollection collection = new BookCollection("My Collection", "Desc", user);

    when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
    when(bookRepository.findById(2L)).thenReturn(Optional.empty());

    Optional<BookCollection> result = bookCollectionService.addBookToCollection(1L, 2L, user);
    assertTrue(result.isEmpty(), "Should be empty if book not found");
    verify(collectionRepository, never()).save(any(BookCollection.class));
  }

  /**
   * Test de l'ajout d'un livre par un user qui n'est pas owner => AccessDeniedException.
   */
  @Test
  void testAddBookToCollection_AccessDenied() {
    User owner = UserFactory.createUser(passwordEncoder, UserRole.USER);
    User other = UserFactory.createUser(passwordEncoder, UserRole.USER);
    BookCollection collection = new BookCollection("My Collection", "Desc", owner);

    when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));

    // L'opération doit lever AccessDeniedException,
    // car other n'est pas propriétaire
    assertThrows(AccessDeniedException.class, () -> {
      bookCollectionService.addBookToCollection(1L, 1L, other);
    });

    verify(bookRepository, never()).findById(anyLong());
    verify(collectionRepository, never()).save(any(BookCollection.class));
  }

  /**
   * Test de la récupération des collections publiques.
   */
  @Test
  void testGetPublicCollections() {
    logger.info("Getting public collections...");
    BookCollection publicCollection1 = new BookCollection("Public_1", "Desc1",
            UserFactory.createUser(passwordEncoder, UserRole.USER));
    publicCollection1.setPublic(true);

    BookCollection publicCollection2 = new BookCollection("Public_2", "Desc2",
            UserFactory.createUser(passwordEncoder, UserRole.USER));
    publicCollection2.setPublic(true);

    List<BookCollection> publicCollections = List.of(publicCollection1, publicCollection2);

    when(collectionRepository.findByIsPublicTrue()).thenReturn(publicCollections);

    List<BookCollection> retrievedCollections = bookCollectionService.getPublicCollections();
    assertNotNull(retrievedCollections);
    assertEquals(2, retrievedCollections.size());
    assertTrue(retrievedCollections.contains(publicCollection1));
    assertTrue(retrievedCollections.contains(publicCollection2));
    verify(collectionRepository, times(1)).findByIsPublicTrue();
  }

  /**
   * Test de la mise à jour d'une collection (existante, ownership OK).
   */
  @Test
  void testUpdateCollection_Success() {
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

    // On simule que la collection existe
    when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
    when(collectionRepository.save(any(BookCollection.class))).thenReturn(collection);

    // Appel
    Optional<BookCollection> updatedOpt = bookCollectionService.updateCollection(1L, owner, updatedData);

    // Vérifications
    assertTrue(updatedOpt.isPresent(), "Collection should be found and updated");
    BookCollection updatedCollection = updatedOpt.get();
    assertEquals("New Title", updatedCollection.getTitle());
    assertEquals("New Description", updatedCollection.getDescription());
    assertTrue(updatedCollection.isPublic());
    verify(collectionRepository, times(1)).save(collection);
  }

  /**
   * Test de la mise à jour d'une collection inexistante => Optional.empty().
   */
  @Test
  void testUpdateCollection_NotFound() {
    logger.info("Testing updateCollection with non-existing collection...");
    User owner = UserFactory.createUser(passwordEncoder, UserRole.USER);
    owner.setId(1L);

    when(collectionRepository.findById(999L)).thenReturn(Optional.empty());

    Optional<BookCollection> result = bookCollectionService.updateCollection(999L, owner, new BookCollection());
    assertTrue(result.isEmpty(), "Should return empty Optional if collection does not exist");

    verify(collectionRepository, never()).save(any(BookCollection.class));
  }

  /**
   * Test de la mise à jour par un user qui n'est pas owner => AccessDeniedException.
   */
  @Test
  void testUpdateCollection_AccessDenied() {
    logger.info("Testing access denied for updating a collection...");

    User owner = UserFactory.createUser(passwordEncoder, UserRole.USER);
    owner.setId(1L);

    User otherUser = UserFactory.createUser(passwordEncoder, UserRole.USER);
    otherUser.setId(2L);

    BookCollection collection = new BookCollection("Title", "Description", owner);
    collection.setId(1L);

    // On simule qu'on trouve bien la collection,
    // mais verifyOwnership va lever AccessDeniedException
    when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));

    assertThrows(AccessDeniedException.class, () -> {
      bookCollectionService.updateCollection(1L, otherUser, new BookCollection());
    });

    verify(collectionRepository, never()).save(any(BookCollection.class));
  }
}
