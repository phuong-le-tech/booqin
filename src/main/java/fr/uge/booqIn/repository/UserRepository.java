package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.AbstractBookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

  @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.collections WHERE u.id = :id")
  Optional<User> findByIdWithCollections(Long id);

//  @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.booksToLend WHERE u.id = :id")
//  Optional<User> findByIdWithBooksToLend(Long id);

  Optional<User> findByEmail(String email);

  Optional<User> findByCartId(Long cartId);

  Optional<User> findByRole(UserRole role);

}