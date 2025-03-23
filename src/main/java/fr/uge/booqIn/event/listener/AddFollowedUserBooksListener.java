package fr.uge.booqIn.event.listener;

import fr.uge.booqIn.entity.AbstractBookCollection;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.event.BooqInEventListener;
import fr.uge.booqIn.event.publishable.AddFollowedUserBooks;
import fr.uge.booqIn.repository.BookCollectionRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.logging.Logger;

@Component
public class AddFollowedUserBooksListener implements BooqInEventListener<AddFollowedUserBooks> {
  private static final Logger logger = Logger.getLogger(AddFollowedUserBooksListener.class.getName());
  private final BookCollectionRepository bookCollectionRepository;
  private final Random random = new Random();

  public AddFollowedUserBooksListener(BookCollectionRepository bookCollectionRepository) {
    this.bookCollectionRepository = bookCollectionRepository;
  }

  @Override
  @EventListener
  public void handleEvent(AddFollowedUserBooks event) {
    User follower = event.follower();
    User followed = event.followed();

    logger.info(String.format("Processing follow event: User %s is following User %s",
            follower.getFullName(), followed.getFullName()));

    var followerFollowedCollection = follower.getFollowedUsersCollection();
    var followedPublicCollection = bookCollectionRepository.findPublicCollectionsByOwner(followed);

    if (followedPublicCollection.isEmpty()) {
      logger.info("No public collections found for user " + followed.getId());
      return;
    }

    // Select a random public collection
    AbstractBookCollection randomCollection =
            followedPublicCollection.get(random.nextInt(followedPublicCollection.size()));

    logger.info(String.format("Selected random collection: %s from user %s",
            randomCollection.getTitle(), followed.getFullName()));

    int addedBooks = 0;
    for (Book book : randomCollection.getBooks()) {
      followerFollowedCollection.addBook(book);
      logger.fine("Added book: " + book.getTitle() + " to followed users collection");
    }

    logger.info(String.format("Added %d books from collection %s to user %s's followed books collection",
            addedBooks, randomCollection.getTitle(), follower.getFullName()));
  }
}
