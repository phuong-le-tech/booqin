package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.entity.Vote;
import fr.uge.booqIn.enums.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long>, JpaSpecificationExecutor<Vote> {

  /**
   * Finds a vote by user, entity ID, and entity type.
   *
   * @param user       The user who cast the vote
   * @param entityId   The unique identifier of the voted entity
   * @param entityType The type of the voted entity
   * @return An optional containing the vote if found
   */
  Optional<Vote> findByUserAndEntityIdAndEntityType(
          @NonNull User user,
          @NonNull Long entityId,
          @NonNull String entityType
  );

  /**
   * Finds all votes for a specific entity.
   *
   * @param entityId   The unique identifier of the entity
   * @param entityType The type of the entity
   * @return A list of votes for the entity
   */
  List<Vote> findAllByEntityIdAndEntityType(
          @NonNull Long entityId,
          @NonNull String entityType
  );

  /**
   * Finds all votes by a specific user.
   *
   * @param user The user who cast the votes
   * @return A list of votes by the user
   */
  List<Vote> findAllByUser(@NonNull User user);

  /**
   * Counts votes of a specific type for an entity.
   *
   * @param entityId   The unique identifier of the entity
   * @param entityType The type of the entity
   * @param voteType   The type of vote (e.g., UPVOTE, DOWNVOTE)
   * @return The number of votes matching the criteria
   */
  long countByEntityIdAndEntityTypeAndVoteType(
          @NonNull Long entityId,
          @NonNull String entityType,
          @NonNull VoteType voteType
  );

  /**
   * Counts all votes for a specific entity.
   *
   * @param entityId   The unique identifier of the entity
   * @param entityType The type of the entity
   * @return The total number of votes for the entity
   */
  long countByEntityIdAndEntityType(
          @NonNull Long entityId,
          @NonNull String entityType
  );


  /**
   * Calculates the vote score for an entity (upvotes minus downvotes).
   *
   * @param entityId   The unique identifier of the entity
   * @param entityType The type of the entity
   * @return The vote score (can be positive, negative, or zero)
   */
  @Query("SELECT COUNT(CASE WHEN v.voteType = 'UPVOTE' THEN 1 END) - " +
          "COUNT(CASE WHEN v.voteType = 'DOWNVOTE' THEN 1 END) " +
          "FROM Vote v WHERE v.entityId = :entityId AND v.entityType = :entityType")
  Integer getVoteScore(
          @NonNull Long entityId,
          @NonNull String entityType
  );
}