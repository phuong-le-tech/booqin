package fr.uge.booqIn.repository;

import fr.uge.booqIn.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
  /**
   * Finds a cart by its payment intent identifier.
   *
   * @param paymentIntentId The unique payment intent identifier
   * @return An optional containing the cart if found
   */
  Optional<Cart> findByPaymentIntentId(@NonNull String paymentIntentId);

  /**
   * Finds a cart by its ID with eager loading of books, authors, and ISBNs.
   *
   * @param id The unique identifier of the cart
   * @return An optional containing the cart with its related entities loaded
   */
  @Override
  @Query("SELECT DISTINCT c FROM Cart c LEFT JOIN FETCH c.books b LEFT JOIN FETCH b.authors LEFT JOIN FETCH b.isbns WHERE c.id = :id")
  @NonNull
  Optional<Cart> findById(@NonNull Long id);

  /**
   * Removes a specific book from a cart.
   * <p>
   * This is a native SQL query that directly deletes the book-cart association
   * from the junction table.
   *
   * @param cartId The unique identifier of the cart
   * @param bookId The unique identifier of the book to remove
   */
  @Modifying
  @Query(value = "DELETE FROM cart_books WHERE cart_id = :cartId AND book_id = :bookId", nativeQuery = true)
  void deleteBookFromCart(
          @NonNull Long cartId,
          @NonNull Long bookId
  );
}