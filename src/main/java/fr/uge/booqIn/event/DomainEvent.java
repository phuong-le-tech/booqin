package fr.uge.booqIn.event;

import java.time.Instant;


public interface DomainEvent {
  Object getSource();
  Instant getTimestamp();
  String getEventId();
  String getEntityType();
}