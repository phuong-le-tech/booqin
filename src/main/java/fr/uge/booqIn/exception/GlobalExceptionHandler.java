package fr.uge.booqIn.exception;

import com.stripe.exception.*;
import fr.uge.booqIn.factory.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
          MethodArgumentNotValidException ex,
          HttpServletRequest request
  ) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Validation failed", errors));
  }

  @ExceptionHandler(BindException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleBindExceptions(
          BindException ex,
          HttpServletRequest request
  ) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Binding failed", errors));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<Map<String, String>>> handleConstraintViolationExceptions(
          ConstraintViolationException ex,
          HttpServletRequest request
  ) {
    Map<String, String> errors = ex.getConstraintViolations().stream()
            .collect(Collectors.toMap(
                    violation -> violation.getPropertyPath().toString(),
                    violation -> violation.getMessage(),
                    (existing, replacement) -> existing + "; " + replacement
            ));

    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Validation constraints violated", errors));
  }

  @ExceptionHandler(ValidationException.class)
  public ResponseEntity<ApiResponse<ErrorResponse>> handleValidationException(
          ValidationException ex,
          HttpServletRequest request
  ) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    ErrorResponse errorResponse = new ErrorResponse(
            status.value(),
            ex.getMessage(),
            request.getRequestURI(),
            Instant.now()
    );

    return ResponseEntity
            .status(status)
            .body(ApiResponse.error("Validation error", errorResponse));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ApiResponse<ErrorResponse>> handleHttpMessageNotReadable(
          HttpMessageNotReadableException ex,
          HttpServletRequest request
  ) {
    HttpStatus status = HttpStatus.BAD_REQUEST;
    ErrorResponse errorResponse = new ErrorResponse(
            status.value(),
            "Invalid request body format",
            request.getRequestURI(),
            Instant.now()
    );

    return ResponseEntity
            .status(status)
            .body(ApiResponse.error("Malformed JSON request", errorResponse));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiResponse<ErrorResponse>> handleDataIntegrityViolation(
          DataIntegrityViolationException ex,
          HttpServletRequest request
  ) {
    HttpStatus status = HttpStatus.CONFLICT;
    ErrorResponse errorResponse = new ErrorResponse(
            status.value(),
            "Database constraint violation",
            request.getRequestURI(),
            Instant.now()
    );

    return ResponseEntity
            .status(status)
            .body(ApiResponse.error("Data integrity violation", errorResponse));
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ApiResponse<ErrorResponse>> handleDuplicateResourceException(
          DuplicateResourceException ex,
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
            .body(ApiResponse.error("Duplicate resource", errorResponse));
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

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiResponse<ErrorResponse>> handleResourceNotFoundException(
          ResourceNotFoundException ex,
          HttpServletRequest request
  ) {
    HttpStatus status = HttpStatus.NOT_FOUND;
    ErrorResponse errorResponse = new ErrorResponse(
            status.value(),
            ex.getMessage(),
            request.getRequestURI(),
            Instant.now()
    );

    return ResponseEntity
            .status(status)
            .body(ApiResponse.error("Resource not found.", errorResponse));
  }
}