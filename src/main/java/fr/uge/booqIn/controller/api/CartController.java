package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.cart.ShopCartDto;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.dto.user.UserShow;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.Cart;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.impl.BookServiceImpl;
import fr.uge.booqIn.service.impl.CartServiceImpl;
import fr.uge.booqIn.service.impl.UserServiceImpl;
import fr.uge.booqIn.dto.cart.CartDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/v1/cart")
public class CartController {
    
    private final CartServiceImpl cartService;
    private final UserServiceImpl userService;
    private final BookServiceImpl bookService;
    
    public CartController(CartServiceImpl cartService, UserServiceImpl userService, BookServiceImpl bookService) {
        this.cartService = cartService;
        this.userService = userService;
        this.bookService = bookService;
    }
    
    @PostMapping("/add-book")
    public ResponseEntity<ApiResponse<Boolean>> addBookToCart(@RequestBody CartDto request) {
        
        Optional<UserIndex> userOptional = Optional.ofNullable(userService.findUserByIdtoIndex(request.userId()));
        
        Optional<BookShow> bookOptional = Optional.ofNullable(bookService.findById(request.bookId()));
        
        if (userOptional.isEmpty() || bookOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User or Book not found", false));
        }
        
        boolean updatedCart = cartService.addBookToCart(userOptional.get().id(), bookOptional.get().id());
        
        return updatedCart
                ? ResponseEntity.ok(ApiResponse.success("Book added to cart", true))
                : ResponseEntity.ok(ApiResponse.success("Book already in cart", false));
    
    }
    
    @DeleteMapping("/remove-book")
    public ResponseEntity<ApiResponse<Boolean>> removeBookFromCart(@RequestParam Long userId, @RequestParam Long bookId) {
        Optional<UserIndex> userOptional = Optional.ofNullable(userService.findUserByIdtoIndex(userId));
        Optional<BookShow> bookOptional = Optional.ofNullable(bookService.findById(bookId));
        
        if (userOptional.isEmpty() || bookOptional.isEmpty()) {
            return ResponseEntity.status(404).body(ApiResponse.error("User or Book not found", false));
        }
        
        boolean updatedCart = cartService.removeBookFromCart(userOptional.get().id(), bookOptional.get().id());
        return updatedCart
                ? ResponseEntity.ok(ApiResponse.success("Book removed from cart", true))
                : ResponseEntity.status(404).body(ApiResponse.error("Book not in cart", false));
    }
    
    @GetMapping("/user-cart/{userId}")
    public ResponseEntity<ApiResponse<ShopCartDto>> getUserCart(@PathVariable Long userId) {
        Optional<UserIndex> userOptional = Optional.ofNullable(userService.findUserByIdtoIndex(userId));
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User not found"));
        }
        
        ShopCartDto shopCartDto = cartService.getCartDto(userOptional.get().id());
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved", shopCartDto));
    }
    
    @DeleteMapping("/clear-cart/{userId}")
    public ResponseEntity<ApiResponse<String>> clearCart(@PathVariable Long userId) {
        Optional<UserIndex> userOptional= Optional.ofNullable(userService.findUserByIdtoIndex(userId));
        
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User not found"));
        }
        
        boolean cleared = cartService.clearCart(userOptional.get().id());
        return cleared
                ? ResponseEntity.ok(ApiResponse.success("Cart cleared", "Cart has been emptied successfully"))
                : ResponseEntity.badRequest().body(ApiResponse.error("Cannot clear a completed cart"));
    }
}