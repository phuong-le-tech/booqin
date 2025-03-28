package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.dto.vote.VoteCount;
import fr.uge.booqIn.dto.vote.VoteCreate;
import fr.uge.booqIn.dto.vote.VoteResponse;
import fr.uge.booqIn.dto.vote.VoteUpdate;
import fr.uge.booqIn.enums.VoteType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Service interface for managing votes in the BooqIn application.
 * Provides methods to cast, retrieve, and manipulate votes on votable entities.
 */
@Validated
public interface VoteServiceInterface {

    /**
     * Cast a vote (create or update) for an entity
     *
     * @param voteCreate DTO containing vote details (required)
     * @return The created or updated vote
     * @throws fr.uge.booqIn.exception.ResourceNotFoundException if user or entity not found
     * @throws IllegalArgumentException if entity is not votable or any parameter is null
     */
    VoteResponse castVote(@NonNull @Valid VoteCreate voteCreate);

    /**
     * Update an existing vote
     *
     * @param userId ID of the voting user (required)
     * @param entityId ID of the entity being voted on (required)
     * @param entityType Type of the entity being voted on (required)
     * @param voteUpdate DTO containing vote update details (required)
     * @return The updated vote
     * @throws fr.uge.booqIn.exception.ResourceNotFoundException if user, entity, or vote not found
     * @throws IllegalArgumentException if entity is not votable or any parameter is null
     */
    VoteResponse updateVote(@NonNull Long userId,
                            @NonNull Long entityId,
                            @NonNull String entityType,
                            @NonNull @Valid VoteUpdate voteUpdate);

    /**
     * Delete a vote
     *
     * @param userId ID of the voting user (required)
     * @param entityId ID of the entity the vote is on (required)
     * @param entityType Type of the entity the vote is on (required)
     * @throws fr.uge.booqIn.exception.ResourceNotFoundException if user or vote not found
     * @throws IllegalArgumentException if any parameter is null
     */
    void deleteVote(@NonNull Long userId, @NonNull Long entityId, @NonNull String entityType);

    /**
     * Get a specific vote by user and entity
     *
     * @param userId ID of the voting user (required)
     * @param entityId ID of the voted entity (required)
     * @param entityType Type of the voted entity (required)
     * @return The vote response
     * @throws fr.uge.booqIn.exception.ResourceNotFoundException if user or vote not found
     * @throws IllegalArgumentException if any parameter is null
     */
    VoteResponse getVote(@NonNull Long userId, @NonNull Long entityId, @NonNull String entityType);

    /**
     * Get all votes for a specific entity
     *
     * @param entityId ID of the entity (required)
     * @param entityType Type of the entity (required)
     * @return List of votes for the entity
     * @throws fr.uge.booqIn.exception.ResourceNotFoundException if entity not found
     * @throws IllegalArgumentException if any parameter is null
     */
    List<VoteResponse> getVotesByEntity(@NonNull Long entityId, @NonNull String entityType);

    /**
     * Get all votes by a specific user
     *
     * @param userId ID of the user (required)
     * @return List of votes cast by the user
     * @throws fr.uge.booqIn.exception.ResourceNotFoundException if user not found
     * @throws IllegalArgumentException if userId is null
     */
    List<VoteResponse> getVotesByUser(@NonNull Long userId);

    /**
     * Get vote counts for a specific entity
     *
     * @param entityId ID of the entity (required)
     * @param entityType Type of the entity (required)
     * @return Vote count information including upvotes, downvotes, and total score
     * @throws fr.uge.booqIn.exception.ResourceNotFoundException if entity not found
     * @throws IllegalArgumentException if any parameter is null
     */
    VoteCount getVoteCounts(@NonNull Long entityId, @NonNull String entityType);

    /**
     * Check if a user has voted on a specific entity
     *
     * @param userId ID of the user (required)
     * @param entityId ID of the entity (required)
     * @param entityType Type of the entity (required)
     * @return true if the user has voted, false otherwise
     * @throws fr.uge.booqIn.exception.ResourceNotFoundException if user not found
     * @throws IllegalArgumentException if any parameter is null
     */
    boolean hasVoted(@NonNull Long userId, @NonNull Long entityId, @NonNull String entityType);

    /**
     * Get the vote type (upvote/downvote) for a user on a specific entity
     *
     * @param userId ID of the user (required)
     * @param entityId ID of the entity (required)
     * @param entityType Type of the entity (required)
     * @return The vote type, or null if the user hasn't voted
     * @throws fr.uge.booqIn.exception.ResourceNotFoundException if user not found
     * @throws IllegalArgumentException if any parameter is null
     */
    VoteType getUserVoteType(@NonNull Long userId, @NonNull Long entityId, @NonNull String entityType);
}