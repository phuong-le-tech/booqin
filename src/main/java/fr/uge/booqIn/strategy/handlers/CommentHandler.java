package fr.uge.booqIn.strategy.handlers;

import fr.uge.booqIn.entity.Comment;
import fr.uge.booqIn.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CommentHandler implements EntityHandler<Comment>{
  private final CommentRepository commentRepository;

  public CommentHandler(CommentRepository commentRepository) {
    this.commentRepository = commentRepository;
  }

  @Override
  public String getEntityType() {
    return "COMMENT";
  }

  @Override
  public Optional<Comment> findById(Long id) {
    return commentRepository.findById(id);
  }
}
