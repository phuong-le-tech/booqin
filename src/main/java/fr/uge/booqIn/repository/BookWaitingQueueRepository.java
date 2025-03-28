package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.BookWaitingQueue;
import fr.uge.booqIn.enums.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookWaitingQueueRepository extends JpaRepository<BookWaitingQueue, Long> {

  /**
   * Finds waiting queue entries for a specific book, ordered by position.
   *
   * @param bookId The unique identifier of the book
   * @param status The status of the queue entries
   * @return A list of waiting queue entries sorted by position
   */
  List<BookWaitingQueue> findByBookIdAndStatusOrderByPositionAsc(
          @NonNull Long bookId,
          @NonNull QueueStatus status
  );

  /**
   * Finds the first waiting queue entry for a specific book, ordered by position.
   *
   * @param bookId The unique identifier of the book
   * @param status The status of the queue entry
   * @return An optional containing the first queue entry
   */
  @Query("SELECT bnq FROM BookWaitingQueue bnq WHERE bnq.book.id = :bookId AND bnq.status = :status ORDER BY bnq.position ASC")
  Optional<BookWaitingQueue> findFirstByBookIdAndStatusOrderByPositionAsc(
          @NonNull Long bookId,
          @NonNull QueueStatus status
  );

  /**
   * Finds waiting queue entries for a specific user, ordered by creation time.
   *
   * @param userId The unique identifier of the user
   * @param status The status of the queue entries
   * @return A list of waiting queue entries sorted by creation time
   */
  List<BookWaitingQueue> findByUserIdAndStatusOrderByCreatedAtDesc(
          @NonNull Long userId,
          @NonNull QueueStatus status
  );

  /**
   * Finds waiting queue entries for a specific user with multiple statuses,
   * ordered by creation time.
   *
   * @param userId   The unique identifier of the user
   * @param statuses List of queue statuses to search for
   * @return A list of waiting queue entries sorted by creation time
   */
  List<BookWaitingQueue> findByUserIdAndStatusInOrderByCreatedAtDesc(
          @NonNull Long userId,
          @NonNull List<QueueStatus> statuses
  );

  /**
   * Finds expired notifications in the waiting queue.
   *
   * @param now The current date and time
   * @return A list of expired queue entries
   */
  @Query("SELECT bnq FROM BookWaitingQueue bnq WHERE bnq.reservationExpiresAt < :now AND bnq.status = fr.uge.booqIn.enums.QueueStatus.NOTIFIED")
  List<BookWaitingQueue> findExpiredNotifications(
          @NonNull LocalDateTime now
  );

  /**
   * Checks if a waiting queue entry exists for a specific book and user
   * with given statuses.
   *
   * @param bookId   The unique identifier of the book
   * @param userId   The unique identifier of the user
   * @param statuses List of queue statuses to check
   * @return True if an entry exists, false otherwise
   */
  boolean existsByBookIdAndUserIdAndStatusIn(
          @NonNull Long bookId,
          @NonNull Long userId,
          @NonNull List<QueueStatus> statuses
  );

  /**
   * Finds the maximum queue position for a specific book.
   *
   * @param bookId The unique identifier of the book
   * @return An optional containing the maximum queue position
   */
  @Query("SELECT MAX(bnq.position) FROM BookWaitingQueue bnq WHERE bnq.book.id = :bookId AND bnq.status = fr.uge.booqIn.enums.QueueStatus.WAITING")
  Optional<Integer> findMaxPositionByBookId(
          @NonNull Long bookId
  );
}