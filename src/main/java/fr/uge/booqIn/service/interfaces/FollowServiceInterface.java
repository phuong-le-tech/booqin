package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.entity.Follow;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.model.Followable;

import java.util.List;

public interface FollowServiceInterface {

  Follow followEntity(Long followerId, Followable followable);
  boolean unfollowEntity(Long followerId, Long entityId, String entityType);
  boolean isFollowing(Long followerId, Long entityId, String entityType);
  List<Long> getFollowedEntities(Long followerId, String entityType);
  List<User> getFollowers(Long entityId, String entityType);
  Long countFollowers(Long entityId, String entityType);
}
