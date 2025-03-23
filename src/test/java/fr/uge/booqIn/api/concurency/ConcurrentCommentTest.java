package fr.uge.booqIn.api.concurency;

import fr.uge.booqIn.dto.comment.CommentCreateDTO;
import fr.uge.booqIn.dto.comment.CommentDTO;
import fr.uge.booqIn.dto.comment.CommentUpdateDTO;
import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.repository.CommentRepository;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.impl.CommentServiceImpl;
import fr.uge.booqIn.service.interfaces.CommentServiceInterface;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("Concurrent Comment Tests")
public class ConcurrentCommentTest {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentCommentTest.class);

    @Autowired
    private CommentServiceInterface commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    private final List<Long> createdCommentIds = new ArrayList<>();
    private final List<Long> createdUserIds = new ArrayList<>();
    private final List<Long> createdBookIds = new ArrayList<>();

    private User testUser;
    private Book testBook;

    // Helper method to create a test address
    private Address createTestAddress() {
        return new Address("123", "Test St", "Test City", "12345");
    }

    @BeforeEach
    public void setup() {
        // Create a test user for comments
        User user = new User(
                "TestFirstName",
                "TestLastName",
                "test.concurrent.comment@example.com",
                "Password123",
                createTestAddress(),
                UserRole.USER
        );
        testUser = userRepository.saveAndFlush(user);
        createdUserIds.add(testUser.getId());
        logger.info("Created test user with ID: {}", testUser.getId());

        // Create a test book
        Book book = new Book(
                "Test Book for Concurrent Comment Tests",
                new HashSet<>(),
                "A test book created for concurrent comment tests",
                new Date()
        );
        testBook = bookRepository.saveAndFlush(book);
        createdBookIds.add(testBook.getId());
        logger.info("Created test book with ID: {}", testBook.getId());

        // Create a test comment for update tests
        CommentCreateDTO commentCreateDTO = new CommentCreateDTO(
                testUser.getId(),
                testBook.getId(),
                testBook.getEntityType(),
                "Test comment for concurrent updates",
                null
        );
        CommentDTO testComment = commentService.create(commentCreateDTO);
        createdCommentIds.add(testComment.id());
        logger.info("Created test comment with ID: {}", testComment.id());
    }

    @AfterEach
    public void cleanup() {
        // Clean up created comments
        for (Long id : createdCommentIds) {
            try {
                commentRepository.deleteById(id);
                logger.info("Deleted test comment with ID: {}", id);
            } catch (Exception e) {
                logger.warn("Could not delete test comment with ID: {}", id, e);
            }
        }
        createdCommentIds.clear();

        // Clean up created users
        for (Long id : createdUserIds) {
            try {
                userRepository.deleteById(id);
                logger.info("Deleted test user with ID: {}", id);
            } catch (Exception e) {
                logger.warn("Could not delete test user with ID: {}", id, e);
            }
        }
        createdUserIds.clear();

        // Clean up created books
        for (Long id : createdBookIds) {
            try {
                bookRepository.deleteById(id);
                logger.info("Deleted test book with ID: {}", id);
            } catch (Exception e) {
                logger.warn("Could not delete test book with ID: {}", id, e);
            }
        }
        createdBookIds.clear();
    }

    @Test
    @DisplayName("Concurrent Comment Creation: Multiple threads creating comments for the same entity")
    public void testConcurrentCommentCreation() throws Exception {
        // Given
        int numThreads = 10;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<Long> successfulCommentIds = new ArrayList<>();

        List<Thread> threads = new ArrayList<>();
        // When: All threads try to create comments for the same entity
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            var thread = new Thread(() -> {
                try {
                    CommentCreateDTO commentCreateDTO = new CommentCreateDTO(
                            testUser.getId(),
                            testBook.getId(),
                            testBook.getEntityType(),
                            "Concurrent comment test " + threadNum,
                            null
                    );
                    CommentDTO createdComment = commentService.create(commentCreateDTO);
                    successCount.incrementAndGet();
                    synchronized (successfulCommentIds) {
                        successfulCommentIds.add(createdComment.id());
                    }
                    logger.info("Thread {} successfully created comment with ID: {}", threadNum, createdComment.id());
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    logger.info("Thread {} failed to create comment: {}", threadNum, e.getMessage());
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

        // Unlike user creation, all comment creations should succeed because comments don't have uniqueness constraints
        assertEquals(numThreads, successCount.get(), "All comment creations should succeed");
        assertEquals(0, failureCount.get(), "No comment creation should fail");

        // Verify each comment exists in the database
        for (Long commentId : successfulCommentIds) {
            assertTrue(commentRepository.existsById(commentId), "Comment with ID " + commentId + " should exist");
        }

        // Add all created comments to the cleanup list
        createdCommentIds.addAll(successfulCommentIds);
    }

    @Test
    @DisplayName("Concurrent Comment Updates: Multiple threads updating the same comment")
    public void testConcurrentCommentUpdates() throws Exception {
        // Given: Use the ID of the comment created in setup
        Long testCommentId = createdCommentIds.get(0);
        int numThreads = 10;

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger concurrentModCount = new AtomicInteger(0);

        List<Thread> threads = new ArrayList<>();
        // When: Multiple threads try to update the same comment simultaneously
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            var thread = new Thread(() -> {
                try {
                    CommentUpdateDTO updateDTO = new CommentUpdateDTO(
                            "Updated content from thread " + threadNum
                    );
                    commentService.update(testCommentId, testUser.getId(), updateDTO);
                    successCount.incrementAndGet();
                    logger.info("Thread {} successfully updated comment", threadNum);
                } catch (ConcurrentModificationException e) {
                    concurrentModCount.incrementAndGet();
                    logger.info("Thread {} failed due to concurrent modification: {}", threadNum, e.getMessage());
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    logger.info("Thread {} failed to update comment: {}", threadNum, e.getMessage());
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

        // Verify that at least one update was successful
        assertTrue(successCount.get() > 0, "At least one update should be successful");
        assertEquals(numThreads, successCount.get() + concurrentModCount.get() + failureCount.get(),
                "Total update attempts should match the number of threads");

        // Verify that the comment was actually updated
        CommentDTO updatedComment = commentService.getById(testCommentId);
        assertNotNull(updatedComment, "The comment should still exist");
        assertTrue(updatedComment.content().startsWith("Updated content from thread"),
                "The comment content should have been updated");

        logger.info("Final comment state - Content: {}", updatedComment.content());
    }

    @Test
    @DisplayName("Concurrent Comment Deletion: Multiple threads trying to delete the same comment")
    public void testConcurrentCommentDeletion() throws Exception {
        // Given: Create a comment to be deleted
        CommentCreateDTO commentCreateDTO = new CommentCreateDTO(
                testUser.getId(),
                testBook.getId(),
                testBook.getEntityType(),
                "Test comment for concurrent deletion",
                null
        );
        CommentDTO commentToDelete = commentService.create(commentCreateDTO);
        Long testCommentId = commentToDelete.id();
        logger.info("Created test comment for deletion with ID: {}", testCommentId);

        int numThreads = 5;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger concurrentModCount = new AtomicInteger(0);

        List<Thread> threads = new ArrayList<>();
        // When: Multiple threads try to delete the same comment simultaneously
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            var thread = new Thread(() -> {
                try {
                    commentService.delete(testCommentId, testUser.getId());
                    successCount.incrementAndGet();
                    logger.info("Thread {} successfully deleted comment", threadNum);
                } catch (ConcurrentModificationException e) {
                    concurrentModCount.incrementAndGet();
                    logger.info("Thread {} failed due to concurrent modification: {}", threadNum, e.getMessage());
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    logger.info("Thread {} failed to delete comment: {}", threadNum, e.getMessage());
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        logger.info("Successful deletions: {}", successCount.get());
        logger.info("Concurrent modification failures: {}", concurrentModCount.get());
        logger.info("Other failures: {}", failureCount.get());

        // Only one thread should successfully delete the comment
        assertEquals(1, successCount.get(), "Only one thread should successfully delete the comment");
        assertEquals(numThreads - 1, concurrentModCount.get() + failureCount.get(),
                "All other deletion attempts should fail");

        // Verify that the comment was actually deleted
        assertFalse(commentRepository.existsById(testCommentId), "The comment should no longer exist");
    }
}