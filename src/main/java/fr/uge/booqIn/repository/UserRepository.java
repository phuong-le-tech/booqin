package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

  @Override
  @Query("SELECT DISTINCT u FROM User u " +
          "LEFT JOIN FETCH u.collections " +
          "LEFT JOIN FETCH u.booksToLend " +
          "WHERE u.id = :id")
  @NonNull
  Optional<User> findById(@NonNull Long id);

  Optional<User> findByEmail(String email);

}