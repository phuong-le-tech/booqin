package fr.uge.booqIn.Adapter.BookAPIs.OpenAPI;

import fr.uge.booqIn.Adapter.BookAPIs.BookFetcher;
import fr.uge.booqIn.Adapter.BookAPIs.GoogleAPI.BookItem;
import fr.uge.booqIn.Adapter.BookAPIs.GoogleAPI.VolumeInfo;
import fr.uge.booqIn.entity.Book;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Optional;

public class OpenAPI implements BookFetcher {
  private static final String API_URL = "https://openlibrary.org/search.json?";
  private final WebClient webClient = WebClient.create();

  @Override
  public List<Book> searchByTitle(String title) {
    return searchBooks("title="+ splitAndAdd(title));
  }

  @Override
  public List<Book> searchByAuthor(String author) {
    return searchBooks("author="+splitAndAdd(author));
  }

  @Override
  public List<Book> searchByIsbn(String isbn) {
    return searchBooks("isbn="+isbn);
  }

  @Override
  public List<Book> searchByPublisher(String publisher) {
    return List.of();
  }

  @Override
  public List<Book> searchByPublishedDate(String publishedDate) {
    return searchBooks("q=publish_year:"+publishedDate);
  }

  private List<Book> searchBooks(String query) {
    String url = API_URL + query;
    Optional<OpenApiResponse> responseMono = webClient.get()
        .uri(url)
        .retrieve()
        .bodyToMono(OpenApiResponse.class).blockOptional();

    return responseMono.
        map(openApiResponse -> openApiResponse.items().stream().map(this::convertToBook).toList())
        .orElseGet(List::of);
  }

  private Book convertToBook(BookItem item) {
    VolumeInfo volumeInfo = item.volumeInfo;
    return new Book(
        volumeInfo.title,
        volumeInfo.authors != null && !volumeInfo.authors.isEmpty() ? String.valueOf(volumeInfo.authors) : "",
        "ISBN TODO",
        volumeInfo.description != null ? volumeInfo.description : "",
        volumeInfo.publishedDate != null ? volumeInfo.publishedDate : null
    );
  }
  private String splitAndAdd(String s){
    return String.join("+", s.split(" "));
  }
}
