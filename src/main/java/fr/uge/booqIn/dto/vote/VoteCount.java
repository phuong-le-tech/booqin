package fr.uge.booqIn.dto.vote;

public record VoteCount(
    Long entityId,
    String entityType,
    long upvoteCount,
    long downvoteCount,
    int score
) {}