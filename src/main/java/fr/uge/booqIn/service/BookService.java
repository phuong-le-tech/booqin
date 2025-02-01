package fr.uge.booqIn.service;

import fr.uge.booqIn.Adapter.BookAPIs.BookFetcher;
import fr.uge.booqIn.Adapter.BookAPIs.BookFetcherFactory;
import fr.uge.booqIn.entity.Book;

import java.util.List;
import java.util.Objects;

public class BookService implements BookFetcher {
    private final BookFetcher bookFetcher;

    public BookService(String api) {
        Objects.requireNonNull(api);
        this.bookFetcher = BookFetcherFactory.adapter(api);
    }

    public List<Book> searchByTitle(String title) {
        return bookFetcher.searchByTitle(title);
    }

    public List<Book> searchByAuthor(String author) {
        return bookFetcher.searchByAuthor(author);
    }

    public List<Book> searchByIsbn(String isbn) {
        return bookFetcher.searchByIsbn(isbn);
    }

    public List<Book> searchByPublisher(String publisher) {
        return bookFetcher.searchByPublisher(publisher);
    }

    public List<Book> searchByPublishedDate(String publishedDate) {
        return bookFetcher.searchByPublishedDate(publishedDate);
    }
}
