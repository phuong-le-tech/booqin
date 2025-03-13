package fr.uge.booqIn.dto.vote;

import fr.uge.booqIn.enums.VoteType;

public record VoteResponse(
    Long id,
    Long userId,
    String username,
    Long entityId,
    String entityType,
    VoteType voteType
) {}