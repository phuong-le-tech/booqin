package fr.uge.booqIn.adapter.bookApi;

import fr.uge.booqIn.adapter.bookApi.googleApi.GoogleBooksAPI;
import fr.uge.booqIn.entity.Book;

import java.util.List;

public class GoogleBooksAdapter implements BookFetcher {
    private final GoogleBooksAPI googleBooksAPI = new GoogleBooksAPI();

    @Override
    public List<Book> searchByTitle(String title) {
        return googleBooksAPI.searchByTitle(title);
    }

    @Override
    public List<Book> searchByAuthor(String author) {
        return googleBooksAPI.searchByAuthor(author);
    }

    @Override
    public List<Book> searchByIsbn(String isbn) {
        return googleBooksAPI.searchByIsbn(isbn);
    }
}
