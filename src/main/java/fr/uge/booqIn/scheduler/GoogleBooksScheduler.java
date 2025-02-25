package fr.uge.booqIn.scheduler;

import fr.uge.booqIn.Adapter.BookAPIs.BookFetcher;
import fr.uge.booqIn.Adapter.BookAPIs.BookFetcherFactory;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.service.BookService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Logger;

@Component
public class GoogleBooksScheduler {
    private static final Logger logger = Logger.getLogger(GoogleBooksScheduler.class.getName());
    private final AuthorIterator authorIterator;
    private final BookService bookService;
    private final BookFetcher googleBooksFetcher;

    public GoogleBooksScheduler(NovelistsBean novelistsBean, BookService bookService) {
        this.authorIterator = new AuthorIterator(novelistsBean, 15);
        this.bookService = bookService;
        this.googleBooksFetcher = BookFetcherFactory.adapter("google");
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void fetchGoogleBooks() {
        List<String> authors = authorIterator.getNextBatch();

        if (authors.isEmpty()) {
            logger.warning("No authors found for Google Books API.");
            return;
        }

        authors.stream()
            .map(googleBooksFetcher::searchByAuthor)
            .flatMap(List::stream)
            .forEach(bookService::save);
    }
}