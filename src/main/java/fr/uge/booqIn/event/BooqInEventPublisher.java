package fr.uge.booqIn.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class BooqInEventPublisher implements ApplicationEventPublisherAware {
  private ApplicationEventPublisher eventPublisher;

  @Override
  public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher applicationEventPublisher) {
    this.eventPublisher = applicationEventPublisher;
  }

  public void publishEvent(BooqInEvent booqinEvent) {
    eventPublisher.publishEvent(booqinEvent);
  }

}
