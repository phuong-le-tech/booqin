package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.Book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

  @Override
  @Query("SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.isbns WHERE b.id = :id")
  Optional<Book> findById(@NonNull Long id);

  @Query("SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.authors WHERE b.id = :id")
  Optional<Book> findByIdWithAuthors(@NonNull Long id);

  @Query("SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.isbns WHERE b.id = :id")
  Optional<Book> findByIdWithIsbns(@NonNull Long id);

  @Query("SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.categories WHERE b.id = :id")
  Optional<Book> findByIdWithCategories(@NonNull Long id);

  @Query("SELECT b FROM Book b JOIN FETCH b.isbns i WHERE i = :isbn")
  Optional<Book> findBookByIsbnInSet(String isbn);

  @Override
  Page<Book> findAll(@Nullable Specification<Book> spec, @NonNull Pageable pageable);

  // Méthode pour trouver les livres plus récents qu'une date donnée
  List<Book> findByCreatedAtAfter(Date date);
}
