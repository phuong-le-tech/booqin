package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.entity.Follow;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.event.publishable.AddFollowedUserBooks;
import fr.uge.booqIn.event.BooqInEventPublisher;
import fr.uge.booqIn.event.publishable.RemoveFollowedUserBooks;
import fr.uge.booqIn.model.Followable;
import fr.uge.booqIn.repository.FollowRepository;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.interfaces.FollowServiceInterface;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl implements FollowServiceInterface {
  private final Logger logger = LoggerFactory.getLogger(FollowServiceImpl.class);
  private final FollowRepository followRepository;
  private final UserRepository userRepository;
  private final BooqInEventPublisher eventPublisher;

  public FollowServiceImpl(
          FollowRepository followRepository,
          UserRepository userRepository,
          BooqInEventPublisher eventPublisher) {
    this.followRepository = followRepository;
    this.userRepository = userRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Transactional
  public Follow followEntity(Long followerId, Followable followable) {
    if (isFollowing(followerId, followable.getId(), followable.getEntityType())) {
      throw new IllegalStateException("Already following this entity");
    }

    User follower = userRepository.findById(followerId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    Follow follow = Follow.create(follower, followable);
    Follow savedFollow = followRepository.save(follow);

    if (followable.getEntityType().equals("USER")) {
      logger.info("Following user: {}", followable.getEntityType());
      User followed = userRepository.findById(followable.getId())
              .orElseThrow(() -> new IllegalArgumentException("Followed user not found"));
      logger.info("Followed user: {}", followed.getFullName());
      eventPublisher.publishEvent(new AddFollowedUserBooks(follower, followed));
    }

    return savedFollow;
  }

  @Override
  @Transactional
  public boolean unfollowEntity(Long followerId, Long entityId, String entityType) {
    if (entityType.equals("USER")) {
      User follower = userRepository.findById(followerId)
              .orElseThrow(() -> new IllegalArgumentException("User not found"));
      User followed = userRepository.findById(entityId)
              .orElseThrow(() -> new IllegalArgumentException("Followed user not found"));
      logger.info("Unfollowing user: {}", followed.getFullName());
      eventPublisher.publishEvent(new RemoveFollowedUserBooks(follower, followed));
    }
    followRepository.deleteByFollowerIdAndEntityIdAndEntityType(followerId, entityId, entityType);
    return true;
  }

  @Override
  public boolean isFollowing(Long followerId, Long entityId, String entityType) {
    return followRepository.existsByFollowerAndEntity(followerId, entityId, entityType);
  }

  @Override
  public List<Long> getFollowedEntities(Long followerId, String entityType) {
    return followRepository.findByFollowerIdAndEntityType(followerId, entityType)
            .stream()
            .map(Follow::getEntityId)
            .collect(Collectors.toList());
  }

  @Override
  public List<User> getFollowers(Long entityId, String entityType) {
    return followRepository.findByEntityIdAndEntityType(entityId, entityType)
            .stream()
            .map(Follow::getFollower)
            .collect(Collectors.toList());
  }

  @Override
  public Long countFollowers(Long entityId, String entityType) {
    return followRepository.countByEntityIdAndEntityType(entityId, entityType);
  }
}