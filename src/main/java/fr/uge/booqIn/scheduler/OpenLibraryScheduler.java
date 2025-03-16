package fr.uge.booqIn.scheduler;

import fr.uge.booqIn.adapter.bookApi.BookFetcher;
import fr.uge.booqIn.adapter.bookApi.BookFetcherFactory;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookLending;
import fr.uge.booqIn.enums.BookStatus;
import fr.uge.booqIn.repository.BookLendingRepository;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Component
public class OpenLibraryScheduler {
    private static final Logger logger = Logger.getLogger(OpenLibraryScheduler.class.getName());
    private static final int PAGE_SIZE = 10;
    private final AuthorIterator authorIterator;
    private final BookServiceInterface bookService;
    private final BookFetcher openApiAdapter;
    private final  BookLendingRepository bookLendingRepository;

    public OpenLibraryScheduler(NovelistsBean novelistsBean, BookServiceInterface bookService, BookLendingRepository bookLendingRepository) {
        this.authorIterator = new AuthorIterator(novelistsBean, 15);
        this.bookService = bookService;
      this.bookLendingRepository = bookLendingRepository;
      this.openApiAdapter = BookFetcherFactory.adapter("open");
      
    }
    
    @Scheduled(cron = "* *  2 * * *")    // * * 2 * * * -> every 2am
    public void fetchOpenLibraryBooks() {
        List<String> authors = authorIterator.getNextBatch();

        if (authors.isEmpty()) {
            logger.warning("No authors found for OpenLibrary API.");
            return;
        }
        authors.forEach(this::fetchPaginatedBooks);
    }

    private void fetchPaginatedBooks(String author) {
        int page = 1;
        List<Book> books;
        do {
            books = openApiAdapter.searchBookByAuthorWithPagination(author, page, PAGE_SIZE);
            books.forEach(bookService::saveFromApi);
            page++;
        } while (!books.isEmpty());
    }
    
    
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expireReservations() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(20);
        List<BookLending> expiredReservations = bookLendingRepository.findByStatusAndReservedAtBefore(BookStatus.RESERVED, expirationTime);
        for (BookLending lending : expiredReservations) {
            lending.setStatus(BookStatus.AVAILABLE);
            lending.setReservedAt(null);
            lending.setBorrower(null);
            bookLendingRepository.save(lending);
        }
    }
    
}