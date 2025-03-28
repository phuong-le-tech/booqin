package fr.uge.booqIn.event.listener;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.event.publishable.BookAddedEvent;
import fr.uge.booqIn.service.GlobalCollectionManager;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.logging.Logger;

@Component
public class GlobalCollectionListener {
    private static final Logger logger = Logger.getLogger(GlobalCollectionListener.class.getName());
    private final GlobalCollectionManager globalCollectionManager;
    private final BookServiceInterface bookRepository;

    public GlobalCollectionListener(GlobalCollectionManager globalCollectionManager, BookServiceInterface bookRepository) {
        this.globalCollectionManager = globalCollectionManager;
        this.bookRepository = bookRepository;
    }

    // Effectué seulent apres save en db et créer une nouvelle transaction
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // attend que ca été validé en bd
    @Transactional(propagation = Propagation.REQUIRES_NEW) // nouvelle transaction
    public void handleEvent(BookAddedEvent event) {
        Long bookId = event.bookId();
        boolean isNewBook = event.isNewBook();
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