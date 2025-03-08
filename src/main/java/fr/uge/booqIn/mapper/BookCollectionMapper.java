package fr.uge.booqIn.mapper;

import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.bookCollection.BookCollectionIndex;
import fr.uge.booqIn.dto.bookCollection.BookCollectionShow;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BookCollectionMapper {
  private final BookMapper bookMapper = new BookMapper();
  private final UserMapper userMapper = new UserMapper();

  /**
   * Maps BookCollection entity to BookCollectionIndex DTO
   */
  public BookCollectionIndex toBookCollectionIndex(BookCollection bookCollection) {
    if (bookCollection == null) {
      return null;
    }

    Set<String> bookTitles = bookCollection.getBooks().stream()
            .map(Book::getTitle)
            .collect(Collectors.toSet());

    String ownerName = bookCollection.getOwner() != null ?
            bookCollection.getOwner().getFirstName() + " " + bookCollection.getOwner().getLastName() : "Unknown";

    return new BookCollectionIndex(
            bookCollection.getId(),
            bookCollection.getTitle(),
            bookCollection.getDescription(),
            ownerName,
            bookCollection.isPublic()
    );
  }

  /**
   * Maps BookCollection entity to BookCollectionShow DTO
   */
  public BookCollectionShow toBookCollectionShow(BookCollection bookCollection) {
    if (bookCollection == null) {
      return null;
    }

    Set<BookIndex> bookShows = bookCollection.getBooks().stream()
            .map(bookMapper::toBookIndex)
            .collect(Collectors.toSet());
    User owner = bookCollection.getOwner();

    return new BookCollectionShow(
            bookCollection.getId(),
            bookCollection.getTitle(),
            bookCollection.getDescription(),
            bookShows,
            //owner,
            userMapper.toUserShow(owner),
            bookCollection.getCreatedAt(),
            bookCollection.getUpdatedAt()
    );
  }

  public BookCollection bookCollectionShowToBookCollection(BookCollectionShow bookCollectionShow) {
    if (bookCollectionShow == null) {
      return null;
    }
    Set<Book> books = bookCollectionShow.books().stream()
            .map(bookMapper::bookIndexToBook)
            .collect(Collectors.toSet());

    return new BookCollection.Builder()
            .title(bookCollectionShow.name())
            .description(bookCollectionShow.description())
            .books(books.stream().toList())
            .owner(userMapper.userShowToUser(bookCollectionShow.user()))
            //.owner(bookCollectionShow.user())
            .createdAt(bookCollectionShow.createdAt())
            .updatedAt(bookCollectionShow.updatedAt())
            .build();
  }
}
