package fr.uge.booqIn.api.concurency;

import fr.uge.booqIn.entity.*;
import fr.uge.booqIn.enums.StatusPayment;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.repository.AuthorRepository;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.repository.CartRepository;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.impl.CartServiceImpl;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@DisplayName("Concurrent Cart Tests")
public class ConcurrentCartTest {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentCartTest.class);

    @Autowired
    private CartServiceImpl cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private final List<Long> createdUserIds = new ArrayList<>();
    private final List<Long> createdBookIds = new ArrayList<>();
    private final List<Long> createdAuthorIds = new ArrayList<>();
    private User testUser;
    private Book testBook;

    // Méthode d'aide pour créer une adresse de test
    private Address createTestAddress() {
        return new Address("123", "Test St", "Test City", "12345");
    }

    @BeforeEach
    public void setup() {
        // Generate a unique ISBN to avoid duplicate insertion issues.
        String uniqueIsbn = "ISBN-" + UUID.randomUUID();

        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        Author author1 = new Author("Test_" + uniqueSuffix, "Author1");
        author1 = authorRepository.saveAndFlush(author1);
        createdAuthorIds.add(author1.getId());

        Set<Author> authorsSet1 = new HashSet<>();
        authorsSet1.add(author1);

        Book book1 = new Book.Builder()
                .title("Test Book 1")
                .authors(authorsSet1)
                .description("Description for Test Book 1")
                .publicationDate(new Date())
                .build();
        // Use the generated unique ISBN instead of a hardcoded one
        book1.addIsbn(uniqueIsbn);
        book1.addCategory("Fiction");
        testBook = bookRepository.saveAndFlush(book1);
        createdBookIds.add(testBook.getId());

        User user = new User(
                "CartTestFirstName",
                "CartTestLastName",
                "cart.concurrent.test@example.com",
                "Password123",
                createTestAddress(),
                UserRole.USER
        );
        testUser = userRepository.saveAndFlush(user);
        createdUserIds.add(testUser.getId());

        // Create and associate a cart to the user
        Cart cart = new Cart();
        cart.setStatut(StatusPayment.PENDING);
        cart = cartRepository.saveAndFlush(cart);

        testUser.setCart(cart);
        testUser = userRepository.saveAndFlush(testUser);

        logger.info("Created test user with ID: {} and book with ID: {}", testUser.getId(), testBook.getId());
    }


    @AfterEach
    public void cleanup() {
        // Suppression des livres de test
        for (Long id : createdBookIds) {
            try {
                bookRepository.deleteById(id);
                logger.info("Deleted test book with ID: {}", id);
            } catch (Exception e) {
                logger.warn("Could not delete test book with ID: {}", id, e);
            }
        }
        createdBookIds.clear();

        // Suppression des utilisateurs de test
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
    @DisplayName("Concurrent Book Addition: Un seul ajout du même livre doit réussir")
    public void testConcurrentBookAddition() throws Exception {
        // Given
        int numThreads = 10;
        final Long userId = testUser.getId();
        final Long bookId = testBook.getId();
      
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        var threads = new ArrayList<Thread>();
        // When : tous les threads tentent d'ajouter le même livre au panier
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            var thread = new Thread(() -> {
                try {
                    boolean added = cartService.addBookToCart(userId, bookId);
                    if (added) {
                        successCount.incrementAndGet();
                        logger.info("Thread {} successfully added book to cart", threadNum);
                    } else {
                        failureCount.incrementAndGet();
                        logger.info("Thread {} failed to add book (already in cart)", threadNum);
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    logger.info("Thread {} failed to add book: {}", threadNum, e.getMessage());
                }
            });
            threads.add(thread);
            thread.start();
        }

        for(Thread thread : threads) {
            thread.join();
        }

        // Then
        logger.info("Successful additions: {}", successCount.get());
        logger.info("Failed additions: {}", failureCount.get());

        // Un seul ajout doit réussir
        assertEquals(1, successCount.get(), "Only one book addition should be successful");
        assertEquals(numThreads - 1, failureCount.get(), "All other addition attempts should fail");

        // Vérifier que le livre est bien dans le panier
        Cart updatedCart = cartRepository.findById(testUser.getCart().getId()).orElse(null);
        assertNotNull(updatedCart, "The cart should exist");
        assertTrue(updatedCart.getBooks().stream().anyMatch(b -> b.getId().equals(bookId)),
                "The book should be in the cart");
        assertEquals(1, updatedCart.getBooks().size(), "There should be exactly one book in the cart");
    }

    @Test
    @DisplayName("Concurrent Cart Clear: Plusieurs tentatives de vider le panier")
    public void testConcurrentCartClear() throws Exception {
        // Given
        // Ajouter un livre au panier d'abord
        cartService.addBookToCart(testUser.getId(), testBook.getId());

        int numThreads = 5;
        final Long userId = testUser.getId();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        var threads = new ArrayList<Thread>();
        // When : tous les threads tentent de vider le panier
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            var thread = new Thread(() -> {
                try {
                    boolean cleared = cartService.clearCart(userId);
                    if (cleared) {
                        successCount.incrementAndGet();
                        logger.info("Thread {} successfully cleared cart", threadNum);
                    } else {
                        failureCount.incrementAndGet();
                        logger.info("Thread {} failed to clear cart", threadNum);
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    logger.info("Thread {} failed with exception: {}", threadNum, e.getMessage());
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
        // Then
        logger.info("Successful clears: {}", successCount.get());
        logger.info("Failed clears: {}", failureCount.get());

        // Toutes les tentatives doivent réussir (car clearCart est idempotent)
        assertEquals(numThreads, successCount.get(), "All clear attempts should succeed");
        assertEquals(0, failureCount.get(), "No clear attempts should fail");

        // Vérifier que le panier est vide
        Cart updatedCart = cartRepository.findById(testUser.getCart().getId()).orElse(null);
        assertNotNull(updatedCart, "The cart should exist");
        assertTrue(updatedCart.getBooks().isEmpty(), "The cart should be empty");
        assertEquals(StatusPayment.PENDING, updatedCart.getStatus(), "The cart status should be PENDING");
    }

    @Test
    @DisplayName("Concurrent Book Removal: Plusieurs tentatives de supprimer le même livre")
    public void testConcurrentBookRemoval() throws Exception {
        // Given
        // Ajouter un livre au panier d'abord
        cartService.addBookToCart(testUser.getId(), testBook.getId());

        int numThreads = 5;
        final Long userId = testUser.getId();
        final Long bookId = testBook.getId();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        var threads = new ArrayList<Thread>();
        // When : tous les threads tentent de supprimer le même livre du panier
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            var thread = new Thread(() -> {
                try {
                    boolean removed = cartService.removeBookFromCart(userId, bookId);
                    if (removed) {
                        successCount.incrementAndGet();
                        logger.info("Thread {} successfully removed book from cart", threadNum);
                    } else {
                        failureCount.incrementAndGet();
                        logger.info("Thread {} failed to remove book (not in cart)", threadNum);
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    logger.info("Thread {} failed with exception: {}", threadNum, e.getMessage());
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        logger.info("Successful removals: {}", successCount.get());
        logger.info("Failed removals: {}", failureCount.get());

        // Le comportement exact dépend de l'implémentation de removeBookFromCart
        // Si SQL DELETE, tous peuvent réussir. Si c'est une opération JPA avec chargement et vérification, seul un peut réussir.

        // Vérifier que le livre n'est plus dans le panier
        Cart updatedCart = cartRepository.findById(testUser.getCart().getId()).orElse(null);
        assertNotNull(updatedCart, "The cart should exist");
        assertFalse(updatedCart.getBooks().stream().anyMatch(b -> b.getId().equals(bookId)),
                "The book should not be in the cart");
    }

    @Test
    @DisplayName("Concurrent Status Update: Mises à jour concurrentes du statut du panier")
    public void testConcurrentStatusUpdate() throws Exception {
        // Given
        int numThreads = 5;
        final Long cartId = testUser.getCart().getId();

        AtomicInteger completedCount = new AtomicInteger(0);
        AtomicInteger pendingCount = new AtomicInteger(0);
        AtomicReference<StatusPayment> finalStatus = new AtomicReference<>(null);

        var threads = new ArrayList<Thread>();
        // When : les threads alternent entre PENDING et COMPLETED
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            final StatusPayment status = threadNum % 2 == 0 ? StatusPayment.CONFIRMED : StatusPayment.PENDING;
            var thread = new Thread(() -> {
                try {
                    Cart updatedCart = cartService.updateCartStatus(cartId, status);
                    logger.info("Thread {} set status to {}", threadNum, status);

                    if (status == StatusPayment.CONFIRMED) {
                        completedCount.incrementAndGet();
                    } else {
                        pendingCount.incrementAndGet();
                    }

                    finalStatus.set(updatedCart.getStatus());
                } catch (Exception e) {
                    logger.info("Thread {} failed with exception: {}", threadNum, e.getMessage());
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }
        // Then
        logger.info("COMPLETED status attempts: {}", completedCount.get());
        logger.info("PENDING status attempts: {}", pendingCount.get());
        logger.info("Final status: {}", finalStatus.get());

        // Vérifier que le statut est soit PENDING soit COMPLETED (dernière mise à jour)
        Cart updatedCart = cartRepository.findById(testUser.getCart().getId()).orElse(null);
        assertNotNull(updatedCart, "The cart should exist");
        assertNotNull(updatedCart.getStatus(), "The cart status should not be null");
        assertTrue(
                updatedCart.getStatus() == StatusPayment.PENDING ||
                        updatedCart.getStatus() == StatusPayment.CONFIRMED,
                "The cart status should be either PENDING or COMPLETED"
        );

        assertEquals(finalStatus.get(), updatedCart.getStatus(),
                "The final status should match the last updated status");
    }
}