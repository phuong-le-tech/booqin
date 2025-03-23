package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.dto.cart.ShopCartDto;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.enums.StatusPayment;

public interface CartServiceInterface {

  Cart getCart(Long cartId);
  Cart updateCartStatus(Long cartId, StatusPayment status);
  boolean addBookToCart(Long id, Long bookId);
  boolean removeBookFromCart(Long id, Long bookId);
  ShopCartDto getCartDto(Long userId);
  boolean clearCart(Long user);
  boolean clearCartAfterPayment(Long cartId);

}
