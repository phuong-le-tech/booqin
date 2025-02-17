package fr.uge.booqIn.Adapter.BookAPIs.GoogleAPI;

import fr.uge.booqIn.Adapter.BookAPIs.BookFetcher;
import fr.uge.booqIn.entity.Book;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class GoogleBooksAPI implements BookFetcher {
    private static final String API_URL = "https://www.googleapis.com/books/v1/volumes?q=";
    private final WebClient webClient = WebClient.create();

    @Override
    public List<Book> searchByTitle(String title) {
        return searchBooks("intitle:" + title);
    }

    @Override
    public List<Book> searchByAuthor(String author) {
        return searchBooks("inauthor:" + author);
    }

    @Override
    public List<Book> searchByIsbn(String isbn) {
        return searchBooks("isbn:" + isbn);
    }

    @Override
    public List<Book> searchByPublisher(String publisher) {
        return searchBooks("inpublisher:" + publisher);
    }

    @Override
    public List<Book> searchByPublishedDate(String publishedDate) {
        return searchBooks("inpublisher:" + publishedDate);
    }

    private List<Book> searchBooks(String query) {
        String url = API_URL + query;
        System.out.println("URL: " + url);
        Mono<GoogleBooksResponse> responseMono = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(GoogleBooksResponse.class);

        GoogleBooksResponse response = responseMono.block();
        return response.items.stream()
                .map(this::convertToBook)
                .collect(Collectors.toList());
    }

    private Book convertToBook(BookItem item) {
        VolumeInfo volumeInfo = item.volumeInfo;
        System.out.println("CONVERTING BOOK: " + volumeInfo);
        var book = new Book(
                volumeInfo.title,
                volumeInfo.authors != null && !volumeInfo.authors.isEmpty() ? String.valueOf(volumeInfo.authors) : "",
                "ISBN TODO",
                volumeInfo.description != null ? volumeInfo.description : "",
                volumeInfo.publishedDate != null ? volumeInfo.publishedDate : null
        );
        System.out.println("CONVERTED BOOK: " + book);
        return book;
    }
}