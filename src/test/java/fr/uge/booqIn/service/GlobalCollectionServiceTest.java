package fr.uge.booqIn.service;

import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.repository.AuthorRepository;
import fr.uge.booqIn.repository.BookCollectionRepository;
import fr.uge.booqIn.repository.BookRepository;
import org.junit.jupiter.api.AfterEach;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class GlobalCollectionServiceTest {

  @Autowired
  private GlobalCollectionService globalCollectionService;

  @Autowired
  private BookCollectionRepository bookCollectionRepository;

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private AuthorRepository authorRepository;

  @Test
  @Transactional
  void testUpdateAuthorCollections() {
    // Préparation des données de test
    Author author = new Author("Stephen", "King");
    author = authorRepository.save(author);

    Book book1 = new Book.Builder()
            .title("The Shining")
            .description("A horror novel")
            .publicationDate(new Date())
            .authors(Set.of(author))
            .createdAt(new Date())
            .updatedAt(new Date())
            .build();

    Book book2 = new Book.Builder()
            .title("It")
            .description("Another horror novel")
            .publicationDate(new Date())
            .authors(Set.of(author))
            .createdAt(new Date())
            .updatedAt(new Date())
            .build();

    // Sauvegarder les livres
    book1 = bookRepository.save(book1);
    book2 = bookRepository.save(book2);

    // Assurez-vous que l'auteur est bien associé aux livres
    author.getBooks().add(book1);
    author.getBooks().add(book2);
    author = authorRepository.save(author);

    // Exécution de la méthode à tester
    globalCollectionService.updateAuthorCollections();

    // Vérification des résultats
    String collectionTitle = "Livres de Stephen King";
    Optional<BookCollection> collectionOpt = bookCollectionRepository
            .findByTitleAndCollectionType(collectionTitle, BookCollection.CollectionType.GLOBAL);

    assertTrue(collectionOpt.isPresent());
    BookCollection collection = collectionOpt.get();
    assertEquals(collectionTitle, collection.getTitle());
    assertTrue(collection.isPublic());
    assertTrue(collection.isSmart());
    assertEquals(BookCollection.CollectionType.GLOBAL, collection.getCollectionType());
    assertEquals(2, collection.getBooks().size());
    assertTrue(collection.getBooks().contains(book1));
    assertTrue(collection.getBooks().contains(book2));
  }

  @Test
  @Transactional
  void testUpdateRecentBooksCollection() {
    // Préparation des données de test
    // Création de livres récents (moins de 30 jours)
    Book recentBook1 = new Book.Builder()
            .title("Recent Book 1")
            .description("A recent book")
            .publicationDate(new Date())
            .build();

    Book recentBook2 = new Book.Builder()
            .title("Recent Book 2")
            .description("Another recent book")
            .publicationDate(new Date())
            .build();

    // Création d'un livre ancien (plus de 30 jours)
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, -40);

    Book oldBook = new Book.Builder()
            .title("Old Book")
            .description("An old book")
            .publicationDate(new Date())
            .createdAt(calendar.getTime())
            .build();

    // Sauvegarde des livres
    bookRepository.saveAll(List.of(recentBook1, recentBook2, oldBook));

    // Exécution de la méthode à tester
    globalCollectionService.updateRecentBooksCollection();

    // Vérification des résultats
    Optional<BookCollection> collectionOpt = bookCollectionRepository
            .findByTitleAndCollectionType("Nouveautés des 30 derniers jours", BookCollection.CollectionType.GLOBAL);

    assertTrue(collectionOpt.isPresent(), "Recent books collection should be created");
    BookCollection collection = collectionOpt.get();
    assertEquals("Nouveautés des 30 derniers jours", collection.getTitle());
    assertTrue(collection.isPublic());
    assertTrue(collection.isSmart());
    assertEquals(BookCollection.CollectionType.GLOBAL, collection.getCollectionType());

    // Vérifier que seuls les livres récents sont inclus
    assertEquals(2, collection.getBooks().size(), "Only recent books should be included");
    assertTrue(collection.getBooks().contains(recentBook1));
    assertTrue(collection.getBooks().contains(recentBook2));
    assertFalse(collection.getBooks().contains(oldBook), "Old books should not be included");
  }

  @Test
  @Transactional
  void testUpdateExistingCollections() {
    // Préparation: Créer une collection existante
    BookCollection existingCollection = new BookCollection();
    existingCollection.setTitle("Livres de J.K. Rowling");
    existingCollection.setDescription("Test collection");
    existingCollection.setPublic(true);
    existingCollection.setSmart(true);
    existingCollection.setCollectionType(BookCollection.CollectionType.GLOBAL);
    existingCollection.setBooks(new ArrayList<>());
    existingCollection.setCreatedAt(new Date());
    existingCollection.setUpdatedAt(new Date());
    existingCollection = bookCollectionRepository.save(existingCollection);

    // Créer un auteur et des livres
    Author author = new Author("J.K.", "Rowling");
    author = authorRepository.save(author);

    Book book = new Book.Builder()
            .title("Harry Potter")
            .description("A fantasy novel")
            .publicationDate(new Date())
            .authors(Set.of(author))
            .build();

    book = bookRepository.save(book);
    author.getBooks().add(book);

    // Exécution
    globalCollectionService.updateAuthorCollections();

    // Vérification que la collection existante a été mise à jour
    BookCollection updatedCollection = bookCollectionRepository.findById(existingCollection.getId()).orElseThrow();
    assertEquals(1, updatedCollection.getBooks().size());
    assertTrue(updatedCollection.getBooks().contains(book));
  }

  @AfterEach
  void cleanup() {
    // Nettoyer les données de test
    bookCollectionRepository.deleteAll();
    bookRepository.deleteAll();
    authorRepository.deleteAll();
  }
}
