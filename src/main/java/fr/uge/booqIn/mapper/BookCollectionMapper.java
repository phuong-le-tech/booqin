package fr.uge.booqIn.mapper;

import fr.uge.booqIn.dto.bookCollection.BookCollectionIndex;
import fr.uge.booqIn.dto.bookCollection.BookCollectionShow;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.entity.AbstractBookCollection;
import fr.uge.booqIn.entity.GlobalBookCollection;
import fr.uge.booqIn.entity.NormalBookCollection;
import fr.uge.booqIn.entity.SmartBookCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between BookCollection entities and DTOs.
 * Handles the polymorphic nature of book collections.
 */
@Component
public class BookCollectionMapper {
  private final BookMapper bookMapper;

  @Autowired
  public BookCollectionMapper(BookMapper bookMapper) {
    this.bookMapper = bookMapper;
  }

  /**
   * Converts a book collection entity to an index DTO.
   * The index DTO contains basic information for listing collections.
   *
   * @param collection The collection entity to convert
   * @return A BookCollectionIndex DTO
   */
  public BookCollectionIndex toBookCollectionIndex(AbstractBookCollection collection) {
    return new BookCollectionIndex(
            collection.getId(),
            collection.getTitle(),
            collection.getDescription(),
            collection.getOwner().getFullName(),
            collection.getOwner().getId(),
            collection.getBooks().size(),
            collection.isPublic()
    );
  }

  /**
   * Converts a book collection entity to a show DTO.
   * The show DTO contains detailed information including all books.
   *
   * @param collection The collection entity to convert
   * @return A BookCollectionShow DTO
   */
  public BookCollectionShow toBookCollectionShow(AbstractBookCollection collection) {

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
            getCollectionType(collection),
            collection.getCreatedAt(),
            collection.getUpdatedAt()
    );
  }

  /**
   * Determines the string representation of the collection type
   * based on the actual type of the collection entity.
   *
   * @param collection The collection entity
   * @return A string representing the collection type
   */
  private String getCollectionType(AbstractBookCollection collection) {
    return switch (collection) {
      case NormalBookCollection normalBookCollection -> "NORMAL";
      case SmartBookCollection smartBookCollection -> "SMART";
      case GlobalBookCollection globalBookCollection -> "GLOBAL";
      case null, default -> throw new IllegalArgumentException("Invalid collection type");
    };
  }
}