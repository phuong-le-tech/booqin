package fr.uge.booqIn.event.publishable;

import fr.uge.booqIn.entity.Notification;
import fr.uge.booqIn.event.BooqInEvent;

public record NotificationEvent(Notification notification) implements BooqInEvent {

}
