package fr.uge.booqIn.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uge.booqIn.entity.EventLog;
import fr.uge.booqIn.event.BookCreatedEvent;
import fr.uge.booqIn.event.BookNotAvailableEvent;
import fr.uge.booqIn.event.BookReturnedEvent;
import fr.uge.booqIn.event.DomainEvent;
import fr.uge.booqIn.repository.EventLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * Service for replaying failed events.
 * Provides functionality to retry events that failed during processing.
 */
@Service
public class EventReplayService {
  private static final Logger logger = LoggerFactory.getLogger(EventReplayService.class);

  private final EventLogRepository eventLogRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final ObjectMapper objectMapper;

  @Value("${app.event.max-retries:3}")
  private int maxRetries;

  public EventReplayService(
      EventLogRepository eventLogRepository,
      ApplicationEventPublisher eventPublisher) {
    this.eventLogRepository = eventLogRepository;
    this.eventPublisher = eventPublisher;
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Scheduled task to check and replay failed events.
   * Runs every 15 minutes by default.
   */
  @Scheduled(fixedDelayString = "${app.event.retry-interval:900000}")
  public void replayFailedEvents() {
    List<EventLog> retryableEvents = eventLogRepository.findRetryableEvents(maxRetries);

    logger.info("Found {} failed events to replay", retryableEvents.size());

    for (EventLog eventLog : retryableEvents) {
      replayEvent(eventLog);
    }
  }

  /**
   * Replays a specific event.
   * 
   * @param eventLog The event log to replay
   * @return true if replay was successful, false otherwise
   */
  public boolean replayEvent(EventLog eventLog) {
    try {
      DomainEvent event = reconstructEvent(eventLog);

      if (event != null) {
        logger.info("Replaying event: {} [ID: {}]",
            eventLog.getEventType(), eventLog.getEventId());

        eventPublisher.publishEvent(event);

        eventLog.setProcessed(true);
        eventLogRepository.save(eventLog);

        return true;
      }

      return false;

    } catch (Exception e) {
      logger.error("Error replaying event: {} [ID: {}]",
          eventLog.getEventType(), eventLog.getEventId(), e);

      eventLog.incrementRetryCount();
      eventLog.setLastRetryAt(Instant.now());
      eventLog.setErrorMessage(e.getMessage());
      eventLogRepository.save(eventLog);

      return false;
    }
  }

  /**
   * Reconstructs a domain event from an event log.
   * 
   * @param eventLog The event log containing the event data
   * @return The reconstructed domain event, or null if the event type is unknown
   */
  private DomainEvent reconstructEvent(EventLog eventLog) throws Exception {
    String eventType = eventLog.getEventType();
    String payload = eventLog.getPayload();

    return switch (eventType) {
      case "BookCreatedEvent" -> objectMapper.readValue(payload, BookCreatedEvent.class);
      case "BookReturnedEvent" -> objectMapper.readValue(payload, BookReturnedEvent.class);
      case "BookNotAvailableEvent" -> objectMapper.readValue(payload, BookNotAvailableEvent.class);
      default -> {
        logger.warn("Unknown event type: {}", eventType);
        yield null;
      }
    };
  }
}