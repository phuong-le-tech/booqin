package fr.uge.booqIn.service.interfaces;

import fr.uge.booqIn.dto.cart.ShopCartDto;
import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.enums.StatutPayment;

import java.util.List;

public interface CartServiceInterface {

  Cart getCart(Long cartId);
  Cart updateCartStatus(Long cartId, StatutPayment status);
  boolean addBookToCart(Long id, Long bookId);
  boolean removeBookFromCart(Long id, Long bookId);
  ShopCartDto getCartDto(Long userId);
  boolean clearCart(Long user);

}
