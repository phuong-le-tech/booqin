package fr.uge.booqIn.api.controller;

import fr.uge.booqIn.controller.api.NotificationController;
import fr.uge.booqIn.dto.notification.BookNotificationDTO;
import fr.uge.booqIn.enums.NotificationStatus;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.interfaces.NotificationServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@DisplayName("NotificationController Tests")
class NotificationControllerTest {

  private NotificationServiceInterface notificationService;
  private NotificationController notificationController;

  @BeforeEach
  void setUp() {
    notificationService = Mockito.mock(NotificationServiceInterface.class);
    notificationController = new NotificationController(notificationService);
  }

  @Test
  @DisplayName("POST /subscribe - Should add user to notification queue and return success")
  void subscribeToNotifications_ShouldReturnSuccess() {
    // Given
    Long bookId = 1L;
    Long userId = 2L;
    when(notificationService.addToNotificationQueue(eq(bookId), eq(userId))).thenReturn(true);

    // When
    ResponseEntity<ApiResponse<Boolean>> response = notificationController.subscribeToNotifications(bookId, userId);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<Boolean> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Ajouté à la file d'attente", apiResponse.message());
    assertTrue(apiResponse.data());
    verify(notificationService).addToNotificationQueue(eq(bookId), eq(userId));
  }

  @Test
  @DisplayName("POST /subscribe - Should return error when user already in queue")
  void subscribeToNotifications_ShouldReturnError_WhenUserAlreadyInQueue() {
    // Given
    Long bookId = 1L;
    Long userId = 2L;
    when(notificationService.addToNotificationQueue(eq(bookId), eq(userId))).thenReturn(false);

    // When
    ResponseEntity<ApiResponse<Boolean>> response = notificationController.subscribeToNotifications(bookId, userId);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ApiResponse<Boolean> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertFalse(apiResponse.success());
    assertEquals("Déjà dans la file d'attente", apiResponse.message());
    assertFalse(apiResponse.data());
    verify(notificationService).addToNotificationQueue(eq(bookId), eq(userId));
  }

  @Test
  @DisplayName("GET /active - Should return list of active notifications")
  void getActiveNotifications_ShouldReturnNotificationsList() {
    // Given
    Long userId = 1L;
    List<BookNotificationDTO> notifications = List.of(
            BookNotificationDTO.builder()
                    .id(1L)
                    .bookId(2L)
                    .bookTitle("Book Title 1")
                    .bookCover("/covers/book1.jpg")
                    .authorNames("Author 1")
                    .status(NotificationStatus.NOTIFIED)
                    .expiresAt(LocalDateTime.now().plusDays(3))
                    .position(1)
                    .createdAt(LocalDateTime.now())
                    .build(),
            BookNotificationDTO.builder()
                    .id(2L)
                    .bookId(3L)
                    .bookTitle("Book Title 2")
                    .bookCover("/covers/book2.jpg")
                    .authorNames("Author 2")
                    .status(NotificationStatus.WAITING)
                    .expiresAt(LocalDateTime.now().plusDays(3))
                    .position(2)
                    .createdAt(LocalDateTime.now())
                    .build()
    );

    when(notificationService.getActiveNotifications(eq(userId))).thenReturn(notifications);

    // When
    ResponseEntity<ApiResponse<List<BookNotificationDTO>>> response = notificationController.getActiveNotifications(userId);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<List<BookNotificationDTO>> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Notifications actives récupérées", apiResponse.message());
    assertEquals(notifications, apiResponse.data());
    assertEquals(2, apiResponse.data().size());
    verify(notificationService).getActiveNotifications(eq(userId));
  }

  @Test
  @DisplayName("POST /reserve - Should reserve notified book and return success")
  void reserveNotifiedBook_ShouldReturnSuccess() {
    // Given
    Long notificationId = 1L;
    Long userId = 2L;
    when(notificationService.reserveNotifiedBook(eq(notificationId), eq(userId))).thenReturn(true);

    // When
    ResponseEntity<ApiResponse<Boolean>> response = notificationController.reserveNotifiedBook(notificationId, userId);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    ApiResponse<Boolean> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertTrue(apiResponse.success());
    assertEquals("Livre réservé avec succès", apiResponse.message());
    assertTrue(apiResponse.data());
    verify(notificationService).reserveNotifiedBook(eq(notificationId), eq(userId));
  }

  @Test
  @DisplayName("POST /reserve - Should return error when reservation fails")
  void reserveNotifiedBook_ShouldReturnError_WhenReservationFails() {
    // Given
    Long notificationId = 1L;
    Long userId = 2L;
    when(notificationService.reserveNotifiedBook(eq(notificationId), eq(userId))).thenReturn(false);

    // When
    ResponseEntity<ApiResponse<Boolean>> response = notificationController.reserveNotifiedBook(notificationId, userId);

    // Then
    assertNotNull(response);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    ApiResponse<Boolean> apiResponse = response.getBody();
    assertNotNull(apiResponse);
    assertFalse(apiResponse.success());
    assertEquals("Impossible de réserver le livre", apiResponse.message());
    assertFalse(apiResponse.data());
    verify(notificationService).reserveNotifiedBook(eq(notificationId), eq(userId));
  }
}