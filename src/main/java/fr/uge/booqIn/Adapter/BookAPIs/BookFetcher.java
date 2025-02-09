package fr.uge.booqIn.Adapter.BookAPIs;

import fr.uge.booqIn.entity.Book;

import java.util.List;

public interface BookFetcher {
    List<Book> searchByTitle(String title);
    List<Book> searchByAuthor(String author);
    List<Book> searchByIsbn(String isbn);
    List<Book> searchByPublisher(String publisher);
}
