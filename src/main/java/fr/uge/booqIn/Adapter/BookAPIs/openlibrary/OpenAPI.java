package fr.uge.booqIn.Adapter.BookAPIs.openlibrary;

import fr.uge.booqIn.Adapter.BookAPIs.BookFetcher;
import fr.uge.booqIn.Adapter.BookAPIs.GoogleAPI.IndustryIdentifier;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.singleton.WebClientSingleton;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;

public class OpenAPI implements BookFetcher {
    private static final String API_URL = "https://openlibrary.org/search.json?";
    private static final String FIELDS = "&fields=title,author_name,isbn,first_publish_year,description&language=eng";
    private final WebClient webClient = WebClientSingleton.getInstance();

    @Override
    public List<Book> searchByTitle(String title) {
        return searchBooks("title=" + splitAndAdd(title));
    }

    @Override
    public List<Book> searchByAuthor(String author) {
        return searchBooks("author=" + splitAndAdd(author));
    }

    @Override
    public List<Book> searchByIsbn(String isbn) {
        return searchBooks("isbn=" + isbn);
    }

    @Override
    public List<Book> searchByPublisher(String publisher) {
        return searchBooks("q=publisher:"+publisher);
    }

    private List<Book> searchBooks(String query) {
        String url = API_URL + query + FIELDS;
        OpenLibraryResponse response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(OpenLibraryResponse.class)
                .block();

        if (response == null || response.docs() == null) {
            return List.of();
        }

        return response.docs().stream()
                .map(this::convertToBook)
                .toList();
    }

    private Book convertToBook(Doc doc) {
        String title = doc.title() != null ? doc.title() : "No Title";
        String author = doc.author_name() != null && !doc.author_name().isEmpty()
                ? String.join(", ", doc.author_name())
                : "Unknown Author";
        List<IndustryIdentifier> isbn = doc.isbn() != null && !doc.isbn().isEmpty()
                ? doc.isbn().stream().map(is -> new IndustryIdentifier("ISBN_"+is.length(),is)).toList()
                : List.of();
        String description = doc.description() != null
                ? doc.description()
                : "";
        String publicationDate = doc.first_publish_year() != null
                ? String.valueOf(doc.first_publish_year())
                : "";

        return new Book(title, author, isbn, description, publicationDate);
    }

    private String splitAndAdd(String s) {
        return String.join("+", s.split(" "));
    }
}