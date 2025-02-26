package fr.uge.booqIn.exception;

import com.stripe.exception.*;
import fr.uge.booqIn.factory.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<ErrorResponse>> handleIllegalArgumentException(
          IllegalArgumentException ex,
          HttpServletRequest request
  ) {
    HttpStatus status = HttpStatus.CONFLICT;
    ErrorResponse errorResponse = new ErrorResponse(
            status.value(),
            ex.getMessage(),
            request.getRequestURI(),
            Instant.now()
    );

    return ResponseEntity
            .status(status)
            .body(ApiResponse.error("Illegal argument provided.", errorResponse));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<ErrorResponse>> handleGenericException(
          Exception ex,
          HttpServletRequest request
  ) {
    HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    ErrorResponse errorResponse = new ErrorResponse(
            status.value(),
            ex.getMessage(),
            request.getRequestURI(),
            Instant.now()
    );

    return ResponseEntity
            .status(status)
            .body(ApiResponse.error("An unexpected error occurred.", errorResponse));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiResponse<ErrorResponse>> handleAccessDenied(
          AccessDeniedException ex,
          HttpServletRequest request
  ) {
    HttpStatus status = HttpStatus.FORBIDDEN;
    ErrorResponse errorResponse = new ErrorResponse(
            status.value(),
            ex.getMessage(),
            request.getRequestURI(),
            Instant.now()
    );

    return ResponseEntity
            .status(status)
            .body(ApiResponse.error("Access denied", errorResponse));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse<ErrorResponse>> handleMethodArgumentTypeMismatchException(
          MethodArgumentTypeMismatchException ex,
          HttpServletRequest request
  ) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    ErrorResponse errorResponse = new ErrorResponse(
            status.value(),
            "Invalid parameter type for '" + ex.getName() + "': " + ex.getValue(),
            request.getRequestURI(),
            Instant.now()
    );

    return ResponseEntity
            .status(status)
            .body(ApiResponse.error("Method argument type mismatch", errorResponse));
  }
  
  
  private HttpStatus getStripeHttpStatus(StripeException ex) {
    Map<Class<? extends StripeException>, HttpStatus> statusMap = Map.of(
            AuthenticationException.class, HttpStatus.UNAUTHORIZED,
            InvalidRequestException.class, HttpStatus.BAD_REQUEST,
            CardException.class, HttpStatus.PAYMENT_REQUIRED,
            RateLimitException.class, HttpStatus.TOO_MANY_REQUESTS,
            ApiException.class, HttpStatus.INTERNAL_SERVER_ERROR
    );
    
    return statusMap.getOrDefault(ex.getClass(), HttpStatus.BAD_REQUEST);
  }
  
  @ExceptionHandler(StripeException.class)
  public ResponseEntity<ApiResponse<ErrorResponse>> handleStripeException(
          StripeException ex,
          HttpServletRequest request
  ) {
    HttpStatus status = getStripeHttpStatus(ex);
    
    ErrorResponse errorResponse = new ErrorResponse(
            status.value(),
            ex.getMessage(),
            request.getRequestURI(),
            Instant.now()
    );
    
    return ResponseEntity
            .status(status)
            .body(ApiResponse.error("Stripe error: " + ex.getMessage(), errorResponse));
  }
  
}

