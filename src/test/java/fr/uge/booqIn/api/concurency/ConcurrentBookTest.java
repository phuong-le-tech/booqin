package fr.uge.booqIn.api.concurency;

import fr.uge.booqIn.dto.book.BookUpdate;
import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.exception.DuplicateResourceException;
import fr.uge.booqIn.repository.AuthorRepository;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.impl.BookServiceImpl;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("Concurrent Book Tests")
public class ConcurrentBookTest {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentBookTest.class);

    @Autowired
    private BookServiceImpl bookService;

    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private AuthorRepository authorRepository;
    
    @Autowired
    private UserRepository userRepository;

    private final List<Long> createdBookIds = new ArrayList<>();
    private final List<Long> createdAuthorIds = new ArrayList<>();
    private final List<Long> createdUserIds = new ArrayList<>();
    
    private User testUser;
    private Author testAuthor;

    @BeforeEach
    public void setup() {
        // Create a test user (admin for book operations)
        User user = new User(
                "BookAdmin",
                "Test",
                "book.admin@example.com",
                "Password123",
                new Address("123", "Test St", "Test City", "12345"),
                UserRole.ADMIN
        );
        testUser = userRepository.saveAndFlush(user);
        createdUserIds.add(testUser.getId());
        
        // Set up security context with the test user
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())
        );
        
        // Create test author
        Author author = new Author("Test", "AuthorMain");
        testAuthor = authorRepository.saveAndFlush(author);
        createdAuthorIds.add(testAuthor.getId());
        
        // Create a test book
        Set<Author> authors = new HashSet<>();
        authors.add(testAuthor);
        
        Book book = new Book.Builder()
                .title("Test Book")
                .authors(authors)
                .description("Test Book Description")
                .publicationDate(new Date())
                .build();
        book.addIsbn("9781234567890");
        book.addCategory("Test");
        
        Book testBook = bookRepository.saveAndFlush(book);
        createdBookIds.add(testBook.getId());
        
        logger.info("Created test book with ID: {}", testBook.getId());
        logger.info("Created test author with ID: {}", testAuthor.getId());
        logger.info("Created test user with ID: {}", testUser.getId());
    }

    @AfterEach
    public void cleanup() {
        // Clear security context
        SecurityContextHolder.clearContext();
        
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
    }

    @Test
    @DisplayName("Concurrent Book Patches: Concurrent updates on the same book")
    public void testConcurrentBookPatches() throws Exception {
        // Given: retrieve the ID of the book created in setup
        Long testBookId = createdBookIds.get(0);
        int numThreads = 10;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger concurrentModCount = new AtomicInteger(0);

        var threads = new ArrayList<Thread>();
        // When: multiple threads attempt to update the book simultaneously
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            var thread = new Thread(() -> {
                try {
                    // Set up security context for each thread
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())
                    );
                    
                    BookUpdate bookUpdate = new BookUpdate(
                            Optional.of("Updated Book Title " + threadNum),
                            Optional.empty(),
                            Optional.of("Updated Description " + threadNum),
                            Optional.empty(),
                            Optional.empty()
                    );
                    
                    bookService.patchBook(testBookId, bookUpdate);
                    successCount.incrementAndGet();
                    logger.info("Thread {} successfully updated book", threadNum);
                } catch (ConcurrentModificationException e) {
                    concurrentModCount.incrementAndGet();
                    logger.info("Thread {} failed due to concurrent modification: {}", threadNum, e.getMessage());
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    logger.info("Thread {} failed to update book: {}", threadNum, e.getMessage());
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
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

        Book updatedBook = bookRepository.findById(testBookId).orElse(null);
        assertNotNull(updatedBook, "The book should still exist");
        assertTrue(updatedBook.getTitle().startsWith("Updated Book Title"),
                "The title should have been updated");

        logger.info("Final book state - Title: {}, Version: {}",
                updatedBook.getTitle(), updatedBook.getVersion());
    }

    @Test
    @DisplayName("Concurrent Book Creation: Only one creation with the same ISBN should succeed")
    public void testConcurrentBookCreation() throws Exception {
        // Given
        int numThreads = 10;
        final String isbn = "9780987654321";

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicReference<Long> createdBookId = new AtomicReference<>(null);

        var threads = new ArrayList<Thread>();
        // When: all threads attempt to create a book with the same ISBN
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            var thread = new Thread(() -> {
                try {
                    // Set up security context for each thread
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities())
                    );
                    
                    // Create a new author for thread
                    Author author = new Author("Concurrent", "Author" + threadNum);
                    author = authorRepository.save(author);
                    
                    Set<Author> authors = new HashSet<>();
                    authors.add(author);
                    
                    // Create book with the same ISBN
                    Book book = new Book.Builder()
                            .title("Concurrent Book " + threadNum)
                            .authors(authors)
                            .description("Description for Book " + threadNum)
                            .publicationDate(new Date())
                            .build();
                    
                    // All threads use the same ISBN
                    book.addIsbn(isbn);
                    book.addCategory("Category" + threadNum);
                    
                    Book savedBook = bookService.createBook(book);
                    successCount.incrementAndGet();
                    createdBookId.set(savedBook.getId());
                    
                    // Add to cleanup list
                    synchronized (createdBookIds) {
                        createdBookIds.add(savedBook.getId());
                    }
                    synchronized (createdAuthorIds) {
                        createdAuthorIds.add(author.getId());
                    }
                    
                    logger.info("Thread {} successfully created book with ID: {}", threadNum, savedBook.getId());
                } catch (DuplicateResourceException e) {
                    failureCount.incrementAndGet();
                    logger.info("Thread {} failed to create book due to duplicate ISBN", threadNum);
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    logger.info("Thread {} failed to create book: {}", threadNum, e.getMessage());
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        logger.info("Successful creations: {}", successCount.get());
        logger.info("Failed creations: {}", failureCount.get());

        // Only one creation should succeed
        assertEquals(1, successCount.get(), "Only one book should be successfully created");
        assertEquals(numThreads - 1, failureCount.get(), "All other creation attempts should fail");

        // Verify the created book
        assertNotNull(createdBookId.get(), "A book ID should have been captured");
        Book foundBook = bookRepository.findById(createdBookId.get()).orElse(null);
        assertNotNull(foundBook, "The created book should exist in the database");
        assertTrue(foundBook.getIsbns().contains(isbn), "The book should have the test ISBN");
    }
}