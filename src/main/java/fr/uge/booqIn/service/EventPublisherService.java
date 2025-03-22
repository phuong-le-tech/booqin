package fr.uge.booqIn.service;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.EventLog;
import fr.uge.booqIn.event.BookCreatedEvent;
import fr.uge.booqIn.event.BookNotAvailableEvent;
import fr.uge.booqIn.event.BookReturnedEvent;
import fr.uge.booqIn.event.DomainEvent;
import fr.uge.booqIn.repository.EventLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * Acts as the Subject in the Observer pattern.
 * Centralizes event creation and distribution to observers.
 * 
 * This service is responsible for publishing all domain events,
 * maintaining a consistent approach to event publication throughout
 * the application.
 */
@Service
public class EventPublisherService {
  private final Logger logger = LoggerFactory.getLogger(EventPublisherService.class);
  private final ApplicationEventPublisher eventPublisher;
  private final EventLogRepository eventLogRepository;

  /**
   * Creates a new EventPublisherService with the given ApplicationEventPublisher.
   * 
   * @param eventPublisher The Spring event publisher
   */
  public EventPublisherService(ApplicationEventPublisher eventPublisher, EventLogRepository eventLogRepository) {
    this.eventPublisher = eventPublisher;
    this.eventLogRepository = eventLogRepository;
  }

  /**
   * Generic method to publish any domain event.
   * 
   * @param event The event to publish
   */
  public void publishEvent(DomainEvent event) {
    logger.debug("Publishing event: {} [ID: {}]",
        event.getClass().getSimpleName(), event.getEventId());
    
    EventLog eventLog = new EventLog(event,"{}");
    eventLogRepository.save(eventLog);
    eventPublisher.publishEvent(event);
  }

  /**
   * Convenience method for book creation events.
   * 
   * @param source The source object that triggered the event
   * @param book   The newly created book
   */
  public void publishBookCreatedEvent(Object source, Book book) {
    publishEvent(new BookCreatedEvent(source, book));
  }

  /**
   * Convenience method for book return events.
   * 
   * @param source The source object that triggered the event
   * @param bookId The ID of the returned book
   */
  public void publishBookReturnedEvent(Object source, Long bookId) {
    publishEvent(new BookReturnedEvent(source, bookId));
  }

  /**
   * Convenience method for book unavailability events.
   * 
   * @param source The source object that triggered the event
   * @param bookId The ID of the unavailable book
   * @param userId The ID of the user trying to access the book
   */
  public void publishBookNotAvailableEvent(Object source, Long bookId, Long userId) {
    publishEvent(new BookNotAvailableEvent(source, bookId, userId));
  }
}