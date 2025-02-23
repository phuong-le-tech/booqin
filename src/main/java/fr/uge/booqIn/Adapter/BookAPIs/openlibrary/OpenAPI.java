package fr.uge.booqIn.Adapter.BookAPIs.openlibrary;

import fr.uge.booqIn.Adapter.BookAPIs.BookFetcher;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.singleton.WebClientSingleton;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class OpenAPI implements BookFetcher {
    private static final String API_URL = "https://openlibrary.org/search.json?";
    private static final String FIELDS = "&fields=title,author_name,isbn,first_publish_year,description&language=eng";
    private final WebClient webClient = WebClientSingleton.getInstance();

    private final static Logger logger = Logger.getLogger(OpenAPI.class.toString());

    @Override
    public List<Book> searchByTitle(String title) {
        return searchBooks("title=" + URLEncoder.encode(title, StandardCharsets.UTF_8));
    }

    @Override
    public List<Book> searchByAuthor(String author) {
        return searchBooks("author=" + URLEncoder.encode(author, StandardCharsets.UTF_8));
    }

    @Override
    public List<Book> searchByIsbn(String isbn) {
        return searchBooks("isbn=" + isbn);
    }


    private List<Book> searchBooks(String query) {
        String url = API_URL + query + FIELDS;
        logger.info(url);
        OpenLibraryResponse response = webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(OpenLibraryResponse.class)
                .block();

        if (response == null || response.docs() == null) {
            return List.of();
        }

        return response.docs().stream()
                .filter(doc -> doc.isbn() != null && !doc.isbn().isEmpty())
                .map(this::convertToBook)
                .toList();
    }

    private Book convertToBook(Doc doc) {
        String title = doc.title() != null ? doc.title() : "No Title";
        Set<Author> authors = new HashSet<>();
        if (doc.author_name() != null && !doc.author_name().isEmpty()) {
            authors = doc.author_name().stream()
                .map(name -> {
                    String[] parts = name.split(" ", 2);
                    return new Author(parts[0], parts.length > 1 ? parts[1] : "");
                })
                .collect(Collectors.toSet());
        }
        String description = doc.description() != null ? doc.description() : "";
        String publicationDate = doc.first_publish_year() != null ? String.valueOf(doc.first_publish_year()) : "";
        return new Book.Builder()
            .title(title)
            .authors(authors)
            .description(description)
            .publicationDate(publicationDate)
            .isbns(doc.isbn())
            .build();
    }
}