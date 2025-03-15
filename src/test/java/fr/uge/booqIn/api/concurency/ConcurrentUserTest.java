package fr.uge.booqIn.api.concurency;

import fr.uge.booqIn.dto.user.UserUpdate;
import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.exception.DuplicateResourceException;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.impl.UserServiceImpl;
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

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("Concurrent User Tests")
public class ConcurrentUserTest {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentUserTest.class);

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    private final List<Long> createdUserIds = new ArrayList<>();

    // Méthode d'aide pour créer une adresse de test
    private Address createTestAddress() {
        return new Address("123", "Test St", "Test City", "12345");
    }

    @BeforeEach
    public void setup() {
        // Given : création d'un utilisateur de test pour les mises à jour concurrentes
        User user = new User(
                "TestFirstName",
                "TestLastName",
                "test.concurrent.update@example.com",
                "Password123",
                createTestAddress(),
                UserRole.USER
        );
        User testUser = userRepository.saveAndFlush(user);
        createdUserIds.add(testUser.getId());
        logger.info("Created test user with ID: {}", testUser.getId());
    }

    @AfterEach
    public void cleanup() {
        // Given : suppression de tous les utilisateurs de test créés
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
    @DisplayName("Concurrent User Creation: Seule une création doit réussir")
    public void testConcurrentUserCreation() throws Exception {
        // Given
        int numThreads = 10;
        final String email = "concurrent.test@example.com";
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicReference<Long> createdUserId = new AtomicReference<>(null);

        // When : tous les threads tentent de créer un utilisateur avec le même email
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            executor.submit(() -> {
                try {
                    User user = new User(
                            "TestFirstName" + threadNum,
                            "TestLastName" + threadNum,
                            email,
                            "Password123",
                            createTestAddress(),
                            UserRole.USER
                    );
                    User savedUser = userService.createUser(user);
                    successCount.incrementAndGet();
                    createdUserId.set(savedUser.getId());
                    logger.info("Thread {} successfully created user with ID: {}", threadNum, savedUser.getId());
                } catch (DuplicateResourceException e) {
                    failureCount.incrementAndGet();
                    logger.info("Thread {} failed to create user due to duplicate email", threadNum);
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    logger.info("Thread {} failed to create user: {}", threadNum, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // Attente de la fin de tous les threads
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Then
        logger.info("Concurrent user creation test completed: {}", completed);
        logger.info("Successful creations: {}", successCount.get());
        logger.info("Failed creations: {}", failureCount.get());

        // Une seule création doit réussir
        assertEquals(1, successCount.get(), "Only one user should be successfully created");
        assertEquals(numThreads - 1, failureCount.get(), "All other creation attempts should fail");

        assertNotNull(createdUserId.get(), "A user ID should have been captured");
        User foundUser = userRepository.findById(createdUserId.get()).orElse(null);
        assertNotNull(foundUser, "The created user should exist in the database");
        assertEquals(email, foundUser.getEmail(), "The email should match");

        long count = userRepository.findAll().stream()
                .filter(u -> email.equals(u.getEmail()))
                .count();
        assertEquals(1, count, "Only one user with the test email should exist");

        // Conserver l'ID pour le nettoyage
        createdUserIds.add(createdUserId.get());
    }

    @Test
    @DisplayName("Concurrent User Patches: Mises à jour concurrentes sur le même utilisateur")
    public void testConcurrentUserPatches() throws Exception {
        // Given : récupérer l'ID de l'utilisateur créé dans le setup
        Long testUserId = createdUserIds.get(0);
        int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger concurrentModCount = new AtomicInteger(0);

        // When : plusieurs threads tentent de mettre à jour l'utilisateur simultanément
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            executor.submit(() -> {
                try {
                    UserUpdate userUpdate = new UserUpdate(
                            java.util.Optional.of("UpdatedFirstName" + threadNum),
                            java.util.Optional.empty(),
                            java.util.Optional.empty(),
                            java.util.Optional.empty(),
                            java.util.Optional.empty()
                    );
                    userService.patchUser(testUserId, userUpdate);
                    successCount.incrementAndGet();
                    logger.info("Thread {} successfully updated user", threadNum);
                } catch (ConcurrentModificationException e) {
                    concurrentModCount.incrementAndGet();
                    logger.info("Thread {} failed due to concurrent modification: {}", threadNum, e.getMessage());
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    logger.info("Thread {} failed to update user: {}", threadNum, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        // Attente de la fin de tous les threads
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Then
        logger.info("Concurrent user patch test completed: {}", completed);
        logger.info("Successful updates: {}", successCount.get());
        logger.info("Concurrent modification failures: {}", concurrentModCount.get());
        logger.info("Other failures: {}", failureCount.get());

        // Vérifier qu'au moins une mise à jour a réussi
        assertTrue(successCount.get() > 0, "At least one update should be successful");
        assertEquals(numThreads, successCount.get() + concurrentModCount.get() + failureCount.get(),
                "Total update attempts should match the number of threads");

        User updatedUser = userRepository.findById(testUserId).orElse(null);
        assertNotNull(updatedUser, "The user should still exist");
        assertTrue(updatedUser.getFirstName().startsWith("UpdatedFirstName"),
                "The first name should have been updated");

        logger.info("Final user state - FirstName: {}, Version: {}",
                updatedUser.getFirstName(), updatedUser.getVersion());
    }
}
