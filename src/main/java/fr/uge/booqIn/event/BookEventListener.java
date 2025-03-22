package fr.uge.booqIn.event;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.service.GlobalCollectionManager;
import org.springframework.stereotype.Component;

@Component
public class BookEventListener implements EventListener<BookCreatedEvent> {

  private final GlobalCollectionManager collectionManager;

  public BookEventListener(GlobalCollectionManager collectionManager) {
    this.collectionManager = collectionManager;
  }

  @Override
  public void processEvent(BookCreatedEvent event) {
    Book book = event.getBook();
    collectionManager.updateAuthorCollectionsForNewBook(book);
  }
}