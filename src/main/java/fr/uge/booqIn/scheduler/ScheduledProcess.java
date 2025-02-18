package fr.uge.booqIn.scheduler;

import fr.uge.booqIn.Adapter.BookAPIs.BookFetcher;
import fr.uge.booqIn.Adapter.BookAPIs.BookFetcherFactory;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.repository.BookService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Logger;

@Component
public class ScheduledProcess {
  private static final Logger logger = Logger.getLogger(ScheduledProcess.class.getName());
  private final AuthorIterator authorIterator;
  private final BookService bookService;

  public ScheduledProcess(NovelistsBean novelistsBean, BookService bookService) {
    this.authorIterator = new AuthorIterator(novelistsBean, 15);
    this.bookService = bookService;
  }

  @Scheduled(cron = "0 0 0 * * *")
  public void askAPI() {
    List<String> authors = authorIterator.getNextBatch();

    if (authors.isEmpty()) {
      logger.warning("No authors found.");
      return;
    }

    BookFetcher googleBooksFetcher = BookFetcherFactory.adapter("google");
    BookFetcher openApiFetcher = BookFetcherFactory.adapter("open");

    for (String author : authors) {
      List<Book> googleBooks = googleBooksFetcher.searchByAuthor(author);
      List<Book> openApiBooks = openApiFetcher.searchByAuthor(author);
      googleBooks.forEach(bookService::save);
      openApiBooks.forEach(bookService::save);
    }
  }
}