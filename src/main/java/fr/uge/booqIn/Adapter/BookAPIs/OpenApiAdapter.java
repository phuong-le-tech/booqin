package fr.uge.booqIn.Adapter.BookAPIs;

import fr.uge.booqIn.Adapter.BookAPIs.openlibrary.OpenAPI;
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
    return List.of();
  }

  @Override
  public List<Book> searchByIsbn(String isbn) {
    return List.of();
  }

  @Override
  public List<Book> searchByPublisher(String publisher) {
    return List.of();
  }
}
