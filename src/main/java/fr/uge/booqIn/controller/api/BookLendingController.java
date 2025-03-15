package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.dto.lending.BookLendingShow;
import fr.uge.booqIn.dto.lending.LendingResponseDTO;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.interfaces.BookLendingInterface;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/loans")
public class BookLendingController {
  
  private final UserServiceInterface userService;
  private final BookServiceInterface bookService;
  private final BookLendingInterface bookLendingInterface;
  
  public BookLendingController(
          UserServiceInterface userService,
          BookServiceInterface bookService,
          BookLendingInterface bookLendingInterface) {
    this.userService = userService;
    this.bookService = bookService;
    this.bookLendingInterface = bookLendingInterface;
  }
  
  @GetMapping("/confirm-loan")
  public ResponseEntity<ApiResponse<Boolean>> confirmLoan(@RequestParam Long lendingId) {
    boolean confirmed = bookLendingInterface.confirmLoan(lendingId);
    
    return confirmed
            ? ResponseEntity.ok(ApiResponse.success("Loan confirmed", true))
            : ResponseEntity.badRequest().body(ApiResponse.error("Loan not confirmed", false));
  }
  
  @GetMapping("/ship-book")
  public ResponseEntity<ApiResponse<Boolean>> shipBook(@RequestParam Long lendingId) {
    boolean shipped = bookLendingInterface.markAsShipped(lendingId);
    
    return shipped
            ? ResponseEntity.ok(ApiResponse.success("Book shipped", true))
            : ResponseEntity.badRequest().body(ApiResponse.error("Book not shipped", false));
  }
  
  @GetMapping("/return-book")
  public ResponseEntity<ApiResponse<Boolean>> returnBook(@RequestParam Long lendingId) {
    boolean returned = bookLendingInterface.returnBook(lendingId);
    
    return returned
            ? ResponseEntity.ok(ApiResponse.success("Book returned", true))
            : ResponseEntity.badRequest().body(ApiResponse.error("Book not returned", false));
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<ApiResponse<LendingResponseDTO>> getUserLendings(
          @PathVariable Long userId,
          @RequestParam(defaultValue = "borrowed") String type) {
    
    if (!type.equalsIgnoreCase("borrowed") &&
            !type.equalsIgnoreCase("lent") &&
            !type.equalsIgnoreCase("all")) {
      return ResponseEntity.badRequest().body(
              ApiResponse.error("Invalid type parameter. Valid values are: borrowed, lent, all", null));
    }
    
    LendingResponseDTO responseDTO = bookLendingInterface.findAllUserLendingsAsDTO(userId, type);
    
    String message;
    if (type.equalsIgnoreCase("all")) {
      message = "All user book lendings retrieved";
    } else if (type.equalsIgnoreCase("borrowed")) {
      message = responseDTO.borrowedBooks().isEmpty() ?
              "No borrowed books found" :
              "Borrowed books retrieved successfully";
    } else {
      message = responseDTO.lentBooks().isEmpty() ?
              "No lent books found" :
              "Lent books retrieved successfully";
    }
    
    return ResponseEntity.ok(ApiResponse.success(message, responseDTO));
  }
}