package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.BookService;
import fr.uge.booqIn.service.CartService;
import fr.uge.booqIn.service.UserService;
import fr.uge.booqIn.dto.cart.CartDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/v1/cart")
public class CartController {
    
    private final CartService cartService;
    private final UserService userService;
    private final BookService bookService;
    
    public CartController(CartService cartService, UserService userService, BookService bookService) {
        this.cartService = cartService;
        this.userService = userService;
        this.bookService = bookService;
    }
    
    @PostMapping("/add-book")
    public ResponseEntity<ApiResponse<Cart>> addBookToCart(@RequestBody CartDto request) {
        Optional<User> userOptional = userService.findEntityById(request.userId());
        Optional<Book> bookOptional = bookService.findByIdWithIsbns(request.bookId());
        
        System.out.println("userOptional: " + userOptional);
        System.out.println("bookOptional: " + bookOptional);
        
        if (userOptional.isEmpty() || bookOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User or Book not found"));
        }
        
        Cart updatedCart = cartService.addBookToCart(userOptional.get(), bookOptional.get());
        return ResponseEntity.ok(ApiResponse.success("Book added to cart", updatedCart));
    }
    
    
    
    @DeleteMapping("/remove-book")
    public ResponseEntity<ApiResponse<Cart>> removeBookFromCart(@RequestParam Long userId, @RequestParam Long bookId) {
        Optional<User> userOptional = userService.findEntityById(userId);
        Optional<Book> bookOptional = bookService.findByIdWithIsbns(bookId);
        
        if (userOptional.isEmpty() || bookOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User or Book not found"));
        }
        
        Cart updatedCart = cartService.removeBookFromCart(userOptional.get(), bookOptional.get());
        return ResponseEntity.ok(ApiResponse.success("Book removed from cart", updatedCart));
    }
    
    @GetMapping("/user-cart/{userId}")
    public ResponseEntity<ApiResponse<Cart>> getUserCart(@PathVariable Long userId) {
        Optional<User> userOptional = userService.findEntityById(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User not found"));
        }
        
        Cart cart = cartService.getCart(userOptional.get());
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved", cart));
    }
    
    @DeleteMapping("/clear-cart/{userId}")
    public ResponseEntity<ApiResponse<String>> clearCart(@PathVariable Long userId) {
        Optional<User> userOptional = userService.findEntityById(userId);
        
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User not found"));
        }
        
        cartService.clearCart(userOptional.get());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", "Cart has been emptied successfully"));
    }
    
}
