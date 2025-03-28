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
import fr.uge.booqIn.model.Votable;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.repository.VoteRepository;
import fr.uge.booqIn.service.interfaces.VoteServiceInterface;
import org.springframework.transaction.annotation.Transactional;
import fr.uge.booqIn.strategy.EntityStrategyContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VoteServiceImpl implements VoteServiceInterface {
  private final VoteRepository voteRepository;
  private final UserRepository userRepository;
  private final EntityStrategyContext strategyContext;
  private final VoteMapper voteMapper;
  private final BooqInEventPublisher eventPublisher;

  public VoteServiceImpl(VoteRepository voteRepository,
                         UserRepository userRepository,
                         EntityStrategyContext strategyContext,
                         VoteMapper voteMapper,
                         BooqInEventPublisher eventPublisher) {
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
  public VoteResponse castVote(@NotNull @Valid VoteCreate voteCreate) {
    var user = userRepository.findById(voteCreate.userId())
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", voteCreate.userId()));

    var targetEntity = strategyContext.findByTypeAndId(
            voteCreate.entityType(),
            voteCreate.entityId());

    if (!(targetEntity instanceof Votable)) {
      throw new IllegalArgumentException("Entity of type " + voteCreate.entityType() + " is not votable");
    }

    // Récupérer explicitement le type d'entité avant de sauvegarder
    String entityType = voteCreate.entityType();
    Long entityId = voteCreate.entityId();

    var existingVote = voteRepository.findByUserAndEntityIdAndEntityType(
            user, entityId, entityType);

    Vote vote;
    if (existingVote.isPresent()) {
      vote = existingVote.get();
      vote.setVoteType(voteCreate.voteType());
    } else {
      vote = new Vote(user, voteCreate.voteType());
      vote.setEntityId(entityId);
      vote.setEntityType(entityType);
    }

    vote = voteRepository.save(vote);

    if (voteCreate.entityType().equals("BOOK")) {
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
  public VoteResponse updateVote(@NotNull Long userId, @NotNull Long entityId,
                                 @NotNull String entityType, @NotNull @Valid VoteUpdate voteUpdate) {
    var user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    var targetEntity = strategyContext.findByTypeAndId(entityType, entityId);

    if (!(targetEntity instanceof Votable)) {
      throw new IllegalArgumentException("Entity of type " + entityType + " is not votable");
    }

    var vote = voteRepository.findByUserAndEntityIdAndEntityType(user, entityId, entityType)
            .orElseThrow(() -> new ResourceNotFoundException("Vote", "user and entity",
                    userId + ", " + entityId + ", " + entityType));

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
  public void deleteVote(@NotNull Long userId, @NotNull Long entityId, @NotNull String entityType) {
    var user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    var vote = voteRepository.findByUserAndEntityIdAndEntityType(user, entityId, entityType)
            .orElseThrow(() -> new ResourceNotFoundException("Vote", "user and entity",
                    userId + ", " + entityId + ", " + entityType));

    voteRepository.delete(vote);
  }

  @Override
  @Transactional
  public VoteResponse getVote(@NotNull Long userId, @NotNull Long entityId, @NotNull String entityType) {
    var user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    var vote = voteRepository.findByUserAndEntityIdAndEntityType(user, entityId, entityType)
            .orElseThrow(() -> new ResourceNotFoundException("Vote", "user and entity",
                    userId + ", " + entityId + ", " + entityType));

    return voteMapper.toVoteResponse(vote);
  }

  @Override
  @Transactional
  public List<VoteResponse> getVotesByEntity(@NotNull Long entityId, @NotNull String entityType) {
    strategyContext.findByTypeAndId(entityType, entityId);

    return voteRepository.findAllByEntityIdAndEntityType(entityId, entityType).stream()
            .map(voteMapper::toVoteResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public List<VoteResponse> getVotesByUser(@NotNull Long userId) {
    var user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    return voteRepository.findAllByUser(user).stream()
            .map(voteMapper::toVoteResponse)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public VoteCount getVoteCounts(@NotNull Long entityId, @NotNull String entityType) {
    strategyContext.findByTypeAndId(entityType, entityId);

    var upvoteCount = voteRepository.countByEntityIdAndEntityTypeAndVoteType(
            entityId, entityType, VoteType.UPVOTE);

    var downvoteCount = voteRepository.countByEntityIdAndEntityTypeAndVoteType(
            entityId, entityType, VoteType.DOWNVOTE);

    var score = voteRepository.getVoteScore(entityId, entityType);

    return voteMapper.toVoteCount(entityId, entityType, upvoteCount, downvoteCount, score != null ? score : 0);
  }

  @Override
  @Transactional
  public boolean hasVoted(@NotNull Long userId, @NotNull Long entityId, @NotNull String entityType) {
    var user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    return voteRepository.findByUserAndEntityIdAndEntityType(user, entityId, entityType).isPresent();
  }

  @Override
  @Transactional
  public VoteType getUserVoteType(@NotNull Long userId, @NotNull Long entityId, @NotNull String entityType) {
    var user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    var vote = voteRepository.findByUserAndEntityIdAndEntityType(user, entityId, entityType);
    return vote.map(Vote::getVoteType).orElse(null);
  }

  @Recover
  public VoteResponse recoverFromOptimisticLockingFailureOnCastVote(ObjectOptimisticLockingFailureException ex, @NotNull @Valid VoteCreate voteCreate) {
    throw new ConcurrentModificationException("The vote has been modified by someone else. Please try again.", ex);
  }

  @Recover
  public VoteResponse recoverFromOptimisticLockingFailureOnUpdateVote(ObjectOptimisticLockingFailureException ex,
                                                                      @NotNull Long userId,
                                                                      @NotNull Long entityId,
                                                                      @NotNull String entityType,
                                                                      @NotNull @Valid VoteUpdate voteUpdate) {
    throw new ConcurrentModificationException("The vote has been modified by someone else. Please try again.", ex);
  }

  @Recover
  public void recoverFromOptimisticLockingFailureOnDeleteVote(ObjectOptimisticLockingFailureException ex,
                                                              @NotNull Long userId,
                                                              @NotNull Long entityId,
                                                              @NotNull String entityType) {
    throw new ConcurrentModificationException("The vote has been modified by someone else. Please try again.", ex);
  }
}