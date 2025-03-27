package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.BookWaitingQueue;
import fr.uge.booqIn.enums.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookWaitingQueueRepository extends JpaRepository<BookWaitingQueue, Long> {

  List<BookWaitingQueue> findByBookIdAndStatusOrderByPositionAsc(Long bookId, QueueStatus status);

  Optional<BookWaitingQueue> findFirstByBookIdAndStatusOrderByPositionAsc(Long bookId, QueueStatus status);

  List<BookWaitingQueue> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, QueueStatus status);

  List<BookWaitingQueue> findByUserIdAndStatusInOrderByCreatedAtDesc(Long userId, List<QueueStatus> statuses);

  @Query("SELECT bnq FROM BookWaitingQueue bnq WHERE bnq.reservationExpiresAt < :now AND bnq.status = fr.uge.booqIn.enums.QueueStatus.NOTIFIED")
  List<BookWaitingQueue> findExpiredNotifications(@Param("now") LocalDateTime now);

  boolean existsByBookIdAndUserIdAndStatusIn(Long bookId, Long userId, List<QueueStatus> statuses);

  @Query("SELECT MAX(bnq.position) FROM BookWaitingQueue bnq WHERE bnq.book.id = :bookId AND bnq.status = fr.uge.booqIn.enums.QueueStatus.WAITING")
  Optional<Integer> findMaxPositionByBookId(@Param("bookId") Long bookId);
}
