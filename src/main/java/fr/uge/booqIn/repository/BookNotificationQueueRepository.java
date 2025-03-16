package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.BookLending;
import fr.uge.booqIn.entity.BookNotificationQueue;
import fr.uge.booqIn.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookNotificationQueueRepository extends JpaRepository<BookNotificationQueue, Long> {

  List<BookNotificationQueue> findByBookIdAndStatusOrderByPositionAsc(Long bookId, NotificationStatus status);
  Optional<BookNotificationQueue> findFirstByBookIdAndStatusOrderByPositionAsc(Long bookId, NotificationStatus status);
  List<BookNotificationQueue> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, NotificationStatus status);
  List<BookNotificationQueue> findByUserIdAndStatusInOrderByCreatedAtDesc(Long userId, List<NotificationStatus> statuses);
  @Query("SELECT bnq FROM BookNotificationQueue bnq WHERE bnq.reservationExpiresAt < :now AND bnq.status = fr.uge.booqIn.enums.NotificationStatus.NOTIFIED")
  List<BookNotificationQueue> findExpiredNotifications(@Param("now") LocalDateTime now);
  boolean existsByBookIdAndUserIdAndStatusIn(Long bookId, Long userId, List<NotificationStatus> statuses);
  @Query("SELECT MAX(bnq.position) FROM BookNotificationQueue bnq WHERE bnq.book.id = :bookId AND bnq.status = fr.uge.booqIn.enums.NotificationStatus.WAITING")
  Optional<Integer> findMaxPositionByBookId(@Param("bookId") Long bookId);
}
