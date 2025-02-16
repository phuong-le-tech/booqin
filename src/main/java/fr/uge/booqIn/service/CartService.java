package fr.uge.booqIn.service;

import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.enums.Statut;
import fr.uge.booqIn.repository.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Cart createCart(User user, List<Book> books) {
        Cart cart = new Cart(user, Statut.PENDING, books);
        return cartRepository.save(cart);
    }

    public Cart getCart(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found with id: " + cartId));
    }

    public Cart updateCartStatus(Long cartId, Statut status) {
        Cart cart = getCart(cartId);
        cart.setStatut(status);
        return cartRepository.save(cart);
    }

    public void deleteCart(Long cartId) {
        cartRepository.deleteById(cartId);
    }

    public List<Cart> getUserCarts(Long userId) {
        return cartRepository.findByUserId(userId);
    }
}