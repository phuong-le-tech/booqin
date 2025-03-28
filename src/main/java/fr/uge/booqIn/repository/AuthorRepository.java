package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    /**
     * Find an author by their first and last name.
     *
     * @param firstName The first name of the author
     * @param lastName The last name of the author
     * @return Optional containing the author if found
     */
    Optional<Author> findByFirstNameAndLastName(
            @NonNull String firstName,
            @NonNull String lastName
    );
}