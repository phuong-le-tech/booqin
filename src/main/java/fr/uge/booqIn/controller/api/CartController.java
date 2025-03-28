package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.dto.book.BookShow;
import fr.uge.booqIn.dto.cart.ShopCartDto;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.entity.BookLending;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.dto.cart.CartDto;
import fr.uge.booqIn.service.interfaces.BookLendingInterface;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import fr.uge.booqIn.service.interfaces.CartServiceInterface;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/v1/carts")
public class CartController {
    
    private final CartServiceInterface cartService;
    private final UserServiceInterface userService;
    private final BookServiceInterface bookService;
    private final BookLendingInterface bookLendingInterface;
    
    public CartController(CartServiceInterface cartService, UserServiceInterface userService, BookServiceInterface bookService, BookLendingInterface bookLendingInterface) {
        this.cartService = cartService;
        this.userService = userService;
        this.bookService = bookService;
      this.bookLendingInterface = bookLendingInterface;
    }
    @GetMapping("/check-book-availability/{bookId}")
    public ResponseEntity<ApiResponse<Boolean>> checkBookAvailability(@PathVariable Long bookId) {
        Optional<BookShow> bookOptional = Optional.ofNullable(bookService.findById(bookId));
        
        if (bookOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Book not found", false));
        }
        

        Optional<BookLending> availableBook = bookLendingInterface.findBookLendingByBookId(bookId);
        boolean isAvailable = availableBook.isPresent();
        
        return ResponseEntity.ok(ApiResponse.success(
                isAvailable ? "Book is available" : "Book is not available",
                isAvailable
        ));
    }
    
    @PostMapping("/add-book")
    public ResponseEntity<ApiResponse<Boolean>> addBookToCart(@RequestBody CartDto request) {
        System.out.println(request);
        Optional<UserIndex> userOptional = Optional.ofNullable(userService.findUserByIdToIndex(request.userId()));
        
        Optional<BookShow> bookOptional = Optional.ofNullable(bookService.findById(request.bookId()));
        
        if (userOptional.isEmpty() || bookOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User or Book not found", false));
        }
        var reserved =  bookLendingInterface.reserveBook(bookOptional.get().id(), userOptional.get().id());
        if (!reserved) {
            return ResponseEntity.badRequest().body(ApiResponse.success("Book not available", false));
        }
        boolean updatedCart = cartService.addBookToCart(userOptional.get().id(), bookOptional.get().id());
        
        return updatedCart
                ? ResponseEntity.ok(ApiResponse.success("Book added to cart", true))
                : ResponseEntity.ok(ApiResponse.success("Book already in cart", false));
    
    }
    
    @DeleteMapping("/remove-book")
    public ResponseEntity<ApiResponse<Boolean>> removeBookFromCart(@RequestParam Long userId, @RequestParam Long bookId) {
        Optional<UserIndex> userOptional = Optional.ofNullable(userService.findUserByIdToIndex(userId));
        Optional<BookShow> bookOptional = Optional.ofNullable(bookService.findById(bookId));
        
        if (userOptional.isEmpty() || bookOptional.isEmpty()) {
            return ResponseEntity.status(404).body(ApiResponse.error("User or Book not found", false));
        }
        
        var canceled = bookLendingInterface.cancelBook(bookOptional.get().id(), userOptional.get().id());
     
        boolean updatedCart = cartService.removeBookFromCart(userOptional.get().id(), bookOptional.get().id());
        return updatedCart
                ? ResponseEntity.ok(ApiResponse.success("Book removed from cart", true))
                : ResponseEntity.status(404).body(ApiResponse.error("Book not in cart", false));
    }
    
    @GetMapping("/user-cart/{userId}")
    public ResponseEntity<ApiResponse<ShopCartDto>> getUserCart(@PathVariable Long userId) {
        Optional<UserIndex> userOptional = Optional.ofNullable(userService.findUserByIdToIndex(userId));
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User not found"));
        }
        
        ShopCartDto shopCartDto = cartService.getCartDto(userOptional.get().id());
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved", shopCartDto));
    }
    
    @DeleteMapping("/clear-cart/{userId}")
    public ResponseEntity<ApiResponse<String>> clearCart(@PathVariable Long userId) {
        Optional<UserIndex> userOptional= Optional.ofNullable(userService.findUserByIdToIndex(userId));
        
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("User not found"));
        }
        
        var listBookLending = bookLendingInterface.findUserLendings(userOptional.get().id());
        bookLendingInterface.ListClearBookstoLend(listBookLending);
        
        boolean cleared = cartService.clearCart(userOptional.get().id());
        return cleared
                ? ResponseEntity.ok(ApiResponse.success("Cart cleared", "Cart has been emptied successfully"))
                : ResponseEntity.badRequest().body(ApiResponse.error("Cannot clear a completed cart"));
    }
}