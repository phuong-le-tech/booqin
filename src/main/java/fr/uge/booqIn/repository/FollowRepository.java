package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

  /**
   * Finds all follow relationships for a given follower and entity type.
   *
   * @param followerId The unique identifier of the follower
   * @param entityType The type of entities to find
   * @return A list of follow relationships
   */
  @Query("SELECT f FROM Follow f WHERE f.follower.id = :followerId AND f.entityType = :entityType")
  List<Follow> findByFollowerIdAndEntityType(
          @NonNull Long followerId,
          @NonNull String entityType
  );

  /**
   * Finds all followers for a specific entity.
   *
   * @param entityId   The unique identifier of the entity
   * @param entityType The type of the entity
   * @return A list of follow relationships
   */
  @Query("SELECT f FROM Follow f WHERE f.entityId = :entityId AND f.entityType = :entityType")
  List<Follow> findByEntityIdAndEntityType(
          @NonNull Long entityId,
          @NonNull String entityType
  );

  /**
   * Checks if a user follows a specific entity.
   *
   * @param followerId The unique identifier of the follower
   * @param entityId   The unique identifier of the entity
   * @param entityType The type of the entity
   * @return True if the user follows the entity, false otherwise
   */
  @Query("SELECT COUNT(f) > 0 FROM Follow f WHERE f.follower.id = :followerId AND f.entityId = :entityId AND f.entityType = :entityType")
  boolean existsByFollowerAndEntity(
          @NonNull Long followerId,
          @NonNull Long entityId,
          @NonNull String entityType
  );

  /**
   * Counts the number of followers for a specific entity.
   *
   * @param entityId   The unique identifier of the entity
   * @param entityType The type of the entity
   * @return The number of followers
   */
  @Query("SELECT COUNT(f) FROM Follow f WHERE f.entityId = :entityId AND f.entityType = :entityType")
  Long countByEntityIdAndEntityType(
          @NonNull Long entityId,
          @NonNull String entityType
  );

  /**
   * Deletes a follow relationship for a specific follower, entity, and entity type.
   *
   * @param followerId The unique identifier of the follower
   * @param entityId   The unique identifier of the entity
   * @param entityType The type of the entity
   */
  void deleteByFollowerIdAndEntityIdAndEntityType(
          @NonNull Long followerId,
          @NonNull Long entityId,
          @NonNull String entityType
  );
}