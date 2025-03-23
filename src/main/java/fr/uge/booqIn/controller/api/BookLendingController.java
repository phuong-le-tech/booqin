package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.dto.lending.LendingResponseDTO;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.interfaces.BookLendingInterface;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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

  @PostMapping("/make-loanable")
  public ResponseEntity<ApiResponse<Boolean>> makeBookLoanable(
          @RequestParam Long bookId,
          @RequestParam Long userId) {
    boolean added = bookLendingInterface.loanable(bookId, userId);
    return added
            ? ResponseEntity.ok(ApiResponse.success("Book added to lending list", true))
            : ResponseEntity.badRequest().body(ApiResponse.error("Failed to add book to lending list", false));
  }
  

  @DeleteMapping("/remove-loanable")
  public ResponseEntity<ApiResponse<Boolean>> removeBookFromLoanable(
          @RequestParam Long bookId,
          @RequestParam Long userId) {
    boolean removed = bookLendingInterface.isNoMoreLoanable(bookId, userId);
    
    return removed
            ? ResponseEntity.ok(ApiResponse.success("Book removed from lending list", true))
            : ResponseEntity.badRequest().body(ApiResponse.error("Failed to remove book from lending list", false));
  }
  
  @GetMapping("/my-lendings")
  public ResponseEntity<ApiResponse<LendingResponseDTO>> getCurrentUserLendings(
          @RequestParam Long userId) {
    LendingResponseDTO responseDTO = bookLendingInterface.findAllUserLendingsAsDTO(userId, "lent");
    if (responseDTO.lentBooks().isEmpty()) {
      return ResponseEntity.ok(ApiResponse.success("No lent books found", responseDTO));
    }else{
      return ResponseEntity.ok(ApiResponse.success("User lent books retrieved", responseDTO));
    }
  }
  
  
  @GetMapping("/ticket-download/{lendingId}")
  public void downloadTicket(@PathVariable Long lendingId, HttpServletResponse response) throws IOException {
    byte[] ticketPDF = bookLendingInterface.getShippingTicketBytes(lendingId);
    
    if (ticketPDF == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "Étiquette non trouvée");
      return;
    }
    
    response.setContentType("application/pdf");
    response.setHeader("Content-Disposition", "attachment; filename=etiquette_expedition_" + lendingId + ".pdf");
    response.setContentLength(ticketPDF.length);
    
    try (ServletOutputStream outputStream = response.getOutputStream()) {
      outputStream.write(ticketPDF);
      outputStream.flush();
    }
  }
}