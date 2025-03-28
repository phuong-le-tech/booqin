package fr.uge.booqIn.event.listener;

import fr.uge.booqIn.entity.AbstractBookCollection;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.enums.VoteType;
import fr.uge.booqIn.event.publishable.VoteBookEvent;
import fr.uge.booqIn.exception.ResourceNotFoundException;
import fr.uge.booqIn.repository.BookCollectionRepository;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.service.interfaces.BookCollectionServiceInterface;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
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

  private final BookCollectionServiceInterface bookCollectionService;
  private final BookServiceInterface bookService;

  public VoteListener(
          BookCollectionServiceInterface bookCollectionService,
          BookServiceInterface bookService) {
    this.bookCollectionService = bookCollectionService;
    this.bookService = bookService;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleOnVoteBook(VoteBookEvent event) {
    Long userId = event.userId();
    Long bookId = event.bookId();
    VoteType voteType = event.voteType();

    logger.debug("Processing book vote event: userId={}, bookId={}, voteType={}", userId, bookId, voteType);
    AbstractBookCollection collection = bookCollectionService.findLikedCollectionByOwnerId(userId);
    Book book = bookService.findBookById(bookId);

    if (voteType != VoteType.UPVOTE) {
      bookCollectionService.removeBookFromCollection(collection.getId(), book.getId());
      return;
    }
    bookCollectionService.addBookToCollection(collection.getId(), book.getId());
  }
}