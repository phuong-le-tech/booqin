package fr.uge.booqIn.event.publishable;

import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.event.BooqInEvent;

public record RemoveFollowedUserBooks(User follower, User followed) implements BooqInEvent {
}