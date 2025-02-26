package fr.uge.booqIn.mapper;

import fr.uge.booqIn.dto.author.AuthorDto;
import fr.uge.booqIn.dto.book.BookDto;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BookMapper {

    public BookDto toDto(Book book) {
        if (book == null) {
            return null;
        }
        
        BookDto dto = new BookDto();
        dto.setId(book.getId());
        dto.setIsbns(book.getIsbns());
        dto.setTitle(book.getTitle());
        dto.setDescription(book.getDescription());
        dto.setPublicationDate(book.getPublicationDate());
        dto.setCreatedAt(book.getCreatedAt());
        dto.setUpdatedAt(book.getUpdatedAt());
        
        if (book.getAuthors() != null) {
            Set<AuthorDto> AuthorDtos = book.getAuthors().stream()
                    .map(this::toAuthorDto)
                    .collect(Collectors.toSet());
            dto.setAuthors(AuthorDtos);
        }
        return dto;
    }
    

    public List<BookDto> toDtoList(List<Book> books) {
        if (books == null) {
            return List.of();
        }
        
        return books.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    

    public AuthorDto toAuthorDto(Author author) {
        if (author == null) {
            return null;
        }
        
        AuthorDto dto = new AuthorDto();
        dto.setId(author.getId());
        dto.setFirstName(author.getFirstName());
        dto.setLastName(author.getLastName());
        
        return dto;
    }

    public Book toEntity(BookDto BookDto) {
        if (BookDto == null) {
            return null;
        }
        
        Book.Builder builder = new Book.Builder()
                .id(BookDto.getId())
                .title(BookDto.getTitle())
                .description(BookDto.getDescription())
                .publicationDate(BookDto.getPublicationDate());
        
        if (BookDto.getIsbns() != null) {
            builder.isbns(BookDto.getIsbns().stream().toList());
        }
        
        return builder.build();
    }
}