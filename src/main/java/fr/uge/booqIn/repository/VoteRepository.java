package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.entity.Vote;
import fr.uge.booqIn.enums.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long>, JpaSpecificationExecutor<Vote> {

    /**
     * Find vote by user, entity ID and entity type
     */
    Optional<Vote> findByUserAndEntityIdAndEntityType(User user, Long entityId, String entityType);

    /**
     * Find all votes for a specific entity
     */
    List<Vote> findAllByEntityIdAndEntityType(Long entityId, String entityType);

    /**
     * Find all votes by a specific user
     */
    List<Vote> findAllByUser(User user);

    /**
     * Find all votes of a specific type for an entity
     */
    List<Vote> findAllByEntityIdAndEntityTypeAndVoteType(Long entityId, String entityType, VoteType voteType);

    /**
     * Count votes of a specific type for an entity
     */
    long countByEntityIdAndEntityTypeAndVoteType(Long entityId, String entityType, VoteType voteType);

    /**
     * Count all votes for a specific entity
     */
    long countByEntityIdAndEntityType(Long entityId, String entityType);

    /**
     * Delete all votes for a specific entity
     */
    void deleteAllByEntityIdAndEntityType(Long entityId, String entityType);

    /**
     * Delete all votes by a specific user
     */
    void deleteAllByUser(User user);

    /**
     * Get vote score (upvotes - downvotes) for an entity
     */
    @Query("SELECT COUNT(CASE WHEN v.voteType = 'UPVOTE' THEN 1 END) - " +
            "COUNT(CASE WHEN v.voteType = 'DOWNVOTE' THEN 1 END) " +
            "FROM Vote v WHERE v.entityId = :entityId AND v.entityType = :entityType")
    Integer getVoteScore(@Param("entityId") Long entityId, @Param("entityType") String entityType);
}