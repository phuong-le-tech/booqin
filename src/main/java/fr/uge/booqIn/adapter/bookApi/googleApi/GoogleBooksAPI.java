package fr.uge.booqIn.adapter.bookApi.googleApi;

import fr.uge.booqIn.adapter.bookApi.AuthorNameParser;
import fr.uge.booqIn.adapter.bookApi.BookFetcher;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.singleton.WebClientSingleton;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class GoogleBooksAPI implements BookFetcher {
    private static final String API_URL = "https://www.googleapis.com/books/v1/volumes?q=";
    private static final String SUFFIX_URL = "&langRestrict=en";
    private final WebClient webClient = WebClientSingleton.getInstance();

    private final static Logger logger = Logger.getLogger(GoogleBooksAPI.class.toString());

    private static final List<SimpleDateFormat> DATE_FORMATS = Arrays.asList(
            new SimpleDateFormat("yyyy-MM-dd"),
            new SimpleDateFormat("yyyy-MM"),
            new SimpleDateFormat("yyyy")
    );

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
        logger.info("URL: " + url);

        try {
            GoogleBooksResponse responseMono = webClient.get()
                    .uri(url + SUFFIX_URL)
                    .retrieve()
                    .bodyToMono(GoogleBooksResponse.class).block();

            if (responseMono == null || responseMono.items() == null) {
                logger.info("Empty responseMono.");
                return List.of();
            }

            return responseMono.items().stream()
                    .map(this::convertToBook)
                    .toList();

        } catch (Exception e) {
            logger.warning("Error while fetching books: " + e.getMessage());
            return List.of();
        }
    }


    private Book convertToBook(BookItem item) {
        var volumeInfo = item.volumeInfo();
        Set<Author> authors = new HashSet<>();
        if (volumeInfo.authors() != null && !volumeInfo.authors().isEmpty()) {
            for (String authorName : volumeInfo.authors()) {
                // Vérifier si l'entrée contient plusieurs auteurs séparés par des virgules
                if (authorName.indexOf(",") != authorName.lastIndexOf(",")) {
                    authors.addAll(AuthorNameParser.parseMultipleAuthors(authorName));
                } else {
                    authors.add(AuthorNameParser.parseAuthorName(authorName));
                }
            }
        }
        String description = volumeInfo.description() != null ? volumeInfo.description() : "";
        Date publicationDate = parsePublicationDate(volumeInfo.publishedDate());

        String isbn10 = getIdentifier(volumeInfo.industryIdentifiers(),"ISBN_10");
        String isbn13 = getIdentifier(volumeInfo.industryIdentifiers(),"ISBN_13");

        Set<String> isbns = new HashSet<>();
        if (isbn10 != null) isbns.add(isbn10);
        if (isbn13 != null) isbns.add(isbn13);

        Set<String> categories = new HashSet<>();
        if (volumeInfo.categories() != null && !volumeInfo.categories().isEmpty()) {
            categories.addAll(volumeInfo.categories());
        }

        Book.Builder bookBuilder = new Book.Builder()
                .title(volumeInfo.title())
                .authors(authors)
                .description(description)
                .publicationDate(publicationDate)
                .isbns(isbns)
                .categories(categories);

        if (volumeInfo.imageLinks() != null && volumeInfo.imageLinks().thumbnail() != null) {
            bookBuilder.coverImagePath(volumeInfo.imageLinks().thumbnail());
            bookBuilder.isExternalImage(true);
        }

        return bookBuilder.build();
    }

    private String getIdentifier(List<IndustryIdentifier> industryIdentifiers, String type){
        return Optional.ofNullable(industryIdentifiers)
                .flatMap(identifiers -> identifiers.stream()
                        .filter(identifier -> type.equals(identifier.type()))
                        .map(IndustryIdentifier::identifier)
                        .findFirst())
                .orElse(null);
    }

    private Date parsePublicationDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        for (SimpleDateFormat format : DATE_FORMATS ) {
            try {
                format.setLenient(true);
                return format.parse(dateString);
            } catch (ParseException e) {
                // Essayer le format suivant
            }
        }

        logger.warning("Failed to parse date: " + dateString);
        return null;
    }
}