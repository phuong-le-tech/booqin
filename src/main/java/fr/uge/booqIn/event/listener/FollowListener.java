package fr.uge.booqIn.event.listener;

import fr.uge.booqIn.entity.AbstractBookCollection;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.event.publishable.AddFollowedUserBooks;
import fr.uge.booqIn.event.publishable.RemoveFollowedUserBooks;
import fr.uge.booqIn.service.interfaces.BookCollectionServiceInterface;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Random;
import java.util.logging.Logger;

@Component
public class FollowListener {
  private static final Logger logger = Logger.getLogger(FollowListener.class.getName());
  private final BookCollectionServiceInterface bookCollectionService;
  private final Random random = new Random();

  public FollowListener(BookCollectionServiceInterface bookCollectionService) {
    this.bookCollectionService = bookCollectionService;
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleOnFollow(AddFollowedUserBooks event) {
    User follower = event.follower();
    User followed = event.followed();

    logger.info(String.format("Processing follow event: User %s is following User %s",
            follower.getFullName(), followed.getFullName()));

    var followerFollowedCollection = follower.getFollowedUsersCollection();
    var followedPublicCollections = bookCollectionService.findPublicCollectionsByOwnerId(followed.getId());

    if (followedPublicCollections.isEmpty()) {
      logger.info("No public collections found for user " + followed.getId());
      return;
    }

    AbstractBookCollection randomCollection =
            followedPublicCollections.get(random.nextInt(followedPublicCollections.size()));

    logger.info(String.format("Selected random collection: %s from user %s",
            randomCollection.getTitle(), followed.getFullName()));

    int addedBooks = 0;
    logger.info("Adding books from collection to user's followed books collection " + randomCollection.getBooks().size());
    for (Book book : randomCollection.getBooks()) {
      bookCollectionService.addBookToCollection(followerFollowedCollection.getId(), book.getId());
      logger.fine("Added book: " + book.getTitle() + " to followed users collection");
    }

    logger.info(String.format("Added %d books from collection %s to user %s's followed books collection",
            addedBooks, randomCollection.getTitle(), follower.getFullName()));
  }

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleOnUnfollowed(RemoveFollowedUserBooks event) {
    User follower = event.follower();
    User followed = event.followed();

    logger.info(String.format("Processing unfollow event: User %s is unfollowing User %s",
            follower.getFullName(), followed.getFullName()));

    var followerFollowedCollection = follower.getFollowedUsersCollection();
    var followedPublicCollections = bookCollectionService.findPublicCollectionsByOwnerId(followed.getId());

    if (followedPublicCollections.isEmpty()) {
      logger.info("No public collections found for user " + followed.getId());
      return;
    }

    // Oui c'est dégueulasse
    int removedBooks = 0;
    for (AbstractBookCollection collection : followedPublicCollections) {
      for (Book book : collection.getBooks()) {
        bookCollectionService.removeBookFromCollection(followerFollowedCollection.getId(), book.getId());
        removedBooks++;
        logger.fine("Removed book: " + book.getTitle() + " from followed users collection");
      }
    }

    logger.info(String.format("Removed %d books from user %s's followed books collection",
            removedBooks, follower.getFullName()));
  }
}
