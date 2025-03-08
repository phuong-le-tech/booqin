package fr.uge.booqIn.repository;

import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart findByUser(User user);
    Optional<Cart> findByPaymentIntentId(String paymentIntentId);


    @Override
    @Query("SELECT DISTINCT c FROM Cart c LEFT JOIN FETCH c.books b LEFT JOIN FETCH b.authors LEFT JOIN FETCH b.isbns WHERE c.id = :id")
    @NonNull
    Optional<Cart> findById(@NonNull Long id);
    
    
    @Modifying
    @Query(value = "DELETE FROM cart_books WHERE cart_id = :cartId AND book_id = :bookId", nativeQuery = true)
    void deleteBookFromCart(@Param("cartId") Long cartId, @Param("bookId") Long bookId);
    
    
}