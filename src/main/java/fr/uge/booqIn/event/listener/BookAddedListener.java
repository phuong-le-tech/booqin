package fr.uge.booqIn.event.listener;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.event.BooqInEventListener;
import fr.uge.booqIn.event.publishable.BookAddedEvent;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.service.GlobalCollectionManager;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Optional;
import java.util.logging.Logger;

@Component
public class BookAddedListener implements BooqInEventListener<BookAddedEvent> {
    private static final Logger logger = Logger.getLogger(BookAddedListener.class.getName());
    private final GlobalCollectionManager globalCollectionManager;
    private final BookServiceInterface bookRepository;

    public BookAddedListener(GlobalCollectionManager globalCollectionManager, BookServiceInterface bookRepository) {
        this.globalCollectionManager = globalCollectionManager;
        this.bookRepository = bookRepository;
    }

    @Override
    public void handleEvent(BookAddedEvent event) {
        Long bookId = event.bookId();
        boolean isNewBook = event.isNewBook();

        logger.info("Handling BookAddedEvent for book ID: " + bookId +
                " (isNewBook: " + isNewBook + ")");

//        Optional<Book> bookOpt = bookRepository.findById(bookId);
//
//        if (bookOpt.isEmpty()) {
//            logger.warning("Book with ID " + bookId + " not found when processing BookAddedEvent. Skipping collection updates.");
//            return;
//        }

        Book book = bookRepository.findBookById(bookId);

        try {
            globalCollectionManager.updateAuthorCollectionsForNewBook(book);

            if (isNewBook) {
                globalCollectionManager.refreshRecentBooksCollection();
            }
        } catch (Exception e) {
            logger.severe("Error updating collections for book ID " + bookId + ": " + e.getMessage());
        }
    }
}