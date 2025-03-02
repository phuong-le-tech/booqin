package fr.uge.booqIn.factory;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {

  public static <U> ApiResponse<U> success(String message, U data) {
    return new ApiResponse<>(true, message, data);
  }

  public static <U> ApiResponse<U> success(String message) {
    return new ApiResponse<>(true, message, null);
  }

  public static <U> ApiResponse<U> error(String message, U data) {
    return new ApiResponse<>(false, message, data);
  }

  public static <U> ApiResponse<U> error(String message) {
    return new ApiResponse<>(false, message, null);
  }
}