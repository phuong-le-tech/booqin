package fr.uge.booqIn.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uge.booqIn.entity.EventLog;
import fr.uge.booqIn.event.DomainEvent;
import fr.uge.booqIn.repository.EventLogRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspect that logs all domain events when they are published.
 * Uses AOP to intercept method calls to the EventPublisherService.
 */
@Aspect
@Component
public class EventLoggingAspect {
  private static final Logger logger = LoggerFactory.getLogger(EventLoggingAspect.class);

  private final EventLogRepository eventLogRepository;
  private final ObjectMapper objectMapper;

  public EventLoggingAspect(EventLogRepository eventLogRepository) {
    this.eventLogRepository = eventLogRepository;
    this.objectMapper = new ObjectMapper();
  }

  /**
   * Logs domain events after they are published.
   * 
   * @param joinPoint The join point representing the method call
   * @param event     The domain event being published
   */
  @AfterReturning(pointcut = "execution(* fr.uge.booqIn.service.EventPublisherService.publishEvent(..)) && args(event)", argNames = "joinPoint,event")
  public void logEvent(JoinPoint joinPoint, DomainEvent event) {
    try {
      logger.debug("Logging event: {} [ID: {}]",
          event.getClass().getSimpleName(), event.getEventId());

      String payload = objectMapper.writeValueAsString(event);
      EventLog eventLog = new EventLog(event, payload);

      eventLogRepository.save(eventLog);

    } catch (Exception e) {
      logger.error("Error logging event: {} [ID: {}]",
          event.getClass().getSimpleName(), event.getEventId(), e);
    }
  }
}