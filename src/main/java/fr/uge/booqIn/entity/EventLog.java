package fr.uge.booqIn.entity;

import fr.uge.booqIn.event.DomainEvent;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entity for logging domain events in the system.
 * Provides a persistent record of all events for traceability and event replay.
 */
@Entity
@Table(name = "event_logs")
public class EventLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "event_id", nullable = false, unique = true)
  private String eventId;

  @Column(name = "event_type", nullable = false)
  private String eventType;

  @Column(name = "entity_type", nullable = false)
  private String entityType;

  @Column(name = "timestamp", nullable = false)
  private Instant timestamp;

  @Column(name = "source_class", nullable = false)
  private String sourceClass;

  @Column(name = "payload", columnDefinition = "TEXT")
  private String payload;

  @Column(name = "processed", nullable = false)
  private boolean processed;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  @Column(name = "retry_count")
  private int retryCount;

  @Column(name = "last_retry_at")
  private Instant lastRetryAt;

  /**
   * Default constructor required by JPA
   */
  protected EventLog() {
  }

  /**
   * Creates a new event log from a domain event
   * 
   * @param event   The domain event to log
   * @param payload String representation of the event payload
   */
  public EventLog(DomainEvent event, String payload) {
    this.eventId = event.getEventId();
    this.eventType = event.getClass().getSimpleName();
    this.entityType = event.getEntityType();
    this.timestamp = event.getTimestamp();
    this.sourceClass = event.getSource().getClass().getName();
    this.payload = payload;
    this.processed = false;
    this.retryCount = 0;
  }

  // Getters and Setters

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEventId() {
    return eventId;
  }

  public void setEventId(String eventId) {
    this.eventId = eventId;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getEntityType() {
    return entityType;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }

  public String getSourceClass() {
    return sourceClass;
  }

  public void setSourceClass(String sourceClass) {
    this.sourceClass = sourceClass;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public boolean isProcessed() {
    return processed;
  }

  public void setProcessed(boolean processed) {
    this.processed = processed;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public int getRetryCount() {
    return retryCount;
  }

  public void setRetryCount(int retryCount) {
    this.retryCount = retryCount;
  }

  public Instant getLastRetryAt() {
    return lastRetryAt;
  }

  public void setLastRetryAt(Instant lastRetryAt) {
    this.lastRetryAt = lastRetryAt;
  }

  /**
   * Increment the retry count and update the last retry timestamp
   */
  public void incrementRetryCount() {
    this.retryCount++;
    this.lastRetryAt = Instant.now();
  }
}