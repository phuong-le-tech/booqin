package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.entity.Follow;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.event.publishable.AddFollowedUserBooks;
import fr.uge.booqIn.event.BooqInEventPublisher;
import fr.uge.booqIn.event.publishable.RemoveFollowedUserBooks;
import fr.uge.booqIn.interfaces.Followable;
import fr.uge.booqIn.repository.FollowRepository;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.interfaces.FollowServiceInterface;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FollowServiceImpl implements FollowServiceInterface {
  private final Logger logger = LoggerFactory.getLogger(FollowServiceImpl.class);
  private final FollowRepository followRepository;
  private final UserRepository userRepository;
  private final BooqInEventPublisher eventPublisher;

  public FollowServiceImpl(
          @NonNull FollowRepository followRepository,
          @NonNull UserRepository userRepository,
          @NonNull BooqInEventPublisher eventPublisher) {
    this.followRepository = followRepository;
    this.userRepository = userRepository;
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public Follow followEntity(@NonNull Long followerId, @NonNull Followable followable) {
    // Check if followable fields are null
    if (followable.getId() == null) {
      throw new IllegalArgumentException("Followable entity ID cannot be null");
    }
    if (followable.getEntityType() == null) {
      throw new IllegalArgumentException("Followable entity type cannot be null");
    }

    if (isFollowing(followerId, followable.getId(), followable.getEntityType())) {
      throw new IllegalStateException("Already following this entity");
    }

    User follower = userRepository.findById(followerId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    Follow follow = Follow.create(follower, followable);
    Follow savedFollow = followRepository.save(follow);

    if ("USER".equals(followable.getEntityType())) {
      logger.info("Following user with ID: {}", followable.getId());
      User followed = userRepository.findById(followable.getId())
              .orElseThrow(() -> new IllegalArgumentException("Followed user not found"));
      logger.info("Followed user: {}", followed.getFullName());
      eventPublisher.publishEvent(new AddFollowedUserBooks(follower, followed));
    }

    return savedFollow;
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public boolean unfollowEntity(@NonNull Long followerId, @NonNull Long entityId, @NonNull String entityType) {
    if ("USER".equals(entityType)) {
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
  public boolean isFollowing(@NonNull Long followerId, @NonNull Long entityId, @NonNull String entityType) {
    return followRepository.existsByFollowerAndEntity(followerId, entityId, entityType);
  }

  @Override
  public List<Long> getFollowedEntities(@NonNull Long followerId, @NonNull String entityType) {
    return followRepository.findByFollowerIdAndEntityType(followerId, entityType)
            .stream()
            .map(Follow::getEntityId)
            .collect(Collectors.toList());
  }

  @Override
  public List<User> getFollowers(@NonNull Long entityId, @NonNull String entityType) {
    return followRepository.findByEntityIdAndEntityType(entityId, entityType)
            .stream()
            .map(Follow::getFollower)
            .collect(Collectors.toList());
  }

  @Override
  public Long countFollowers(@NonNull Long entityId, @NonNull String entityType) {
    return followRepository.countByEntityIdAndEntityType(entityId, entityType);
  }

  @Recover
  public Follow recoverFromOptimisticLockingFailureOnFollow(
          ObjectOptimisticLockingFailureException ex,
          Long followerId,
          Followable followable) {
    logger.error("Optimistic locking failure during follow operation: {}", ex.getMessage());
    throw new ConcurrentModificationException("The follow relationship could not be created due to concurrent modifications. Please try again.", ex);
  }

  @Recover
  public boolean recoverFromOptimisticLockingFailureOnUnfollow(
          ObjectOptimisticLockingFailureException ex,
          Long followerId,
          Long entityId,
          String entityType) {
    logger.error("Optimistic locking failure during unfollow operation: {}", ex.getMessage());
    throw new ConcurrentModificationException("The follow relationship could not be removed due to concurrent modifications. Please try again.", ex);
  }
}