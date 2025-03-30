package fr.uge.booqIn.api.concurency;

import fr.uge.booqIn.dto.auth.AuthResponse;
import fr.uge.booqIn.dto.auth.LoginDto;
import fr.uge.booqIn.dto.auth.RegisterDto;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.entity.Address;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.JwtService;
import fr.uge.booqIn.service.impl.AuthServiceImpl;
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("Concurrent Auth Tests")
public class ConcurrentAuthTest {

    private static final Logger logger = LoggerFactory.getLogger(ConcurrentAuthTest.class);

    @Autowired
    private AuthServiceImpl authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    private final List<String> createdUserEmails = new ArrayList<>();
    private final AtomicReference<String> testRefreshToken = new AtomicReference<>(null);

    @BeforeEach
    public void setup() {
        // Given: création d'un utilisateur de test pour les connexions concurrentes
        RegisterDto registerDto = new RegisterDto(
                "TestAuth",
                "User",
                "test.auth.concurrent@example.com",
                "Password123",
                createTestAddress(),
                UserRole.USER
        );
        try {
            UserIndex testUser = authService.register(registerDto);
            createdUserEmails.add(testUser.email());
            logger.info("Created test user with email: {}", testUser.email());
        } catch (Exception e) {
            logger.warn("Error setting up test user: {}", e.getMessage());
        }
    }

    @AfterEach
    public void cleanup() {
        // Given: suppression de tous les utilisateurs de test créés
        for (String email : createdUserEmails) {
            try {
                Optional<User> user = userRepository.findByEmail(email);
                user.ifPresent(u -> userRepository.deleteById(u.getId()));
                logger.info("Deleted test user with email: {}", email);
            } catch (Exception e) {
                logger.warn("Could not delete test user with email: {}", email, e);
            }
        }
        createdUserEmails.clear();
        testRefreshToken.set(null);
    }

    // Méthode d'aide pour créer une adresse de test
    private Address createTestAddress() {
        return new Address("123", "Test St", "Test City", "12345");
    }

    // Méthode d'aide pour créer un RegisterDto avec les mêmes paramètres de base
    private RegisterDto createRegisterDto(String firstName, String lastName, String email) {
        return new RegisterDto(firstName, lastName, email, "Password123", createTestAddress(), UserRole.USER);
    }

    @Test
    @DisplayName("Concurrent Registration: Seule une inscription doit réussir")
    public void testConcurrentRegistration() throws Exception {
        // Given
        int numThreads = 10;
        String baseEmail = "concurrent.register.test@example.com";

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        var threads = new ArrayList<Thread>();
        // When: tous les threads tentent de s'inscrire avec le même email
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            final String email = baseEmail; // même email pour tous
            var thread = new Thread(() -> {
                try {
                    RegisterDto registerDto = createRegisterDto("ConcurrentUser" + threadNum, "Test" + threadNum, email);
                    UserIndex user = authService.register(registerDto);
                    if (user != null && user.id() != null) {
                        successCount.incrementAndGet();
                        createdUserEmails.add(email);
                        logger.info("Thread {} successfully registered user", threadNum);
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    logger.info("Thread {} failed to register user: {}", threadNum, e.getMessage());
                }
            });
            threads.add(thread);
            thread.start();
        }

        for(Thread thread : threads) {
            thread.join();
        }


        // Then
        logger.info("Successful registrations: {}", successCount.get());
        logger.info("Failed registrations: {}", failureCount.get());

        assertEquals(1, successCount.get(), "Only one registration should succeed");
        assertEquals(numThreads - 1, failureCount.get(), "All other registrations should fail");

        // Vérifier qu'un seul utilisateur avec cet email existe dans la BDD
        Optional<User> user = userRepository.findByEmail(baseEmail);
        assertTrue(user.isPresent(), "User should exist in the database");
        assertEquals(baseEmail, user.get().getEmail(), "Email should match");
    }

    @Test
    @DisplayName("Concurrent Login: Toutes les connexions doivent réussir")
    public void testConcurrentLogin() throws Exception {
        // Given
        int numThreads = 10;

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicReference<String> firstRefreshToken = new AtomicReference<>(null);

        var threads = new ArrayList<Thread>();
        // When: tous les threads se connectent simultanément
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            var thread = new Thread(() -> {
                try {
                    LoginDto loginDto = new LoginDto("test.auth.concurrent@example.com", "Password123");
                    AuthResponse response = authService.login(loginDto);
                    if (response != null && response.accessToken() != null) {
                        successCount.incrementAndGet();
                        logger.info("Thread {} successfully logged in", threadNum);
                        firstRefreshToken.compareAndSet(null, response.refreshToken());
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    logger.info("Thread {} failed to login: {}", threadNum, e.getMessage());
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        logger.info("Successful logins: {}", successCount.get());
        logger.info("Failed logins: {}", failureCount.get());

        assertEquals(numThreads, successCount.get(), "All logins should succeed");
        assertEquals(0, failureCount.get(), "No logins should fail");

        // Sauvegarder le premier refresh token pour le prochain test
        testRefreshToken.set(firstRefreshToken.get());
        assertNotNull(testRefreshToken.get(), "Should have captured a refresh token");
    }

    /*@Test
    @DisplayName("Concurrent Token Refresh: Seul le premier rafraîchissement doit réussir")
    public void testConcurrentTokenRefresh() throws Exception {
        // Given: s'assurer d'avoir un refresh token valide
        if (testRefreshToken.get() == null) {
            LoginDto loginDto = new LoginDto("test.auth.concurrent@example.com", "Password123");
            AuthResponse response = authService.login(loginDto);
            testRefreshToken.set(response.refreshToken());
        }
        assertNotNull(testRefreshToken.get(), "Refresh token should be available");

        int numThreads = 5;

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger tokenReuseCount = new AtomicInteger(0);
        String refreshToken = testRefreshToken.get();

        var threads = new ArrayList<Thread>();
        // When: tous les threads tentent de rafraîchir le même token
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            var thread = new Thread(() -> {
                try {

                    AuthResponse response = authService.refreshToken(refreshToken);
                    if (response != null && response.accessToken() != null) {
                        successCount.incrementAndGet();
                        logger.info("Thread {} successfully refreshed token", threadNum);
                    }
                } catch (IllegalStateException e) {
                    if (e.getMessage().contains("Token reuse detected")) {
                        tokenReuseCount.incrementAndGet();
                        logger.info("Thread {} detected token reuse", threadNum);
                    } else {
                        failureCount.incrementAndGet();
                        logger.info("Thread {} failed with state error: {}", threadNum, e.getMessage());
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    logger.info("Thread {} failed to refresh token: {}", threadNum, e.getMessage());
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        logger.info("Successful refreshes: {}", successCount.get());
        logger.info("Token reuse detections: {}", tokenReuseCount.get());
        logger.info("Other failures: {}", failureCount.get());

        // Seul le premier rafraîchissement doit réussir, les autres doivent échouer pour cause de réutilisation de token
        assertEquals(1, successCount.get(), "Only one refresh should succeed");
        assertEquals(numThreads - 1, tokenReuseCount.get() + failureCount.get(), "All other refreshes should fail");
    }*/

    @Test
    @DisplayName("Concurrent Logout: Toutes les opérations de logout doivent réussir")
    public void testConcurrentLogout() throws Exception {
        // Given: création de plusieurs utilisateurs pour tester le logout
        List<UserIndex> testUsers = new ArrayList<>();
        List<String> tokens = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            RegisterDto registerDto = createRegisterDto("LogoutUser" + i, "Test" + i, "logout.test" + i + "@example.com");
            UserIndex user = authService.register(registerDto);
            testUsers.add(user);
            createdUserEmails.add(user.email());

            // Connexion pour obtenir le token
            LoginDto loginDto = new LoginDto(user.email(), "Password123");
            AuthResponse response = authService.login(loginDto);
            tokens.add(response.accessToken());
        }

        int numThreads = 10; // plus de threads que d'utilisateurs

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        var threads = new ArrayList<Thread>();
        // When: exécuter des opérations de logout concurrentes
        for (int i = 0; i < numThreads; i++) {
            final int threadNum = i;
            final int userIndex = threadNum % testUsers.size(); // alterner entre utilisateurs
            final Long userId = testUsers.get(userIndex).id();
            final String token = tokens.get(userIndex);
            var thread = new Thread(() -> {
                try {
                    authService.invalidateUserSessions(userId);
                    jwtService.revokeToken(token);
                    successCount.incrementAndGet();
                    logger.info("Thread {} successfully performed logout operations", threadNum);
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    logger.info("Thread {} failed to perform logout: {}", threadNum, e.getMessage());
                }
            });
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        logger.info("Successful operations: {}", successCount.get());
        logger.info("Failed operations: {}", failureCount.get());

        assertEquals(numThreads, successCount.get(), "All logout operations should succeed");
        assertEquals(0, failureCount.get(), "No logout operations should fail");

    }
}
