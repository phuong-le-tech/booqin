package fr.uge.booqIn.mapper;

import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.bookCollection.BookCollectionIndex;
import fr.uge.booqIn.dto.bookCollection.BookCollectionShow;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class BookCollectionMapper {
  private final BookMapper bookMapper = new BookMapper();
  private final UserMapper userMapper = new UserMapper(this);

//  public BookCollectionMapper(UserMapper userMapper) {
//    this.userMapper = userMapper;
//  }


  /**
   * Maps BookCollection entity to BookCollectionIndex DTO
   */
  public BookCollectionIndex toBookCollectionIndex(BookCollection bookCollection) {
    if (bookCollection == null) {
      return null;
    }

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

    Set<BookIndex> bookIndexes = bookCollection.getBooks().stream()
            .map(bookMapper::toBookIndex)
            .collect(Collectors.toSet());
    User owner = bookCollection.getOwner();

    UserIndex userIndex = new UserIndex(
            owner.getId(),
            owner.getFirstName(),
            owner.getLastName(),
            owner.getEmail(),
            owner.getAddress(),
            owner.getRole()
    );

    return new BookCollectionShow(
            bookCollection.getId(),
            bookCollection.getTitle(),
            bookCollection.getDescription(),
            bookCollection.isPublic(),
            bookIndexes,
            //owner,
            userIndex,
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

    System.out.println("ID IN BOOK COLLECTION CONVERT SHOW TO ENTITY: " + bookCollectionShow.id());
    return new BookCollection.Builder()
            .id(bookCollectionShow.id())
            .title(bookCollectionShow.title())
            .description(bookCollectionShow.description())
            .isPublic(bookCollectionShow.isPublic())
            .books(books.stream().toList())
            .owner(userMapper.userIndexToUser(bookCollectionShow.user()))
            //.owner(bookCollectionShow.user())
            .createdAt(bookCollectionShow.createdAt())
            .updatedAt(bookCollectionShow.updatedAt())
            .build();
  }
}
