package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.EventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing EventLog entities.
 * Provides methods for querying and managing event logs.
 */
@Repository
public interface EventLogRepository extends JpaRepository<EventLog, Long> {

  /**
   * Find an event log by its event ID
   * 
   * @param eventId The ID of the event
   * @return The event log if found
   */
  Optional<EventLog> findByEventId(String eventId);

  /**
   * Find all unprocessed event logs
   * 
   * @return List of unprocessed event logs
   */
  List<EventLog> findByProcessedFalse();

  /**
   * Find event logs with retry count less than the specified limit and not yet
   * processed
   * 
   * @param maxRetries The maximum retry count
   * @return List of event logs that can be retried
   */
  @Query("SELECT e FROM EventLog e WHERE e.processed = false AND e.retryCount < :maxRetries")
  List<EventLog> findRetryableEvents(@Param("maxRetries") int maxRetries);

  /**
   * Find event logs of a specific type
   * 
   * @param eventType The type of event
   * @return List of event logs of the specified type
   */
  List<EventLog> findByEventType(String eventType);

  /**
   * Find event logs for a specific entity type
   * 
   * @param entityType The type of entity
   * @return List of event logs for the specified entity type
   */
  List<EventLog> findByEntityType(String entityType);

  /**
   * Find event logs within a time range
   * 
   * @param start The start of the time range
   * @param end   The end of the time range
   * @return List of event logs within the time range
   */
  List<EventLog> findByTimestampBetween(Instant start, Instant end);
}