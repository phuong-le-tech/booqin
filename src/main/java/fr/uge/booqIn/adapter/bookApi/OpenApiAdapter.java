package fr.uge.booqIn.adapter.bookApi;

import fr.uge.booqIn.adapter.bookApi.openlibrary.OpenAPI;
import fr.uge.booqIn.entity.Book;

import java.util.List;

public class OpenApiAdapter implements BookFetcher {
  private final OpenAPI openAPI  = new OpenAPI();
  @Override
  public List<Book> searchByTitle(String title) {
    return openAPI.searchByTitle(title);
  }

  @Override
  public List<Book> searchByAuthor(String author) {
    return openAPI.searchByAuthor(author);
  }

  @Override
  public List<Book> searchByIsbn(String isbn) {
    return openAPI.searchByIsbn(isbn);
  }

  @Override
  public List<Book> searchBookByAuthorWithPagination(String author, int page, int pageSize){
    return openAPI.searchPaginatedBooksByAuthor(author,page,pageSize);
  }
}
