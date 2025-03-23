package fr.uge.booqIn.event.listener;

import fr.uge.booqIn.entity.AbstractBookCollection;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.event.BooqInEventListener;
import fr.uge.booqIn.event.publishable.RemoveFollowedUserBooks;
import fr.uge.booqIn.repository.BookCollectionRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class RemoveFollowedUserBooksListener implements BooqInEventListener<RemoveFollowedUserBooks> {
  private static final Logger logger = Logger.getLogger(RemoveFollowedUserBooksListener.class.getName());
  private final BookCollectionRepository bookCollectionRepository;

  public RemoveFollowedUserBooksListener(BookCollectionRepository bookCollectionRepository) {
    this.bookCollectionRepository = bookCollectionRepository;
  }

  @Override
  @EventListener
  public void handleEvent(RemoveFollowedUserBooks event) {
    User follower = event.follower();
    User followed = event.followed();

    logger.info(String.format("Processing unfollow event: User %s is unfollowing User %s",
            follower.getFullName(), followed.getFullName()));

    var followerFollowedCollection = follower.getFollowedUsersCollection();
    var followedPublicCollections = bookCollectionRepository.findPublicCollectionsByOwner(followed);

    if (followedPublicCollections.isEmpty()) {
      logger.info("No public collections found for user " + followed.getId());
      return;
    }

    int removedBooks = 0;
    for (AbstractBookCollection collection : followedPublicCollections) {
      for (Book book : collection.getBooks()) {
        followerFollowedCollection.removeBook(book);
        removedBooks++;
        logger.fine("Removed book: " + book.getTitle() + " from followed users collection");
      }
    }

    logger.info(String.format("Removed %d books from user %s's followed books collection",
            removedBooks, follower.getFullName()));
  }
}