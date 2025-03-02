package fr.uge.booqIn.specification;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class BookCollectionSpecification {

  /**
   * Specification to filter book collections by title (case-insensitive, contains)
   */
  public static Specification<BookCollection> hasTitle(String title) {
    return (root, query, criteriaBuilder) ->
            criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")),
                    "%" + title.toLowerCase() + "%"
            );
  }

  public static Specification<BookCollection> hasDescription(String description) {
    return (root, query, criteriaBuilder) ->
            criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),
                    "%" + description.toLowerCase() + "%"
            );
  }

  /**
   * Specification to filter book collections by owner's first or last name
   */
  public static Specification<BookCollection> hasOwner(String ownerName) {
    return (root, query, criteriaBuilder) -> {
      Join<BookCollection, User> ownerJoin = root.join("owner", JoinType.INNER);

      return criteriaBuilder.or(
              criteriaBuilder.like(
                      criteriaBuilder.lower(ownerJoin.get("firstName")),
                      "%" + ownerName.toLowerCase() + "%"
              ),
              criteriaBuilder.like(
                      criteriaBuilder.lower(ownerJoin.get("lastName")),
                      "%" + ownerName.toLowerCase() + "%"
              )
      );
    };
  }

  public static Specification<BookCollection> hasOwner(Long ownerId) {
    return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("owner").get("id"), ownerId);
  }

  /**
   * Specification to filter book collections by whether they are public
   */
  public static Specification<BookCollection> isPublic() {
    return (root, query, criteriaBuilder) ->
            criteriaBuilder.isTrue(root.get("isPublic"));
  }

  /**
   * Specification to filter book collections by whether they are smart
   */
  public static Specification<BookCollection> isSmart() {
    return (root, query, criteriaBuilder) ->
            criteriaBuilder.isTrue(root.get("isSmart"));
  }

  /**
   * Specification to filter book collections by collection type
   */
  public static Specification<BookCollection> hasCollectionType(BookCollection.CollectionType collectionType) {
    return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("collectionType"), collectionType);
  }

  /**
   * Specification to filter book collections by book title within the collection
   */
  public static Specification<BookCollection> hasBookWithTitle(String bookTitle) {
    return (root, query, criteriaBuilder) -> {
      Join<BookCollection, Book> booksJoin = root.join("books", JoinType.INNER);

      return criteriaBuilder.like(
              criteriaBuilder.lower(booksJoin.get("title")),
              "%" + bookTitle.toLowerCase() + "%"
      );
    };
  }
}
