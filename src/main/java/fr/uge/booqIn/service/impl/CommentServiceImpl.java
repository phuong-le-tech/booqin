package fr.uge.booqIn.service.impl;

import fr.uge.booqIn.dto.comment.CommentCreateDTO;
import fr.uge.booqIn.dto.comment.CommentDTO;
import fr.uge.booqIn.dto.comment.CommentUpdateDTO;
import fr.uge.booqIn.entity.Comment;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.mapper.CommentMapper;
import fr.uge.booqIn.repository.CommentRepository;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.service.interfaces.CommentServiceInterface;
import fr.uge.booqIn.specification.CommentSpecification;
import fr.uge.booqIn.strategy.EntityStrategyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Objects;

@Service
public class CommentServiceImpl implements CommentServiceInterface {
    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EntityStrategyContext strategyContext;
    private final CommentMapper commentMapper;

    public CommentServiceImpl(CommentRepository commentRepository, UserRepository userRepository,
                              EntityStrategyContext strategyContext, CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.strategyContext = strategyContext;
        this.commentMapper = commentMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommentDTO> findComments(String content, Long userId, String entityType, Long entityId, Pageable pageable) {
        logger.info("Finding comments with filters: content={}, userId={}, entityType={}, entityId={}",
                content, userId, entityType, entityId);

        Specification<Comment> spec = Specification.where(null);

        if (content != null && !content.isEmpty()) {
            spec = spec.and(CommentSpecification.hasContent(content));
        }

        if (userId != null) {
            spec = spec.and(CommentSpecification.byUser(userId));
        }

        if (entityType != null && !entityType.isEmpty()) {
            spec = spec.and(CommentSpecification.forEntityType(entityType));
        }

        if (entityId != null) {
            spec = spec.and(CommentSpecification.forEntityId(entityId));
        }

        return commentRepository.findAll(spec, pageable)
                .map(commentMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDTO getById(@NonNull Long commentId) {
        logger.info("Finding comment with id: {}", commentId);

        Comment comment = findCommentById(commentId);
        return commentMapper.toDTO(comment);
    }

    @Override
    @Transactional
    @Retryable(
            maxAttemptsExpression = "${spring.retry.max-attempts}",
            retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
            backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
    )
    public CommentDTO create(@NonNull CommentCreateDTO createDTO) {
        logger.info("Creating a new comment");

        // Validate parent comment first if provided
        Comment parentComment = null;
        if (createDTO.parentId() != null) {
            parentComment = commentRepository.findById(createDTO.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", createDTO.parentId()));

            if (!parentComment.getEntityId().equals(createDTO.entityId()) ||
                    !parentComment.getEntityType().equals(createDTO.entityType())) {
                throw new IllegalArgumentException("Parent comment must belong to the same entity");
            }
        }

        var user = userRepository.findById(createDTO.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", createDTO.userId()));

        var targetEntity = strategyContext.findByTypeAndId(
                createDTO.entityType(),
                createDTO.entityId());

        var comment = new Comment();
        comment.setContent(createDTO.content());
        comment.setUser(user);
        comment.setTargetEntity(targetEntity);
        comment.setParentComment(parentComment);
        comment.setCreatedAt(LocalDateTime.now());
        comment = commentRepository.save(comment);

        return commentMapper.toDTO(comment);
    }

    @Override
    @Transactional
    @Retryable(
            maxAttemptsExpression = "${spring.retry.max-attempts}",
            retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
            backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
    )
    public Comment createComment(@NonNull @Validated Comment comment) {
        logger.info("Creating a new comment from entity");

        Objects.requireNonNull(comment, "Comment to save cannot be null");
        Objects.requireNonNull(comment.getUser(), "Comment user cannot be null");

        if (!userRepository.existsById(comment.getUser().getId())) {
            throw new ResourceNotFoundException("User", "id", comment.getUser().getId());
        }

        if (comment.getCreatedAt() == null) {
            comment.setCreatedAt(LocalDateTime.now());
        }

        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    @Retryable(
            maxAttemptsExpression = "${spring.retry.max-attempts}",
            retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
            backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
    )
    public CommentDTO update(@NonNull Long commentId, @NonNull Long userId, @NonNull CommentUpdateDTO updateDTO) {
        logger.info("Updating comment with id: {}", commentId);

        Comment comment = findCommentById(commentId);

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to update this comment");
        }

        comment.setContent(updateDTO.content());
        comment.setUpdatedAt(LocalDateTime.now());
        comment = commentRepository.save(comment);

        return commentMapper.toDTO(comment);
    }

    @Override
    @Transactional
    @Retryable(
            maxAttemptsExpression = "${spring.retry.max-attempts}",
            retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
            backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
    )
    public Comment updateComment(@NonNull @Validated Comment comment) {
        logger.info("Updating comment from entity with id: {}", comment.getId());

        if (!commentRepository.existsById(comment.getId())) {
            throw new ResourceNotFoundException("Comment", "id", comment.getId());
        }

        comment.setUpdatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    @Retryable(
            maxAttemptsExpression = "${spring.retry.max-attempts}",
            retryFor = {ConcurrentModificationException.class, ObjectOptimisticLockingFailureException.class},
            backoff = @Backoff(delayExpression = "${spring.retry.backoff.delay}")
    )
    public void delete(@NonNull Long commentId, @NonNull Long userId) {
        logger.info("Deleting comment with id: {}", commentId);

        Comment comment = findCommentById(commentId);

        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("User is not authorized to delete this comment");
        }

        commentRepository.delete(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDTO> getByEntity(@NonNull Long entityId, @NonNull String entityType) {
        logger.info("Finding root comments for entity of type {} with id: {}", entityType, entityId);

        strategyContext.findByTypeAndId(entityType, entityId);

        var comments = commentRepository.findRootCommentsByEntity(entityId, entityType);

        return commentMapper.toDTOList(comments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDTO> getRepliesByCommentId(@NonNull Long commentId) {
        logger.info("Finding replies to comment with id: {}", commentId);

        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment", "id", commentId);
        }
        var replies = commentRepository.findRepliesByParentCommentId(commentId);
        return commentMapper.toDTOList(replies);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDTO> getByUser(@NonNull Long userId) {
        logger.info("Finding comments by user with id: {}", userId);

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        var comments = commentRepository.findByUserOrderByCreatedAtDesc(user);
        return commentMapper.toDTOList(comments);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countByEntity(@NonNull Long entityId, @NonNull String entityType) {
        logger.info("Counting comments for entity of type {} with id: {}", entityType, entityId);

        strategyContext.findByTypeAndId(entityType, entityId);
        return commentRepository.countByEntity(entityId, entityType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDTO> getCommentTreeByEntity(@NonNull Long entityId, @NonNull String entityType) {
        logger.info("Finding comment tree for entity of type {} with id: {}", entityType, entityId);

        strategyContext.findByTypeAndId(entityType, entityId);
        var rootComments = commentRepository.findRootCommentsWithRepliesByEntity(entityId, entityType);
        return commentMapper.toTreeDTOList(rootComments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> findAll() {
        logger.info("Finding all comments");
        return commentRepository.findAll();
    }

    private Comment findCommentById(Long id) {
        return commentRepository.findWithUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", id));
    }

    @Recover
    public CommentDTO recoverFromOptimisticLockingFailureOnCreate(ObjectOptimisticLockingFailureException ex, @NonNull CommentCreateDTO createDTO) {
        logger.error("Optimistic locking failure during comment creation: {}", ex.getMessage());
        throw new ConcurrentModificationException("The comment could not be created due to concurrent modifications. Please try again.", ex);
    }

    @Recover
    public Comment recoverFromOptimisticLockingFailureOnCreateComment(ObjectOptimisticLockingFailureException ex, @NonNull @Validated Comment comment) {
        logger.error("Optimistic locking failure during comment creation: {}", ex.getMessage());
        throw new ConcurrentModificationException("The comment could not be created due to concurrent modifications. Please try again.", ex);
    }

    @Recover
    public CommentDTO recoverFromOptimisticLockingFailureOnUpdate(ObjectOptimisticLockingFailureException ex, @NonNull Long commentId, @NonNull Long userId, @NonNull CommentUpdateDTO updateDTO) {
        logger.error("Optimistic locking failure during comment update: {}", ex.getMessage());
        throw new ConcurrentModificationException("The comment has been modified by someone else. Please try again.", ex);
    }

    @Recover
    public Comment recoverFromOptimisticLockingFailureOnUpdateComment(ObjectOptimisticLockingFailureException ex, @NonNull @Validated Comment comment) {
        logger.error("Optimistic locking failure during comment update: {}", ex.getMessage());
        throw new ConcurrentModificationException("The comment has been modified by someone else. Please try again.", ex);
    }

    @Recover
    public void recoverFromOptimisticLockingFailureOnDelete(ObjectOptimisticLockingFailureException ex, @NonNull Long commentId, @NonNull Long userId) {
        logger.error("Optimistic locking failure during comment deletion: {}", ex.getMessage());
        throw new ConcurrentModificationException("The comment has been modified by someone else. Please try again.", ex);
    }
}