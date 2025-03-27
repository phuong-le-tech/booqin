package fr.uge.booqIn.event.listener;

import fr.uge.booqIn.entity.BookLending;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.BookStatus;
import fr.uge.booqIn.event.publishable.StatusUpdateReminderEvent;
import fr.uge.booqIn.repository.BookLendingRepository;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Component
public class StatusUpdateReminderListener  {
  private static final Logger logger = Logger.getLogger(StatusUpdateReminderListener.class.getName());
  
  private final BookLendingRepository bookLendingRepository;

  private final fr.uge.booqIn.event.BooqInEventPublisher eventPublisher;
  
  public StatusUpdateReminderListener(
          BookLendingRepository bookLendingRepository,
          fr.uge.booqIn.event.BooqInEventPublisher eventPublisher) {
    this.bookLendingRepository = bookLendingRepository;
    this.eventPublisher = eventPublisher;
  }
  
  @EventListener
  public void handleEvent(StatusUpdateReminderEvent event) {
    BookLending lending = event.bookLending();
    User recipient = determineRecipient(lending);
    
    if (recipient == null) {
      logger.warning("Could not determine recipient for status update reminder");
      return;
    }
    
    String message = createMessageForStatus(lending);
    
    logger.info(String.format("Processing status update reminder for book '%s' with status %s",
            lending.getBook().getTitle(), lending.getStatus()));
    
    //notif
    
    logger.info("Status update reminder sent to: " + recipient.getFullName());
  }
  
  /**
   * Determine who should receive the notification based on the current status
   */
  private User determineRecipient(BookLending lending) {
    switch (lending.getStatus()) {
      case PENDING_SHIPMENT:
        return lending.getLender();
      case SHIPPED:
        return lending.getBorrower();
      default:
        return null;
    }
  }

  private String createMessageForStatus(BookLending lending) {
    String bookTitle = lending.getBook().getTitle();
    
    switch (lending.getStatus()) {
      case PENDING_SHIPMENT:
        return "Veuillez expédier le livre '" + bookTitle + "' et mettre à jour son statut.";
      case SHIPPED:
        return "Avez-vous reçu le livre '" + bookTitle + "'? Veuillez mettre à jour son statut.";
      default:
        return "Veuillez mettre à jour le statut du livre '" + bookTitle + "'.";
    }
  }
  
 
  @Scheduled(cron = "0 0 2 * * ?")
  @Transactional(readOnly = true)
  public void checkForStatusUpdateReminders() {
    logger.info("Running scheduled check for status update reminders");
    
    LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
    
    List<BookLending> pendingShipment = bookLendingRepository.findByStatusAndStartDateBefore(
            BookStatus.PENDING_SHIPMENT, oneWeekAgo);
    
    List<BookLending> shipped = bookLendingRepository.findByStatusAndStartDateBefore(
            BookStatus.SHIPPED, oneWeekAgo);
    
    logger.info("Found " + pendingShipment.size() + " pending shipment reminders");
    logger.info("Found " + shipped.size() + " shipped but not confirmed reminders");
    
    for (BookLending lending : pendingShipment) {
      eventPublisher.publishEvent(new StatusUpdateReminderEvent(lending));
    }
    
    for (BookLending lending : shipped) {
      eventPublisher.publishEvent(new StatusUpdateReminderEvent(lending));
    }
  }
}