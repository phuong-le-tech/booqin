package fr.uge.booqIn.scheduler;

import fr.uge.booqIn.Adapter.BookAPIs.BookFetcher;
import fr.uge.booqIn.Adapter.BookAPIs.OpenApiAdapter;
import fr.uge.booqIn.Adapter.BookAPIs.BookFetcherFactory;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.service.BookService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Logger;

@Component
public class OpenLibraryScheduler {
    private static final Logger logger = Logger.getLogger(OpenLibraryScheduler.class.getName());
    private static final int PAGE_SIZE = 10;
    private final AuthorIterator authorIterator;
    private final BookService bookService;
    private final BookFetcher openApiAdapter;

    public OpenLibraryScheduler(NovelistsBean novelistsBean, BookService bookService) {
        this.authorIterator = new AuthorIterator(novelistsBean, 15);
        this.bookService = bookService;
        this.openApiAdapter = BookFetcherFactory.adapter("open");
    }

    @Scheduled(cron = "* * 2 * * *")
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
            books.forEach(bookService::save);
            page++;
        } while (!books.isEmpty());
    }
}