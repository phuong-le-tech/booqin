package fr.uge.booqIn.Adapter.BookAPIs.GoogleAPI;

import fr.uge.booqIn.Adapter.BookAPIs.BookFetcher;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.singleton.WebClientSingleton;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
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
        var volumeInfo = item.volumeInfo;
        Set<Author> authors = volumeInfo.authors() != null && !volumeInfo.authors().isEmpty()
                ? volumeInfo.authors().stream().map(authorName -> new Author(authorName, "")).collect(Collectors.toSet())
                : Set.of();
        String description = volumeInfo.description() != null ? volumeInfo.description() : "";
        String publicationDate = volumeInfo.publishedDate();

        String isbn10 = null;
        String isbn13 = null;
        if (volumeInfo.industryIdentifiers() != null) {
            for (var identifier : volumeInfo.industryIdentifiers()) {
                if ("ISBN_10".equals(identifier.type())) {
                    isbn10 = identifier.identifier();
                } else if ("ISBN_13".equals(identifier.type())) {
                    isbn13 = identifier.identifier();
                }
            }
        }
        return new Book(
                isbn10,
                isbn13,
                volumeInfo.title(),
                authors,
                description,
                publicationDate
        );
    }
}
