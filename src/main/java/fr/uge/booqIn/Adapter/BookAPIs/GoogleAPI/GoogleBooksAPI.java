package fr.uge.booqIn.Adapter.BookAPIs.GoogleAPI;

import fr.uge.booqIn.Adapter.BookAPIs.BookFetcher;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.singleton.WebClientSingleton;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

public class GoogleBooksAPI implements BookFetcher {
    private static final String API_URL = "https://www.googleapis.com/books/v1/volumes?q=";
    private static final String SUFFIX_URL = "&langRestrict=en";
    private final WebClient webClient = WebClientSingleton.getInstance();

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


    private List<Book> searchBooks(String query) {
        String url = API_URL + query;
        System.out.println("URL: " + url);

        try {
            Mono<GoogleBooksResponse> responseMono = webClient.get()
                    .uri(url + SUFFIX_URL)
                    .retrieve()
                    .bodyToMono(GoogleBooksResponse.class);

            GoogleBooksResponse response = responseMono.blockOptional().orElse(null);

            if (response == null || response.items == null) {
                System.out.println("Empty response.");
                return List.of();
            }

            return response.items.stream()
                    .map(this::convertToBook)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.out.println("Error while fetching books: " + e.getMessage());
            return List.of();
        }
    }


    private Book convertToBook(BookItem item) {
        VolumeInfo volumeInfo = item.volumeInfo;
        System.out.println("CONVERTING BOOK: " + volumeInfo);

        Book book = new Book(
                volumeInfo.title(),
                (volumeInfo.authors() != null && !volumeInfo.authors().isEmpty())
                        ? String.join(", ", volumeInfo.authors())
                        : "",
                volumeInfo.industryIdentifiers(),
                volumeInfo.description() != null ? volumeInfo.description() : "",
                volumeInfo.publishedDate()
        );
        System.out.println("CONVERTED BOOK: " + book);
        return book;
    }
}
