package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.dto.cart.ShopCartDto;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.enums.StatusPayment;
import org.springframework.lang.NonNull;

/**
 * Service interface for managing shopping carts in the BooqIn application.
 * Provides methods to retrieve, update, add items to, and remove items from carts.
 */
public interface CartServiceInterface {

  /**
   * Retrieves a cart by its associated user ID
   *
   * @param userId the ID of the user whose cart to retrieve
   * @return the user's cart
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if the cart is not found
   */
  Cart getCart(@NonNull Long userId);

  /**
   * Updates the payment status of a cart
   *
   * @param cartId the ID of the cart to update
   * @param status the new payment status to set
   * @return the updated cart
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if the cart is not found
   */
  Cart updateCartStatus(@NonNull Long cartId, @NonNull StatusPayment status);

  /**
   * Adds a book to a user's cart
   *
   * @param userId the ID of the user whose cart to modify
   * @param bookId the ID of the book to add
   * @return true if the book was added, false if it was already in the cart
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if the user or book is not found
   */
  boolean addBookToCart(@NonNull Long userId, @NonNull Long bookId);

  /**
   * Removes a book from a user's cart
   *
   * @param userId the ID of the user whose cart to modify
   * @param bookId the ID of the book to remove
   * @return true if the operation was successful
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if the user or book is not found
   */
  boolean removeBookFromCart(@NonNull Long userId, @NonNull Long bookId);

  /**
   * Retrieves a cart as a DTO with detailed information
   *
   * @param userId the ID of the user whose cart to retrieve
   * @return a DTO containing cart information
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if the cart is not found
   */
  ShopCartDto getCartDto(@NonNull Long userId);

  /**
   * Clears all books from a user's cart and resets status to PENDING
   *
   * @param userId the ID of the user whose cart to clear
   * @return true if the operation was successful
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if the cart is not found
   */
  boolean clearCart(@NonNull Long userId);

  /**
   * Clears a cart after payment has been processed
   *
   * @param cartId the ID of the cart to clear
   * @throws fr.uge.booqIn.exception.ResourceNotFoundException if the cart is not found
   */
  void clearCartAfterPayment(@NonNull Long cartId);
}