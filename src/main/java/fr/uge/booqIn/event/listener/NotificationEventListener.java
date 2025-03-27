package fr.uge.booqIn.event.listener;

import fr.uge.booqIn.entity.Notification;
import fr.uge.booqIn.event.publishable.BookAvailableEvent;
import fr.uge.booqIn.event.publishable.bookqueue.BookQueueCancellationEvent;
import fr.uge.booqIn.event.publishable.bookqueue.BookQueueExpirationEvent;
import fr.uge.booqIn.event.publishable.bookqueue.BookQueueJoinedEvent;
import fr.uge.booqIn.event.publishable.bookqueue.BookQueueReservationEvent;
import fr.uge.booqIn.factory.NotificationFactory;
import fr.uge.booqIn.repository.NotificationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.logging.Logger;

@Component
public class NotificationEventListener {
  private static final Logger log = Logger.getLogger(NotificationEventListener.class.getName());
  private final NotificationRepository notificationRepository;
  private final NotificationFactory notificationFactory;

  public NotificationEventListener(NotificationRepository notificationRepository, NotificationFactory notificationFactory) {
    this.notificationRepository = notificationRepository;
    this.notificationFactory = notificationFactory;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleOnBookAvailable(BookAvailableEvent event) {
    log.info("Handling BookAvailable event");
    Notification notification = notificationFactory.createBookAvailableNotification(event.userId(), event.bookTitle());
    notificationRepository.save(notification);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleOnBookQueueJoin(BookQueueJoinedEvent event) {
    log.info("Handling BookQueueJoined event");
    Notification notification = notificationFactory.createQueueJoinedNotification(
            event.userId(),
            event.bookTitle(),
            event.position());
    notificationRepository.save(notification);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleOnBookQueueExpiration(BookQueueExpirationEvent event) {
    log.info("Handling BookQueueExpiration event");
    Notification notification = notificationFactory.createQueueExpirationNotification(
            event.userId(),
            event.bookTitle());
    notificationRepository.save(notification);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleOnBookQueueReservation(BookQueueReservationEvent event) {
    log.info("Handling BookQueueReservation event");
    Notification notification = notificationFactory.createQueueBookAvailableNotification(
            event.userId(),
            event.bookTitle());
    notificationRepository.save(notification);
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleOnBookQueueCancellation(BookQueueCancellationEvent event) {
    log.info("Handling BookQueueCancellation event");
    Notification notification = notificationFactory.createQueueCancelledNotification(
            event.userId(),
            event.bookTitle());
    notificationRepository.save(notification);
  }

}
