package fr.uge.booqIn.event;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.service.GlobalCollectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BookEventListener {

  private final GlobalCollectionManager collectionManager;

  @Autowired
  public BookEventListener(GlobalCollectionManager collectionManager) {
    this.collectionManager = collectionManager;
  }

  @EventListener
  @Transactional
  public void handleBookCreatedEvent(BookCreatedEvent event) {
    Book book = event.getBook();
    collectionManager.updateAuthorCollectionsForNewBook(book);
  }
}