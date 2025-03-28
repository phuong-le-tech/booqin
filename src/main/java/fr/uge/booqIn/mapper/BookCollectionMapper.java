package fr.uge.booqIn.mapper;

import fr.uge.booqIn.dto.bookCollection.BookCollectionIndex;
import fr.uge.booqIn.dto.bookCollection.BookCollectionShow;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.entity.*;
import fr.uge.booqIn.model.BookCollection;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between BookCollection entities and DTOs.
 * Handles the polymorphic nature of book collections.
 */
@Component
public class BookCollectionMapper {
  private final BookMapper bookMapper = new BookMapper();


  /**
   * Converts a book collection entity to an index DTO.
   * The index DTO contains basic information for listing collections.
   *
   * @param collection The collection entity to convert
   * @return A BookCollectionIndex DTO
   */
  public BookCollectionIndex toBookCollectionIndex(BookCollection collection) {
    return new BookCollectionIndex(
            collection.getId(),
            collection.getTitle(),
            collection.getDescription(),
            collection.getOwner().getFullName(),
            collection.getOwner().getId(),
            collection.getBooks().size(),
            collection.isPublic(),
            collection.getCollectionType()
    );
  }

  /**
   * Converts a book collection entity to a show DTO.
   * The show DTO contains detailed information including all books.
   *
   * @param collection The collection entity to convert
   * @return A BookCollectionShow DTO
   */
  public BookCollectionShow toBookCollectionShow(BookCollection collection) {

    UserIndex user = new UserIndex(
            collection.getOwner().getId(),
            collection.getOwner().getFirstName(),
            collection.getOwner().getLastName(),
            collection.getOwner().getEmail(),
            collection.getOwner().getAddress(),
            collection.getOwner().getRole()
    );
    return new BookCollectionShow(
            collection.getId(),
            collection.getTitle(),
            collection.getDescription(),
            collection.isPublic(),
            collection.getBooks().stream().map(bookMapper::toBookIndex).toList(),
            user,
            collection.getCollectionType(),
            collection.getCreatedAt(),
            collection.getUpdatedAt()
    );
  }
}
