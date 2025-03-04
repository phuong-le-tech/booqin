package fr.uge.booqIn.Adapter.BookAPIs.openlibrary;

import fr.uge.booqIn.Adapter.BookAPIs.AuthorNameParser;
import fr.uge.booqIn.Adapter.BookAPIs.BookFetcher;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.singleton.WebClientSingleton;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class OpenAPI implements BookFetcher {
    private static final String API_URL = "https://openlibrary.org/search.json?";
    private static final String FIELDS = "&fields=title,author_name,isbn,first_publish_year,description,subject_facet&language=eng";
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(OpenAPI.class);
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
        return getBookList(url);
    }

    public List<Book> searchPaginatedBooksByAuthor(String author, int page, int pageSize){
        String paginatedurl = String.format("%sauthor=%s%s&page=%d&limit=%d",
                API_URL, URLEncoder.encode(author, StandardCharsets.UTF_8), FIELDS, page, pageSize);
        logger.info("Getting info of "+author+" with url\n\r"+paginatedurl);
        return getBookList(paginatedurl);
    }

    private List<Book> getBookList(String url) {
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
            for (String authorName : doc.author_name()) {
                // Vérifier si l'entrée contient plusieurs auteurs séparés par des virgules
                if (authorName.indexOf(",") != authorName.lastIndexOf(",")) {
                    authors.addAll(AuthorNameParser.parseMultipleAuthors(authorName));
                } else {
                    authors.add(AuthorNameParser.parseAuthorName(authorName));
                }
            }
        }

        String description = doc.description() != null ? doc.description() : "";
        Date publicationDate = convertYearToDate(doc.first_publish_year());

        Set<String> categories = new HashSet<>();
        if (doc.subject_facet() != null && !doc.subject_facet().isEmpty()) {
            categories.addAll(doc.subject_facet());
        }

        return new Book.Builder()
                .title(title)
                .authors(authors)
                .description(description)
                .publicationDate(publicationDate)
                .isbns(new HashSet<>(doc.isbn()))
                .categories(categories)
                .build();
    }

    private Date convertYearToDate(Integer year) {
        if (year == null) {
            return null;
        }

        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        yearFormat.setLenient(true);

        try {
            // Convertir l'année en chaîne et la parser en date
            return yearFormat.parse(year.toString());
        } catch (ParseException e) {
            logger.warning("Failed to parse publication year: " + year);
            return null;
        }
    }
}