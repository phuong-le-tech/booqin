package fr.uge.booqIn.mapper;

import fr.uge.booqIn.dto.author.AuthorDto;
import fr.uge.booqIn.dto.book.BookDto;
import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BookMapper {

    /**
     * Maps Book entity to BookIndex DTO
     */
    public BookIndex toBookIndex(Book book) {
        if (book == null) {
            return null;
        }

        Set<String> authorNames = book.getAuthors().stream()
                .map(author -> author.getFirstName() + " " + author.getLastName())
                .collect(Collectors.toSet());

        return new BookIndex(
                book.getId(),
                book.getTitle(),
                authorNames,
                book.getPublicationDate()
        );
    }

    /**
     * Maps Book entity to BookShow DTO
     */
    public BookShow toBookShow(Book book) {
        if (book == null) {
            return null;
        }
        System.out.println(book.getCollections());
        return new BookShow(
                book.getId(),
                book.getIsbns(),
                book.getTitle(),
                book.getAuthors() != null ? book.getAuthors().stream()
                        .map(this::toAuthorDto)
                        .collect(Collectors.toSet()) : new HashSet<>(),
                book.getDescription(),
                book.getPublicationDate(),
                book.getCollections(),
                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }

    public AuthorDto toAuthorDto(Author author) {
        if (author == null) {
            return null;
        }
        return new AuthorDto(author.getId(), author.getFirstName(), author.getLastName());
    }
}