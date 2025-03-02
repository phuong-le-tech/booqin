package fr.uge.booqIn.Adapter.BookAPIs.GoogleAPI;

import fr.uge.booqIn.Adapter.BookAPIs.BookFetcher;
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
import java.util.stream.Collectors;

public class GoogleBooksAPI implements BookFetcher {
    private static final String API_URL = "https://www.googleapis.com/books/v1/volumes?q=";
    private static final String SUFFIX_URL = "&langRestrict=en";
    private final WebClient webClient = WebClientSingleton.getInstance();
    private static final Logger logger = Logger.getLogger(GoogleBooksAPI.class.getName());

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
        logger.info("Searching info for :%s".formatted(author));
        return searchBooks("inauthor:" + URLEncoder.encode(author, StandardCharsets.UTF_8));
    }

    @Override
    public List<Book> searchByIsbn(String isbn) {
        return searchBooks("isbn:" + isbn);
    }


    private List<Book> searchBooks(String query) {
        String url = API_URL + query;
//        System.out.println("URL: " + url);

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
            ? volumeInfo.authors().stream()
            .map(this::parseAuthor)
            .collect(Collectors.toSet())
            : Set.of();
        String description = volumeInfo.description() != null ? volumeInfo.description() : "";
        Date publicationDate = parsePublicationDate(volumeInfo.publishedDate());

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

    private Author parseAuthor(String authorName) {
        String[] parts = authorName.split(" ", 2);
        String firstName = parts[0];
        String lastName = parts.length > 1 ? parts[1] : "";
        if (firstName.length() > 255) {
            firstName = firstName.substring(0, 255);
        }
        if (lastName.length() > 255) {
            lastName = lastName.substring(0, 255);
        }
        return new Author(firstName, lastName);
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

        System.out.println("Failed to parse date: " + dateString);
        return null;
    }
}
