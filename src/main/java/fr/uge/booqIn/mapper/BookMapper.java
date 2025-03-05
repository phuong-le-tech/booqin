package fr.uge.booqIn.mapper;

import fr.uge.booqIn.dto.author.AuthorDto;
import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import org.springframework.stereotype.Component;

import java.util.HashSet;
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
                book.getIsbns(),
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
        return new BookShow(
                book.getId(),
                book.getIsbns(),
                book.getTitle(),
                book.getAuthors() != null ? book.getAuthors().stream()
                        .map(this::toAuthorDto)
                        .collect(Collectors.toSet()) : new HashSet<AuthorDto>(),
                book.getDescription(),
                book.getPublicationDate(),
                book.getCreatedAt(),
                book.getUpdatedAt(),
                book.getCategories()
        );
    }

    public AuthorDto toAuthorDto(Author author) {
        if (author == null) {
            return null;
        }
        return new AuthorDto(author.getId(), author.getFirstName(), author.getLastName());
    }

    public Author toAuthor(AuthorDto authorDto) {
        if (authorDto == null) {
            return null;
        }
        return new Author(authorDto.firstName(), authorDto.lastName());
    }

    public Book bookIndexToBook(BookIndex bookIndex) {
        if (bookIndex == null) {
            return null;
        }
        Set<Author> authors = bookIndex.authorNames().stream()
                .map(authorName -> {
                    String[] names = authorName.split(" ");
                    return new Author(names[0], names[1]);
                })
                .collect(Collectors.toSet());
        return new Book.Builder().id(bookIndex.id()).isbns(bookIndex.isbns())
                .title(bookIndex.title()).authors(authors).publicationDate(bookIndex.publicationDate())
                .build();
    }
}