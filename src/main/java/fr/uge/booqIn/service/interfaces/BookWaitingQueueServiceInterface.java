package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.dto.notification.BookNotificationDTO;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Service interface for managing book waiting queues in the BooqIn application.
 * Provides methods for users to join waiting lists for books that are currently unavailable,
 * process notifications when books become available, and manage reservation priorities.
 */
public interface BookWaitingQueueServiceInterface {

  /**
   * Adds a user to the waiting queue for a specific book.
   *
   * @param bookId The ID of the book the user wants to borrow
   * @param userId The ID of the user joining the waiting queue
   * @return true if the user was successfully added to the queue, false if already in queue
   */
  boolean addToWaitingQueue(@NotNull Long bookId, @NotNull Long userId);

  /**
   * Processes the next user in the waiting queue when a book becomes available.
   * Sends notification to the user at the front of the queue.
   *
   * @param bookId The ID of the book that has become available
   */
  void processNextInQueue(@NotNull Long bookId);

  /**
   * Allows a user to reserve a book from a notification they received.
   *
   * @param queueId The ID of the queue entry/notification
   * @param userId The ID of the user reserving the book
   * @return true if the book was successfully reserved, false otherwise
   */
  boolean reserveBookFromQueue(@NotNull Long queueId, @NotNull Long userId);

  /**
   * Cancels a user's position in a book's waiting queue.
   *
   * @param queueId The ID of the queue entry to cancel
   * @param userId The ID of the user who wants to cancel their queue position
   * @return true if the queue entry was successfully canceled, false otherwise
   */
  boolean cancelQueueEntry(@NotNull Long queueId, @NotNull Long userId);

  /**
   * Retrieves all active queue entries/notifications for a specific user.
   *
   * @param userId The ID of the user
   * @return List of BookNotificationDTO objects representing active notifications
   */
  List<BookNotificationDTO> getUserActiveQueues(@NotNull Long userId);

  /**
   * Gets a user's position in the waiting queue for a specific book.
   *
   * @param bookId The ID of the book
   * @param userId The ID of the user
   * @return The position in the queue (1 = next in line), or -1 if not in queue
   */
  int getUserQueuePosition(@NotNull Long bookId, @NotNull Long userId);

  /**
   * Processes all expired notifications in the system.
   * This method should be called periodically to check for expired notifications.
   */
  void processExpiredNotifications();
}