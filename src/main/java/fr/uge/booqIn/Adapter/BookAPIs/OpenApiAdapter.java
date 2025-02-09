package fr.uge.booqIn.Adapter.BookAPIs;

import fr.uge.booqIn.Adapter.BookAPIs.OpenAPI.OpenAPI;
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
  public List<Book> searchByPublisher(String publisher) {
    return openAPI.searchByPublisher(publisher);
  }

  @Override
  public List<Book> searchByPublishedDate(String publishedDate) {
    return openAPI.searchByPublishedDate(publishedDate);
  }
}
