package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    
    /**
     * Find a follow relationship by follower, entity ID and entity type
     */
    @Query("SELECT f FROM Follow f WHERE f.follower.id = :followerId AND f.entityId = :entityId AND f.entityType = :entityType")
    Optional<Follow> findByFollowerAndEntity(@Param("followerId") Long followerId,
                                             @Param("entityId") Long entityId,
                                             @Param("entityType") String entityType);
    
    /**
     * Find all follows by the given follower
     */
    List<Follow> findByFollowerId(Long followerId);
    
    /**
     * Find all follows by the given follower and entity type
     */
    @Query("SELECT f FROM Follow f WHERE f.follower.id = :followerId AND f.entityType = :entityType")
    List<Follow> findByFollowerIdAndEntityType(@Param("followerId") Long followerId,
                                              @Param("entityType") String entityType);
    
    /**
     * Find all followers of a specific entity
     */
    @Query("SELECT f FROM Follow f WHERE f.entityId = :entityId AND f.entityType = :entityType")
    List<Follow> findByEntityIdAndEntityType(@Param("entityId") Long entityId,
                                            @Param("entityType") String entityType);
    
    /**
     * Check if a user follows a specific entity
     */
    @Query("SELECT COUNT(f) > 0 FROM Follow f WHERE f.follower.id = :followerId AND f.entityId = :entityId AND f.entityType = :entityType")
    boolean existsByFollowerAndEntity(@Param("followerId") Long followerId,
                                     @Param("entityId") Long entityId,
                                     @Param("entityType") String entityType);
    
    /**
     * Count followers for a specific entity
     */
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.entityId = :entityId AND f.entityType = :entityType")
    Long countByEntityIdAndEntityType(@Param("entityId") Long entityId,
                                     @Param("entityType") String entityType);
    
    /**
     * Delete a follow relationship
     */
    void deleteByFollowerIdAndEntityIdAndEntityType(Long followerId, Long entityId, String entityType);
}