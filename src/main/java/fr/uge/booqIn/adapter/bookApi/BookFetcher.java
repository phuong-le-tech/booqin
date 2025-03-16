package fr.uge.booqIn.adapter.bookApi;

import fr.uge.booqIn.entity.Book;

import java.util.List;

public interface BookFetcher {
    List<Book> searchByTitle(String title);
    List<Book> searchByAuthor(String author);
    List<Book> searchByIsbn(String isbn);
    default List<Book> searchBookByAuthorWithPagination(String author, int page, int pageSize){
        return searchByAuthor(author);
    }
}
