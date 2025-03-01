package fr.uge.booqIn.service;

import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.enums.StatutPayment;
import fr.uge.booqIn.repository.CartRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {
    
    private final CartRepository cartRepository;
    
    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }
    
    public Cart createCart(User user, List<Book> books) {
        Cart cart = new Cart(user, StatutPayment.PENDING, books);
        return cartRepository.save(cart);
    }
    
    public Cart getCart(Long cartId) {
        return cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found with id: " + cartId));
    }
    
    public Cart updateCartStatus(Long cartId, StatutPayment status) {
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
    
    @Transactional
    public Cart addBookToCart(User user, Book book) {
        Cart cart = getCart(user);
        if (!cart.getBooks().contains(book)) {
            cart.getBooks().add(book);
            cartRepository.save(cart);
        }
        return cart;
    }
    
    @Transactional
    public Cart removeBookFromCart(User user, Book book) {
        Cart cart = getCart(user);
        cart.getBooks().remove(book);
        cartRepository.save(cart);
        return cart;
    }
    
    @Transactional
    public Cart getCart(User user) {
        return cartRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Cart should always exist for a user"));
    }
    
    @Transactional
    public void clearCart(User user) {
        Cart cart = getCart(user);
        cart.getBooks().clear();
        cartRepository.save(cart);
    }
}
