package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.dto.vote.VoteCount;
import fr.uge.booqIn.dto.vote.VoteCreate;
import fr.uge.booqIn.dto.vote.VoteResponse;
import fr.uge.booqIn.dto.vote.VoteUpdate;
import fr.uge.booqIn.entity.Vote;
import fr.uge.booqIn.enums.VoteType;
import fr.uge.booqIn.event.BooqInEventPublisher;
import fr.uge.booqIn.event.publishable.VoteBookEvent;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.mapper.VoteMapper;
import fr.uge.booqIn.interfaces.Votable;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.repository.VoteRepository;
import fr.uge.booqIn.service.interfaces.VoteServiceInterface;
import fr.uge.booqIn.strategy.EntityStrategyContext;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VoteServiceImpl implements VoteServiceInterface {
  private static final Logger logger = LoggerFactory.getLogger(VoteServiceImpl.class);

  private final VoteRepository voteRepository;
  private final UserRepository userRepository;
  private final EntityStrategyContext strategyContext;
  private final VoteMapper voteMapper;
  private final BooqInEventPublisher eventPublisher;

  public VoteServiceImpl(
          @NonNull VoteRepository voteRepository,
          @NonNull UserRepository userRepository,
          @NonNull EntityStrategyContext strategyContext,
          @NonNull VoteMapper voteMapper,
          @NonNull BooqInEventPublisher eventPublisher) {
    this.voteRepository = voteRepository;
    this.userRepository = userRepository;
    this.strategyContext = strategyContext;
    this.voteMapper = voteMapper;
    this.eventPublisher = eventPublisher;
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public VoteResponse castVote(@NonNull @Valid VoteCreate voteCreate) {
    logger.debug("Casting vote: User ID={}, Entity ID={}, Entity Type={}, Vote Type={}",
            voteCreate.userId(), voteCreate.entityId(), voteCreate.entityType(), voteCreate.voteType());

    var user = userRepository.findById(voteCreate.userId())
            .orElseThrow(() -> {
              logger.warn("User not found with ID: {}", voteCreate.userId());
              return new ResourceNotFoundException("User", "id", voteCreate.userId());
            });

    var targetEntity = strategyContext.findByTypeAndId(
            voteCreate.entityType(),
            voteCreate.entityId());

    if (!(targetEntity instanceof Votable)) {
      logger.warn("Entity of type {} is not votable", voteCreate.entityType());
      throw new IllegalArgumentException("Entity of type " + voteCreate.entityType() + " is not votable");
    }

    // Get entity type and ID explicitly before saving
    String entityType = voteCreate.entityType();
    Long entityId = voteCreate.entityId();

    var existingVote = voteRepository.findByUserAndEntityIdAndEntityType(
            user, entityId, entityType);

    Vote vote;
    boolean isNewVote = false;
    if (existingVote.isPresent()) {
      logger.debug("Updating existing vote: ID={}", existingVote.get().getId());
      vote = existingVote.get();
      vote.setVoteType(voteCreate.voteType());
    } else {
      logger.debug("Creating new vote");
      vote = new Vote(user, voteCreate.voteType());
      vote.setEntityId(entityId);
      vote.setEntityType(entityType);
      isNewVote = true;
    }

    vote = voteRepository.save(vote);

    if ("BOOK".equals(entityType)) {
      logger.debug("Publishing VoteBookEvent for book ID: {}", entityId);
      eventPublisher.publishEvent(new VoteBookEvent(user.getId(), entityId, vote.getVoteType()));
    }

    return voteMapper.toVoteResponse(vote);
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public VoteResponse updateVote(@NonNull Long userId, @NonNull Long entityId,
                                 @NonNull String entityType, @NonNull @Valid VoteUpdate voteUpdate) {
    logger.debug("Updating vote: User ID={}, Entity ID={}, Entity Type={}, New Vote Type={}",
            userId, entityId, entityType, voteUpdate.voteType());

    var user = userRepository.findById(userId)
            .orElseThrow(() -> {
              logger.warn("User not found with ID: {}", userId);
              return new ResourceNotFoundException("User", "id", userId);
            });

    var targetEntity = strategyContext.findByTypeAndId(entityType, entityId);

    if (!(targetEntity instanceof Votable)) {
      logger.warn("Entity of type {} is not votable", entityType);
      throw new IllegalArgumentException("Entity of type " + entityType + " is not votable");
    }

    var vote = voteRepository.findByUserAndEntityIdAndEntityType(user, entityId, entityType)
            .orElseThrow(() -> {
              logger.warn("Vote not found for User ID={}, Entity ID={}, Entity Type={}",
                      userId, entityId, entityType);
              return new ResourceNotFoundException("Vote", "user and entity",
                      userId + ", " + entityId + ", " + entityType);
            });

    vote.setVoteType(voteUpdate.voteType());
    vote = voteRepository.save(vote);

    return voteMapper.toVoteResponse(vote);
  }

  @Override
  @Transactional
  @Retryable(
          maxAttemptsExpression = "${spring.retry.max-attempts}",
          retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
          backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
  )
  public void deleteVote(@NonNull Long userId, @NonNull Long entityId, @NonNull String entityType) {
    logger.debug("Deleting vote: User ID={}, Entity ID={}, Entity Type={}",
            userId, entityId, entityType);

    var user = userRepository.findById(userId)
            .orElseThrow(() -> {
              logger.warn("User not found with ID: {}", userId);
              return new ResourceNotFoundException("User", "id", userId);
            });

    var vote = voteRepository.findByUserAndEntityIdAndEntityType(user, entityId, entityType)
            .orElseThrow(() -> {
              logger.warn("Vote not found for User ID={}, Entity ID={}, Entity Type={}",
                      userId, entityId, entityType);
              return new ResourceNotFoundException("Vote", "user and entity",
                      userId + ", " + entityId + ", " + entityType);
            });

    voteRepository.delete(vote);
  }

  @Override
  @Transactional(readOnly = true)
  public VoteResponse getVote(@NonNull Long userId, @NonNull Long entityId, @NonNull String entityType) {
    logger.debug("Getting vote: User ID={}, Entity ID={}, Entity Type={}",
            userId, entityId, entityType);

    var user = userRepository.findById(userId)
            .orElseThrow(() -> {
              logger.warn("User not found with ID: {}", userId);
              return new ResourceNotFoundException("User", "id", userId);
            });

    var vote = voteRepository.findByUserAndEntityIdAndEntityType(user, entityId, entityType)
            .orElseThrow(() -> {
              logger.warn("Vote not found for User ID={}, Entity ID={}, Entity Type={}",
                      userId, entityId, entityType);
              return new ResourceNotFoundException("Vote", "user and entity",
                      userId + ", " + entityId + ", " + entityType);
            });

    return voteMapper.toVoteResponse(vote);
  }

  @Override
  @Transactional(readOnly = true)
  public List<VoteResponse> getVotesByEntity(@NonNull Long entityId, @NonNull String entityType) {
    logger.debug("Getting votes for entity: ID={}, Type={}", entityId, entityType);

    strategyContext.findByTypeAndId(entityType, entityId);

    List<Vote> votes = voteRepository.findAllByEntityIdAndEntityType(entityId, entityType);
    logger.debug("Found {} votes for entity: ID={}, Type={}", votes.size(), entityId, entityType);

    return votes.stream()
            .map(voteMapper::toVoteResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<VoteResponse> getVotesByUser(@NonNull Long userId) {
    logger.debug("Getting votes by user: ID={}", userId);

    var user = userRepository.findById(userId)
            .orElseThrow(() -> {
              logger.warn("User not found with ID: {}", userId);
              return new ResourceNotFoundException("User", "id", userId);
            });

    List<Vote> votes = voteRepository.findAllByUser(user);
    logger.debug("Found {} votes by user: ID={}", votes.size(), userId);

    return votes.stream()
            .map(voteMapper::toVoteResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public VoteCount getVoteCounts(@NonNull Long entityId, @NonNull String entityType) {
    logger.debug("Getting vote counts for entity: ID={}, Type={}", entityId, entityType);

    strategyContext.findByTypeAndId(entityType, entityId);

    var upvoteCount = voteRepository.countByEntityIdAndEntityTypeAndVoteType(
            entityId, entityType, VoteType.UPVOTE);

    var downvoteCount = voteRepository.countByEntityIdAndEntityTypeAndVoteType(
            entityId, entityType, VoteType.DOWNVOTE);

    var score = voteRepository.getVoteScore(entityId, entityType);

    logger.debug("Vote counts for entity ID={}, Type={}: Upvotes={}, Downvotes={}, Score={}",
            entityId, entityType, upvoteCount, downvoteCount, score != null ? score : 0);

    return voteMapper.toVoteCount(entityId, entityType, upvoteCount, downvoteCount, score != null ? score : 0);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean hasVoted(@NonNull Long userId, @NonNull Long entityId, @NonNull String entityType) {
    logger.debug("Checking if user has voted: User ID={}, Entity ID={}, Entity Type={}",
            userId, entityId, entityType);

    var user = userRepository.findById(userId)
            .orElseThrow(() -> {
              logger.warn("User not found with ID: {}", userId);
              return new ResourceNotFoundException("User", "id", userId);
            });

    boolean hasVoted = voteRepository.findByUserAndEntityIdAndEntityType(user, entityId, entityType).isPresent();
    logger.debug("User ID={} has {} for Entity ID={}, Type={}",
            userId, hasVoted ? "voted" : "not voted", entityId, entityType);

    return hasVoted;
  }

  @Override
  @Transactional(readOnly = true)
  public VoteType getUserVoteType(@NonNull Long userId, @NonNull Long entityId, @NonNull String entityType) {
    logger.debug("Getting vote type for user: User ID={}, Entity ID={}, Entity Type={}",
            userId, entityId, entityType);

    var user = userRepository.findById(userId)
            .orElseThrow(() -> {
              logger.warn("User not found with ID: {}", userId);
              return new ResourceNotFoundException("User", "id", userId);
            });

    var vote = voteRepository.findByUserAndEntityIdAndEntityType(user, entityId, entityType);
    VoteType voteType = vote.map(Vote::getVoteType).orElse(null);

    logger.debug("User ID={} vote type for Entity ID={}, Type={}: {}",
            userId, entityId, entityType, voteType != null ? voteType : "None");

    return voteType;
  }

  @Recover
  public VoteResponse recoverFromOptimisticLockingFailureOnCastVote(
          ObjectOptimisticLockingFailureException ex,
          @NonNull @Valid VoteCreate voteCreate) {
    logger.error("Optimistic locking failure during vote casting: {}", ex.getMessage());
    throw new ConcurrentModificationException("The vote has been modified by someone else. Please try again.", ex);
  }

  @Recover
  public VoteResponse recoverFromOptimisticLockingFailureOnUpdateVote(
          ObjectOptimisticLockingFailureException ex,
          @NonNull Long userId,
          @NonNull Long entityId,
          @NonNull String entityType,
          @NonNull @Valid VoteUpdate voteUpdate) {
    logger.error("Optimistic locking failure during vote update: {}", ex.getMessage());
    throw new ConcurrentModificationException("The vote has been modified by someone else. Please try again.", ex);
  }

  @Recover
  public void recoverFromOptimisticLockingFailureOnDeleteVote(
          ObjectOptimisticLockingFailureException ex,
          @NonNull Long userId,
          @NonNull Long entityId,
          @NonNull String entityType) {
    logger.error("Optimistic locking failure during vote deletion: {}", ex.getMessage());
    throw new ConcurrentModificationException("The vote has been modified by someone else. Please try again.", ex);
  }
}