package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.entity.Follow;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.interfaces.Followable;
import jakarta.validation.Valid;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Service interface for managing follow relationships in the BooqIn application.
 * Provides methods to follow and unfollow entities, check follow status, and retrieve followers.
 */
@Validated
public interface FollowServiceInterface {

  /**
   * Creates a follow relationship between a user and a followable entity.
   *
   * @param followerId the ID of the user who is following (required)
   * @param followable the entity being followed (required)
   * @return the created Follow entity
   * @throws IllegalStateException if the user is already following the entity
   * @throws IllegalArgumentException if the user or followable entity is not found
   */
  Follow followEntity(@NonNull Long followerId, @NonNull @Valid Followable followable);

  /**
   * Removes a follow relationship between a user and an entity.
   *
   * @param followerId the ID of the user who is unfollowing (required)
   * @param entityId the ID of the entity being unfollowed (required)
   * @param entityType the type of the entity being unfollowed (required)
   * @return true if the unfollow operation was successful
   * @throws IllegalArgumentException if the user or entity is not found
   */
  boolean unfollowEntity(@NonNull Long followerId, @NonNull Long entityId, @NonNull String entityType);

  /**
   * Checks if a user is following a specific entity.
   *
   * @param followerId the ID of the user who might be following (required)
   * @param entityId the ID of the entity that might be followed (required)
   * @param entityType the type of the entity (required)
   * @return true if the user is following the entity, false otherwise
   */
  boolean isFollowing(@NonNull Long followerId, @NonNull Long entityId, @NonNull String entityType);

  /**
   * Retrieves the IDs of all entities of a specific type that a user is following.
   *
   * @param followerId the ID of the user (required)
   * @param entityType the type of entities to retrieve (required)
   * @return a list of entity IDs that the user is following
   */
  List<Long> getFollowedEntities(@NonNull Long followerId, @NonNull String entityType);

  /**
   * Retrieves all users who follow a specific entity.
   *
   * @param entityId the ID of the entity (required)
   * @param entityType the type of the entity (required)
   * @return a list of users who follow the entity
   */
  List<User> getFollowers(@NonNull Long entityId, @NonNull String entityType);

  /**
   * Counts the number of followers for a specific entity.
   *
   * @param entityId the ID of the entity (required)
   * @param entityType the type of the entity (required)
   * @return the number of followers
   */
  Long countFollowers(@NonNull Long entityId, @NonNull String entityType);
}