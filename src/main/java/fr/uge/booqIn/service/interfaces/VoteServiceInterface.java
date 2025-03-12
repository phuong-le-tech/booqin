package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.dto.vote.VoteCount;
import fr.uge.booqIn.dto.vote.VoteCreate;
import fr.uge.booqIn.dto.vote.VoteResponse;
import fr.uge.booqIn.dto.vote.VoteUpdate;
import fr.uge.booqIn.enums.VoteType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface VoteServiceInterface {

    /**
     * Cast a vote (create or update)
     */
    VoteResponse castVote(@NotNull @Valid VoteCreate voteCreate);

    /**
     * Update an existing vote
     */
    VoteResponse updateVote(@NotNull Long userId, @NotNull Long entityId, 
                           @NotNull String entityType, @NotNull @Valid VoteUpdate voteUpdate);

    /**
     * Delete a vote
     */
    void deleteVote(@NotNull Long userId, @NotNull Long entityId, @NotNull String entityType);

    /**
     * Get vote by user and entity
     */
    VoteResponse getVote(@NotNull Long userId, @NotNull Long entityId, @NotNull String entityType);

    /**
     * Get all votes for an entity
     */
    List<VoteResponse> getVotesByEntity(@NotNull Long entityId, @NotNull String entityType);

    /**
     * Get all votes by a user
     */
    List<VoteResponse> getVotesByUser(@NotNull Long userId);

    /**
     * Get vote counts for an entity
     */
    VoteCount getVoteCounts(@NotNull Long entityId, @NotNull String entityType);

    /**
     * Check if a user has voted on an entity
     */
    boolean hasVoted(@NotNull Long userId, @NotNull Long entityId, @NotNull String entityType);

    /**
     * Get vote type for a user and entity
     */
    VoteType getUserVoteType(@NotNull Long userId, @NotNull Long entityId, @NotNull String entityType);
}