package fr.uge.booqIn.specification;

import fr.uge.booqIn.entity.User;
import org.springframework.data.jpa.domain.Specification;

/**
 * Specification class for User entity to build dynamic queries
 */
public class UserSpecification {

    /**
     * Create a specification to filter users by first name
     * Uses case-insensitive "like" comparison for partial matches
     *
     * @param firstName First name to search for
     * @return Specification for first name filter
     */
    public static Specification<User> hasFirstName(String firstName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("firstName")),
                        "%" + firstName.toLowerCase() + "%"
                );
    }

    /**
     * Create a specification to filter users by last name
     * Uses case-insensitive "like" comparison for partial matches
     *
     * @param lastName Last name to search for
     * @return Specification for last name filter
     */
    public static Specification<User> hasLastName(String lastName) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("lastName")),
                        "%" + lastName.toLowerCase() + "%"
                );
    }

    /**
     * Create a specification to filter users by email
     * Uses case-insensitive "like" comparison for partial matches
     *
     * @param email Email to search for
     * @return Specification for email filter
     */
    public static Specification<User> hasEmail(String email) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),

                        "%" + email.toLowerCase() + "%"
                );
    }

    /**
     * Create a specification to filter users by role
     *
     * @param role Role to filter by
     * @return Specification for role filter
     */
    public static Specification<User> hasRole(String role) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("role"), role);
    }

    /**
     * Create a specification to filter users by enabled status
     *
     * @param enabled Enabled status to filter by
     * @return Specification for enabled status filter
     */
    public static Specification<User> isEnabled(boolean enabled) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("enabled"), enabled);
    }

    /**
     * Create a specification to filter users by city
     *
     * @param city City to search for
     * @return Specification for city filter
     */
    public static Specification<User> hasCity(String city) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("address").get("city")),
                        "%" + city.toLowerCase() + "%"
                );
    }

    /**
     * Create a specification to filter users by country
     *
     * @param country Country to search for
     * @return Specification for country filter
     */
    public static Specification<User> hasCountry(String country) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("address").get("country")),
                        "%" + country.toLowerCase() + "%"
                );
    }

    /**
     * Create a specification to filter users who have books to lend
     *
     * @return Specification for users with books to lend
     */
    public static Specification<User> hasBooksToLend() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isNotEmpty(root.get("booksToLend"));
    }
}