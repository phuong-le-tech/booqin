package fr.uge.booqIn.api.concurency;

import fr.uge.booqIn.dto.vote.VoteCreate;
import fr.uge.booqIn.dto.vote.VoteResponse;
import fr.uge.booqIn.dto.vote.VoteUpdate;
import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.enums.VoteType;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.repository.VoteRepository;
import fr.uge.booqIn.service.impl.VoteServiceImpl;
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
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("Concurrent Vote Tests")
public class ConcurrentVoteTest {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentVoteTest.class);

    @Autowired
    private VoteServiceImpl voteService;

    @Autowired
    private VoteRepository voteRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    // Generate unique identifiers for test data
    private final String uniqueId = UUID.randomUUID().toString().substring(0, 8);

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
        // Create a test user for votes with unique email
        User user = new User(
                "TestFirstName",
                "TestLastName",
                "test.concurrent.vote." + uniqueId + "@example.com",
                "Password123",
                createTestAddress(),
                UserRole.USER
        );
        testUser = userRepository.saveAndFlush(user);
        createdUserIds.add(testUser.getId());
        logger.info("Created test user with ID: {}", testUser.getId());

        // Create a test book to vote on
        Book book = new Book(
                "Test Book for Concurrent Vote Tests",
                new HashSet<>(),
                "A test book created for concurrent vote tests",
                new Date()
        );
        testBook = bookRepository.saveAndFlush(book);
        createdBookIds.add(testBook.getId());
        logger.info("Created test book with ID: {}", testBook.getId());
    }

    @AfterEach
    public void cleanup() {
        // Clean up created votes - safely
        if (testBook != null && testUser != null) {
            try {
                // Delete votes one by one instead of using a bulk method
              voteRepository.deleteAll(voteRepository.findAllByEntityIdAndEntityType(testBook.getId(), testBook.getEntityType()));
                logger.info("Deleted all test votes for entity: {}", testBook.getId());
            } catch (Exception e) {
                logger.warn("Could not delete test votes: {}", e.getMessage());
            }
        }

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
    @DisplayName("Concurrent Vote Creation: Multiple users voting on the same entity")
    public void testConcurrentVoteCreation() throws Exception {
        // Given
        int numThreads = 10;
        List<User> testUsers = new ArrayList<>();

        // Create multiple test users
        for (int i = 0; i < numThreads; i++) {
            User user = new User(
                    "VoteUser" + i,
                    "LastName" + i,
                    "vote.user" + i + "." + uniqueId + "@example.com",
                    "Password123",
                    createTestAddress(),
                    UserRole.USER
            );
            user = userRepository.saveAndFlush(user);
            testUsers.add(user);
            createdUserIds.add(user.getId());
            logger.info("Created test user {} with ID: {}", i, user.getId());
        }

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        List<Thread> threads = new ArrayList<>();
        // When: All threads try to create votes for the same entity with different users
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            final User currentUser = testUsers.get(i);

            var thread = new Thread(() -> {
                try {
                    VoteCreate voteCreate = new VoteCreate(
                            currentUser.getId(),
                            testBook.getId(),
                            testBook.getEntityType(),
                            threadNum % 2 == 0 ? VoteType.UPVOTE : VoteType.DOWNVOTE
                    );
                    VoteResponse createdVote = voteService.castVote(voteCreate);
                    successCount.incrementAndGet();
                    logger.info("Thread {} successfully created vote for user {}", threadNum, currentUser.getId());
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    logger.info("Thread {} failed to create vote: {}", threadNum, e.getMessage());
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        logger.info("Successful vote creations: {}", successCount.get());
        logger.info("Failed vote creations: {}", failureCount.get());

        // All vote creations should succeed because each vote is for a different user
        assertEquals(numThreads, successCount.get(), "All vote creations should succeed");
        assertEquals(0, failureCount.get(), "No vote creation should fail");

        // Verify the vote count
        long voteCount = voteRepository.countByEntityIdAndEntityType(testBook.getId(), testBook.getEntityType());
        assertEquals(numThreads, voteCount, "There should be exactly " + numThreads + " votes");
    }

    @Test
    @DisplayName("Concurrent Vote Update: Multiple threads updating the same vote")
    public void testConcurrentVoteUpdates() throws Exception {
        // Given: First create a vote to update
        VoteCreate voteCreate = new VoteCreate(
                testUser.getId(),
                testBook.getId(),
                testBook.getEntityType(),
                VoteType.UPVOTE
        );
        VoteResponse initialVote = voteService.castVote(voteCreate);
        logger.info("Created initial vote for user ID: {}", testUser.getId());

        int numThreads = 10;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger concurrentModCount = new AtomicInteger(0);

        List<Thread> threads = new ArrayList<>();
        // When: Multiple threads try to update the same vote simultaneously
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            VoteType voteType = threadNum % 2 == 0 ? VoteType.UPVOTE : VoteType.DOWNVOTE;

            var thread = new Thread(() -> {
                try {
                    VoteUpdate voteUpdate = new VoteUpdate(voteType);
                    voteService.updateVote(testUser.getId(), testBook.getId(), testBook.getEntityType(), voteUpdate);
                    successCount.incrementAndGet();
                    logger.info("Thread {} successfully updated vote to {}", threadNum, voteType);
                } catch (ConcurrentModificationException e) {
                    concurrentModCount.incrementAndGet();
                    logger.info("Thread {} failed due to concurrent modification: {}", threadNum, e.getMessage());
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    logger.info("Thread {} failed to update vote: {}", threadNum, e.getMessage());
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

        // Verify that the vote still exists and was updated
        VoteResponse finalVote = voteService.getVote(testUser.getId(), testBook.getId(), testBook.getEntityType());
        assertNotNull(finalVote, "The vote should still exist");
        logger.info("Final vote type: {}", finalVote.voteType());
    }

    @Test
    @DisplayName("Concurrent Vote Deletion: Multiple threads trying to delete the same vote")
    public void testConcurrentVoteDeletion() throws Exception {
        // Given: First create a vote to delete
        VoteCreate voteCreate = new VoteCreate(
                testUser.getId(),
                testBook.getId(),
                testBook.getEntityType(),
                VoteType.UPVOTE
        );
        VoteResponse voteToDelete = voteService.castVote(voteCreate);
        logger.info("Created vote for deletion for user ID: {}", testUser.getId());

        int numThreads = 5;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger concurrentModCount = new AtomicInteger(0);

        List<Thread> threads = new ArrayList<>();
        // When: Multiple threads try to delete the same vote simultaneously
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            var thread = new Thread(() -> {
                try {
                    voteService.deleteVote(testUser.getId(), testBook.getId(), testBook.getEntityType());
                    successCount.incrementAndGet();
                    logger.info("Thread {} successfully deleted vote", threadNum);
                } catch (ConcurrentModificationException e) {
                    concurrentModCount.incrementAndGet();
                    logger.info("Thread {} failed due to concurrent modification: {}", threadNum, e.getMessage());
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    logger.info("Thread {} failed to delete vote: {}", threadNum, e.getMessage());
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

        // Only one thread should successfully delete the vote
        assertEquals(1, successCount.get(), "Only one thread should successfully delete the vote");
        assertEquals(numThreads - 1, concurrentModCount.get() + failureCount.get(),
                "All other deletion attempts should fail");

        // Verify that the vote was actually deleted
        boolean voteExists = voteService.hasVoted(testUser.getId(), testBook.getId(), testBook.getEntityType());
        assertFalse(voteExists, "The vote should no longer exist");
    }

    @Test
    @DisplayName("Concurrent Vote Type Toggle: One user repeatedly toggling their vote type")
    public void testConcurrentVoteTypeToggle() throws Exception {
        // Create initial vote
        VoteCreate voteCreate = new VoteCreate(
                testUser.getId(),
                testBook.getId(),
                testBook.getEntityType(),
                VoteType.UPVOTE
        );
        voteService.castVote(voteCreate);
        logger.info("Created initial UPVOTE for user ID: {}", testUser.getId());

        int numToggleThreads = 20; // High number of toggles
        AtomicInteger upvoteSuccesses = new AtomicInteger(0);
        AtomicInteger downvoteSuccesses = new AtomicInteger(0);
        AtomicInteger failures = new AtomicInteger(0);

        List<Thread> threads = new ArrayList<>();

        // When: Multiple threads toggle the vote type back and forth
        for (int i = 0; i < numToggleThreads; i++) {
            final int threadNum = i;
            final VoteType toggleType = (i % 2 == 0) ? VoteType.DOWNVOTE : VoteType.UPVOTE;

            var thread = new Thread(() -> {
                try {
                    VoteUpdate voteUpdate = new VoteUpdate(toggleType);
                    VoteResponse response = voteService.updateVote(
                            testUser.getId(), testBook.getId(), testBook.getEntityType(), voteUpdate
                    );

                    if (toggleType == VoteType.UPVOTE) {
                        upvoteSuccesses.incrementAndGet();
                    } else {
                        downvoteSuccesses.incrementAndGet();
                    }

                    logger.info("Thread {} successfully set vote to {}", threadNum, toggleType);
                } catch (Exception e) {
                    failures.incrementAndGet();
                    logger.info("Thread {} failed to toggle vote: {}", threadNum, e.getMessage());
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        logger.info("UPVOTE successes: {}", upvoteSuccesses.get());
        logger.info("DOWNVOTE successes: {}", downvoteSuccesses.get());
        logger.info("Failures: {}", failures.get());

        // Ensure the vote still exists with a final state
        boolean voteExists = voteService.hasVoted(testUser.getId(), testBook.getId(), testBook.getEntityType());
        assertTrue(voteExists, "The vote should still exist after multiple toggles");

        VoteResponse finalVote = voteService.getVote(testUser.getId(), testBook.getId(), testBook.getEntityType());
        logger.info("Final vote state after toggles: {}", finalVote.voteType());

        // Assert that the total successes plus failures equals the number of threads
        assertEquals(numToggleThreads, upvoteSuccesses.get() + downvoteSuccesses.get() + failures.get(),
                "Total operations should match the number of toggle threads");
    }
}