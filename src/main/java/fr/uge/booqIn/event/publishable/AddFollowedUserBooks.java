package fr.uge.booqIn.event.publishable;

import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.event.BooqInEvent;

public record AddFollowedUserBooks(User follower, User followed) implements BooqInEvent {

  @Override
  public String toString() {
    return "ApplicationEvent :: Add Books from " + followed + " to " + follower;
  }
}