package fr.uge.booqIn.Adapter.BookAPIs.GoogleAPI;

import fr.uge.booqIn.Adapter.BookAPIs.BookFetcher;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.singleton.WebClientSingleton;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
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
        return searchBooks("inauthor:" + URLEncoder.encode(author, StandardCharsets.UTF_8));
    }

    @Override
    public List<Book> searchByIsbn(String isbn) {
        return searchBooks("isbn:" + isbn);
    }


    private List<Book> searchBooks(String query) {
        String url = API_URL + query;
        System.out.println("URL: " + url);

        try {
            GoogleBooksResponse responseMono = webClient.get()
                    .uri(url + SUFFIX_URL)
                    .retrieve()
                    .bodyToMono(GoogleBooksResponse.class).block();

            if (responseMono == null || responseMono.items() == null) {
                System.out.println("Empty responseMono.");
                return List.of();
            }

            return responseMono.items().stream()
                    .map(this::convertToBook)
                    .toList();

        } catch (Exception e) {
            System.out.println("Error while fetching books: " + e.getMessage());
            return List.of();
        }
    }


    private Book convertToBook(BookItem item) {
        var volumeInfo = item.volumeInfo();
        Set<Author> authors = volumeInfo.authors() != null && !volumeInfo.authors().isEmpty()
                ? volumeInfo.authors().stream().map(authorName -> new Author(authorName, "")).collect(Collectors.toSet())
                : Set.of();
        String description = volumeInfo.description() != null ? volumeInfo.description() : "";
        String publicationDate = volumeInfo.publishedDate();

        String isbn10 = getIdentifier(volumeInfo.industryIdentifiers(),"ISBN_10");
        String isbn13 = getIdentifier(volumeInfo.industryIdentifiers(),"ISBN_13");

        var b = new Book(
                volumeInfo.title(),
                authors,
                description,
                publicationDate
        );
        b.addIsbn(isbn10);
        b.addIsbn(isbn13);
        return b;
    }

    private String getIdentifier(List<IndustryIdentifier> industryIdentifiers, String type){
        return Optional.ofNullable(industryIdentifiers)
                .flatMap(identifiers -> identifiers.stream()
                        .filter(identifier -> type.equals(identifier.type()))
                        .map(IndustryIdentifier::identifier)
                        .findFirst())
                .orElse(null);
    }
}
