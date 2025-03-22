package fr.uge.booqIn.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public interface EventListener<T extends DomainEvent> {

  default Logger getLogger() {
    return LoggerFactory.getLogger(getClass());
  }

  @Transactional
  default void handleEvent(T event) {
    try {
      getLogger().debug("Processing event: {} [ID: {}]",
              event.getClass().getSimpleName(), event.getEventId());
      processEvent(event);
      getLogger().debug("Successfully processed event");
    } catch (Exception e) {
      getLogger().error("Error processing event", e);
      handleEventProcessingError(event, e);
    }
  }

  void processEvent(T event);

  default void handleEventProcessingError(T event, Exception exception) {

  }
}