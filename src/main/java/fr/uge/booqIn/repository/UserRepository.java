package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  User findByPersonEmail(String email);
  Optional<User> findById(long id);
  
}