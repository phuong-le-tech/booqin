package fr.uge.booqIn.specification;

import fr.uge.booqIn.entity.AbstractBookCollection;
import fr.uge.booqIn.entity.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class BookCollectionSpecification {

  /**
   * Specification to filter book collections by title (case-insensitive, contains)
   */
  public static <T extends AbstractBookCollection> Specification<T> hasTitle(String title) {
    return (root, query, criteriaBuilder) ->
            criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")),
                    "%" + title.toLowerCase() + "%"
            );
  }

  public static <T extends AbstractBookCollection> Specification<T>  hasDescription(String description) {
    return (root, query, criteriaBuilder) ->
            criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),
                    "%" + description.toLowerCase() + "%"
            );
  }

  /**
   * Specification to filter book collections by owner's first or last name
   */
  public static <T extends AbstractBookCollection> Specification<T>  hasOwner(String ownerName) {
    return (root, query, criteriaBuilder) -> {
      Join<AbstractBookCollection, User> ownerJoin = root.join("owner", JoinType.INNER);

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

  public static <T extends AbstractBookCollection> Specification<T>  hasOwner(Long ownerId) {
    return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("owner").get("id"), ownerId);
  }

  /**
   * Specification to filter book collections by whether they are public
   */
  public static <T extends AbstractBookCollection> Specification<T>  isPublic() {
    return (root, query, criteriaBuilder) ->
            criteriaBuilder.isTrue(root.get("isPublic"));
  }

}
