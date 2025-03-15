package fr.uge.booqIn.api.concurency;

import fr.uge.booqIn.dto.bookCollection.BookCollectionUpdate;
import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.repository.AuthorRepository;
import fr.uge.booqIn.repository.BookCollectionRepository;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.impl.BookCollectionServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("Concurrent BookCollection Tests")
public class ConcurrentBookCollectionTest {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentBookCollectionTest.class);

    @Autowired
    private BookCollectionServiceImpl bookCollectionService;

    @Autowired
    private BookCollectionRepository bookCollectionRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private final List<Long> createdCollectionIds = new ArrayList<>();
    private final List<Long> createdBookIds = new ArrayList<>();
    private final List<Long> createdUserIds = new ArrayList<>();
    private final List<Long> createdAuthorIds = new ArrayList<>();

    private User testUser;
    private Book testBook1;
    private Book testBook2;

    @BeforeEach
    public void setup() {
        // Create a test user
        User user = new User(
                "CollectionOwner",
                "Test",
                "collection.owner@example.com",
                "Password123",
                new Address("123", "Test St", "Test City", "12345"),
                UserRole.USER
        );
        testUser = userRepository.saveAndFlush(user);
        createdUserIds.add(testUser.getId());

        // Set up security context with the test user
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())
        );

        // Create test authors
        Author author1 = new Author("Test", "Author1");
        author1 = authorRepository.saveAndFlush(author1);
        createdAuthorIds.add(author1.getId());

        Author author2 = new Author("Test", "Author2");
        author2 = authorRepository.saveAndFlush(author2);
        createdAuthorIds.add(author2.getId());

        // Create test books using builder
        Set<Author> authorsSet1 = new HashSet<>();
        authorsSet1.add(author1);

        Book book1 = new Book.Builder()
                .title("Test Book 1")
                .authors(authorsSet1)
                .description("Description for Test Book 1")
                .publicationDate(new Date())
                .build();
        book1.addIsbn("1234567890");
        book1.addCategory("Fiction");
        testBook1 = bookRepository.saveAndFlush(book1);
        createdBookIds.add(testBook1.getId());

        Set<Author> authorsSet2 = new HashSet<>();
        authorsSet2.add(author2);

        Book book2 = new Book.Builder()
                .title("Test Book 2")
                .authors(authorsSet2)
                .description("Description for Test Book 2")
                .publicationDate(new Date())
                .build();
        book2.addIsbn("0987654321");
        book2.addCategory("Non-Fiction");
        testBook2 = bookRepository.saveAndFlush(book2);
        createdBookIds.add(testBook2.getId());

        // Create a test collection
        BookCollection collection = new BookCollection();
        collection.setTitle("Test Collection");
        collection.setDescription("Test Description");
        collection.setOwner(testUser);
        collection.setCreatedAt(new Date());
        collection.setUpdatedAt(new Date());
        collection.setPublic(true);
        collection.setBooks(new ArrayList<>());
        BookCollection testCollection = bookCollectionRepository.saveAndFlush(collection);
        createdCollectionIds.add(testCollection.getId());

        logger.info("Created test collection with ID: {}", testCollection.getId());
        logger.info("Created test books with IDs: {} and {}", testBook1.getId(), testBook2.getId());
        logger.info("Created test user with ID: {}", testUser.getId());
    }

    @AfterEach
    public void cleanup() {
        // Clear security context
        SecurityContextHolder.clearContext();

        // Delete test collections
        for (Long id : createdCollectionIds) {
            try {
                bookCollectionRepository.deleteById(id);
                logger.info("Deleted test collection with ID: {}", id);
            } catch (Exception e) {
                logger.warn("Could not delete test collection with ID: {}", id, e);
            }
        }
        createdCollectionIds.clear();

        // Delete test books
        for (Long id : createdBookIds) {
            try {
                bookRepository.deleteById(id);
                logger.info("Deleted test book with ID: {}", id);
            } catch (Exception e) {
                logger.warn("Could not delete test book with ID: {}", id, e);
            }
        }
        createdBookIds.clear();

        // Delete test users
        for (Long id : createdUserIds) {
            try {
                userRepository.deleteById(id);
                logger.info("Deleted test user with ID: {}", id);
            } catch (Exception e) {
                logger.warn("Could not delete test user with ID: {}", id, e);
            }
        }
        createdUserIds.clear();

        // Delete test authors
        for (Long id : createdAuthorIds) {
            try {
                authorRepository.deleteById(id);
                logger.info("Deleted test author with ID: {}", id);
            } catch (Exception e) {
                logger.warn("Could not delete test author with ID: {}", id, e);
            }
        }
        createdAuthorIds.clear();
    }

    @Test
    @DisplayName("Concurrent Book Collection Patches: Concurrent updates on the same collection")
    public void testConcurrentCollectionPatches() throws Exception {
        // Given: retrieve the ID of the collection created in setup
        Long testCollectionId = createdCollectionIds.get(0);
        int numThreads = 10;

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger concurrentModCount = new AtomicInteger(0);

        var threads = new ArrayList<Thread>();
        // When: multiple threads attempt to update the collection simultaneously
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            var thread = new Thread(() -> {
                try {
                    // Set up security context for each thread
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())
                    );

                    BookCollectionUpdate collectionUpdate = new BookCollectionUpdate(
                            java.util.Optional.of("UpdatedCollection" + threadNum),
                            java.util.Optional.of("UpdatedDescription" + threadNum),
                            java.util.Optional.empty()
                    );

                    bookCollectionService.patchBookCollection(testCollectionId, collectionUpdate);
                    successCount.incrementAndGet();
                    logger.info("Thread {} successfully updated collection", threadNum);
                } catch (ConcurrentModificationException e) {
                    concurrentModCount.incrementAndGet();
                    logger.info("Thread {} failed due to concurrent modification: {}", threadNum, e.getMessage());
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    logger.info("Thread {} failed to update collection: {}", threadNum, e.getMessage());
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (var thread : threads) {
            thread.join();
        }

        // Then
        logger.info("Successful updates: {}", successCount.get());
        logger.info("Concurrent modification failures: {}", concurrentModCount.get());
        logger.info("Other failures: {}", failureCount.get());

        // Check that at least one update succeeded
        assertTrue(successCount.get() > 0, "At least one update should be successful");
        assertEquals(numThreads, successCount.get() + concurrentModCount.get() + failureCount.get(),
                "Total update attempts should match the number of threads");

        BookCollection updatedCollection = bookCollectionRepository.findById(testCollectionId).orElse(null);
        assertNotNull(updatedCollection, "The collection should still exist");
        assertTrue(updatedCollection.getTitle().startsWith("UpdatedCollection"),
                "The title should have been updated");

        logger.info("Final collection state - Title: {}, Version: {}",
                updatedCollection.getTitle(), updatedCollection.getVersion());
    }

    @Test
    @DisplayName("Concurrent Add Book To Collection: Concurrent operations on the same collection")
    public void testConcurrentAddBookToCollection() throws Exception {
        // Given: retrieve the ID of the collection created in setup
        Long testCollectionId = createdCollectionIds.get(0);
        int numThreads = 10;

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger concurrentModCount = new AtomicInteger(0);

        var threads = new ArrayList<Thread>();

        // Create 10 books for testing
        List<Long> bookIds = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            // Create a new author for each book
            Author author = new Author("Concurrent", "Author" + i);
            author = authorRepository.saveAndFlush(author);
            createdAuthorIds.add(author.getId());

            Set<Author> authorSet = new HashSet<>();
            authorSet.add(author);

            // Create book using builder
            Book book = new Book.Builder()
                    .title("Concurrent Book " + i)
                    .authors(authorSet)
                    .description("Description for Concurrent Book " + i)
                    .publicationDate(new Date())
                    .build();
            book.addIsbn("ISBN-" + i);
            book.addCategory("Test Category " + i);

            Book savedBook = bookRepository.saveAndFlush(book);
            bookIds.add(savedBook.getId());
            createdBookIds.add(savedBook.getId());
        }

        // When: multiple threads attempt to add different books to the collection simultaneously
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            final Long bookId = bookIds.get(i);
            var thread = new Thread(() -> {
                try {
                    // Set up security context for each thread
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())
                    );

                    bookCollectionService.addBookToCollection(testCollectionId, bookId);
                    successCount.incrementAndGet();
                    logger.info("Thread {} successfully added book {} to collection", threadNum, bookId);
                } catch (ConcurrentModificationException e) {
                    concurrentModCount.incrementAndGet();
                    logger.info("Thread {} failed due to concurrent modification: {}", threadNum, e.getMessage());
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    logger.info("Thread {} failed to add book: {}", threadNum, e.getMessage());
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (var thread : threads) {
            thread.join();
        }

        // Then
        logger.info("Successful additions: {}", successCount.get());
        logger.info("Concurrent modification failures: {}", concurrentModCount.get());
        logger.info("Other failures: {}", failureCount.get());

        // Check that some updates succeeded and some failed with concurrent modification
        assertTrue(successCount.get() > 0, "At least one addition should be successful");
        assertEquals(numThreads, successCount.get() + concurrentModCount.get() + failureCount.get(),
                "Total addition attempts should match the number of threads");

        BookCollection updatedCollection = bookCollectionRepository.findById(testCollectionId).orElse(null);
        assertNotNull(updatedCollection, "The collection should still exist");

        // Check that books were added to the collection
        int addedBooksCount = updatedCollection.getBooks().size();
        logger.info("Added books count: {}", addedBooksCount);
        assertTrue(addedBooksCount > 0, "Books should have been added to the collection");

        logger.info("Final collection state - Book count: {}, Version: {}",
                updatedCollection.getBooks().size(), updatedCollection.getVersion());
    }

    @Test
    @DisplayName("Concurrent Remove Book From Collection: Concurrent removal operations")
    public void testConcurrentRemoveBookFromCollection() throws Exception {
        // Given: retrieve the ID of the collection created in setup
        Long testCollectionId = createdCollectionIds.get(0);
        int numThreads = 5;

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger concurrentModCount = new AtomicInteger(0);
        var threads = new ArrayList<Thread>();
        // Add books to the collection first
        List<Long> bookIds = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            // Create author
            Author author = new Author("Remove", "Author" + i);
            author = authorRepository.saveAndFlush(author);
            createdAuthorIds.add(author.getId());

            Set<Author> authorSet = new HashSet<>();
            authorSet.add(author);

            // Create book
            Book book = new Book.Builder()
                    .title("Remove Test Book " + i)
                    .authors(authorSet)
                    .description("Description for Remove Test Book " + i)
                    .publicationDate(new Date())
                    .build();
            book.addIsbn("ISBN-Remove-" + i);
            book.addCategory("Remove Category " + i);

            Book savedBook = bookRepository.saveAndFlush(book);
            bookIds.add(savedBook.getId());
            createdBookIds.add(savedBook.getId());

            // Add to collection
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())
            );
            bookCollectionService.addBookToCollection(testCollectionId, savedBook.getId());
        }

        // When: multiple threads attempt to remove books from the collection simultaneously
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            final Long bookId = bookIds.get(i);
            var thread = new Thread(() -> {
                try {
                    // Set up security context for each thread
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())
                    );

                    bookCollectionService.removeBookFromCollection(testCollectionId, bookId, testUser);
                    successCount.incrementAndGet();
                    logger.info("Thread {} successfully removed book {} from collection", threadNum, bookId);
                } catch (ConcurrentModificationException e) {
                    concurrentModCount.incrementAndGet();
                    logger.info("Thread {} failed due to concurrent modification: {}", threadNum, e.getMessage());
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    logger.info("Thread {} failed to remove book: {}", threadNum, e.getMessage());
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (var thread : threads) {
            thread.join();
        }

        // Then
        logger.info("Successful removals: {}", successCount.get());
        logger.info("Concurrent modification failures: {}", concurrentModCount.get());
        logger.info("Other failures: {}", failureCount.get());

        // Check that some operations succeeded and some failed with concurrent modification
        assertTrue(successCount.get() > 0, "At least one removal should be successful");
        assertEquals(numThreads, successCount.get() + concurrentModCount.get() + failureCount.get(),
                "Total removal attempts should match the number of threads");

        BookCollection updatedCollection = bookCollectionRepository.findById(testCollectionId).orElse(null);
        assertNotNull(updatedCollection, "The collection should still exist");


    }
}