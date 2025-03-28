package fr.uge.booqIn.event.publishable;

import fr.uge.booqIn.enums.VoteType;
import fr.uge.booqIn.event.BooqInEvent;

public record VoteBookEvent(Long userId, Long bookId, VoteType voteType) implements BooqInEvent {
}
