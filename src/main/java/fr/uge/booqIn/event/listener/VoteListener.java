package fr.uge.booqIn.event.listener;

import fr.uge.booqIn.entity.AbstractBookCollection;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.enums.VoteType;
import fr.uge.booqIn.event.publishable.VoteBookEvent;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.repository.BookCollectionRepository;
import fr.uge.booqIn.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;

@Component
public class VoteListener {
  private static final Logger logger = LoggerFactory.getLogger(VoteListener.class);

  private final BookCollectionRepository bookCollectionRepository;
  private final BookRepository bookRepository;

  public VoteListener(
          BookCollectionRepository bookCollectionRepository,
          BookRepository bookRepository) {
    this.bookCollectionRepository = bookCollectionRepository;
    this.bookRepository = bookRepository;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleOnVoteBook(VoteBookEvent event) {
    Long userId = event.userId();
    Long bookId = event.bookId();
    VoteType voteType = event.voteType();

    logger.debug("Processing book vote event: userId={}, bookId={}, voteType={}", userId, bookId, voteType);

    if (voteType != VoteType.UPVOTE) {
      logger.debug("Skipping non-upvote event");
      return;
    }

    try {
      Optional<AbstractBookCollection> collectionOpt = bookCollectionRepository.findLikedCollectionByOwnerId(userId);
      AbstractBookCollection likedCollection = collectionOpt.orElseThrow(() ->
              new ResourceNotFoundException("Liked collection not found for user: " + userId));

      Book book = bookRepository.findById(bookId)
              .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + bookId));

      likedCollection.addBook(book);
      bookCollectionRepository.save(likedCollection);

      logger.info("Successfully added book id={} to user's (id={}) liked collection", bookId, userId);

    } catch (ResourceNotFoundException e) {
      logger.warn("Unable to add book to liked collection: {}", e.getMessage());
    } catch (IllegalArgumentException e) {
      logger.error("Book already in the collection", e);
    }
  }
}