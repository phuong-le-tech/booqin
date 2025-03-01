package fr.uge.booqIn.mapper;

import fr.uge.booqIn.dto.author.AuthorDto;
import fr.uge.booqIn.dto.book.BookDto;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BookMapper {

    public BookDto toDto(Book book) {
        if (book == null) {
            return null;
        }

        return new BookDto(
            book.getId(),
            book.getIsbns(),
            book.getTitle(),
            book.getAuthors() != null ? book.getAuthors().stream()
                .map(this::toAuthorDto)
                .collect(Collectors.toSet()) : new HashSet<>(),
            book.getDescription(),
            book.getPublicationDate(),
            book.getCreatedAt(),
            book.getUpdatedAt()
        );
    }
    

    public List<BookDto> toDtoList(List<Book> books) {
        if (books == null) {
            return List.of();
        }
        
        return books.stream()
                .map(this::toDto)
                .toList();
    }
    

    public AuthorDto toAuthorDto(Author author) {
        if (author == null) {
            return null;
        }
        return new AuthorDto(author.getId(), author.getFirstName(), author.getLastName());
    }

    public Book toEntity(BookDto BookDto) {
        if (BookDto == null) {
            return null;
        }
        
        Book.Builder builder = new Book.Builder()
                .id(BookDto.id())
                .title(BookDto.title())
                .description(BookDto.description())
                .publicationDate(BookDto.publicationDate());
        
        if (BookDto.isbns() != null) {
            builder.isbns(BookDto.isbns().stream().toList());
        }
        
        return builder.build();
    }
}