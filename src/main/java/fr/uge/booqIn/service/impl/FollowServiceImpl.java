package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.entity.Follow;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.model.Followable;
import fr.uge.booqIn.repository.FollowRepository;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.interfaces.FollowServiceInterface;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl implements FollowServiceInterface {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowServiceImpl(FollowRepository followRepository,
                             UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    /**
     * Follow an entity (user, book, collection, etc.)
     * 
     * @param followerId ID of the user who is following
     * @param followable The entity to follow
     * @return The created follow relationship
     */
    @Override
    @Transactional
    public Follow followEntity(Long followerId, Followable followable) {
        // Check if already following
        if (isFollowing(followerId, followable.getId(), followable.getEntityType())) {
            throw new IllegalStateException("Already following this entity");
        }
        
        // Get the user
        User follower = userRepository.findById(followerId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Create the follow relationship
        Follow follow = Follow.create(follower, followable);
        Follow savedFollow = followRepository.save(follow);
        
/*        // Send notification if following a user
        if (followable.getEntityType().equals("USER")) {
            notificationService.createNotification(
                followable.getId(),
                followerId,
                "FOLLOW",
                "You have a new follower: " + follower.getFullName()
            );
        }*/
        
        return savedFollow;
    }

    /**
     * Unfollow an entity
     * 
     * @param followerId ID of the user who is unfollowing
     * @param entityId ID of the entity to unfollow
     * @param entityType Type of the entity to unfollow
     * @return true if successful, false otherwise
     */
    @Override
    @Transactional
    public boolean unfollowEntity(Long followerId, Long entityId, String entityType) {
        try {
            followRepository.deleteByFollowerIdAndEntityIdAndEntityType(followerId, entityId, entityType);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check if a user is following a specific entity
     */
    @Override
    public boolean isFollowing(Long followerId, Long entityId, String entityType) {
        return followRepository.existsByFollowerAndEntity(followerId, entityId, entityType);
    }

    /**
     * Get all entities of a specific type that a user is following
     */
    @Override
    public List<Long> getFollowedEntities(Long followerId, String entityType) {
        return followRepository.findByFollowerIdAndEntityType(followerId, entityType)
            .stream()
            .map(Follow::getEntityId)
            .collect(Collectors.toList());
    }

    /**
     * Get all followers of a specific entity
     */
    @Override
    public List<User> getFollowers(Long entityId, String entityType) {
        return followRepository.findByEntityIdAndEntityType(entityId, entityType)
            .stream()
            .map(Follow::getFollower)
            .collect(Collectors.toList());
    }

    /**
     * Count followers for a specific entity
     */
    @Override
    public Long countFollowers(Long entityId, String entityType) {
        return followRepository.countByEntityIdAndEntityType(entityId, entityType);
    }
}