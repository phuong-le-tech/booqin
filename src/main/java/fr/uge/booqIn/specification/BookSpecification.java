package fr.uge.booqIn.specification;

import fr.uge.booqIn.entity.Book;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.SetJoin;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;

public class BookSpecification {

  /**
   * Specification to filter books by title (case-insensitive, contains)
   */
  public static Specification<Book> hasTitle(String title) {
    return (root, query, criteriaBuilder) ->
            criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")),
                    "%" + title.toLowerCase() + "%"
            );
  }

  /**
   * Specification to filter books by author's first or last name
   */
  public static Specification<Book> hasAuthor(String authorName) {
    return (root, query, criteriaBuilder) -> {
      Join<Book, Collection<String>> authorsJoin = root.join("authors", JoinType.INNER);

      return criteriaBuilder.or(
              criteriaBuilder.like(
                      criteriaBuilder.lower(authorsJoin.get("firstName")),
                      "%" + authorName.toLowerCase() + "%"
              ),
              criteriaBuilder.like(
                      criteriaBuilder.lower(authorsJoin.get("lastName")),
                      "%" + authorName.toLowerCase() + "%"
              )
      );
    };
  }

  /**
   * Specification to filter books by ISBN
   */
  public static Specification<Book> hasIsbn(String isbn) {
    return (root, query, criteriaBuilder) -> {
      SetJoin<Book, String> isbnsJoin = root.joinSet("isbns", JoinType.INNER);
      return criteriaBuilder.equal(isbnsJoin, isbn);
    };
  }

  /**
   * Specification to filter books by publication date
   */
  public static Specification<Book> hasPublicationDate(String publicationDate) {
    return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("publicationDate"), publicationDate);
  }
}