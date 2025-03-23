package fr.uge.booqIn.event;

public interface BooqInEventListener<T extends BooqInEvent> {
  void handleEvent(T event);
}
