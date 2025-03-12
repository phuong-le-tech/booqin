package fr.uge.booqIn.dto.vote;

import fr.uge.booqIn.enums.VoteType;

public record VoteUpdate(
    VoteType voteType
) {}