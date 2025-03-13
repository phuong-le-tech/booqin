package fr.uge.booqIn.dto.vote;

import fr.uge.booqIn.enums.VoteType;

public record VoteCreate(
    Long userId,
    Long entityId,
    String entityType,
    VoteType voteType
) {}