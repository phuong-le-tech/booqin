package fr.uge.booqIn.factory;

import java.util.HashMap;
import java.util.Map;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Map<String, Object> meta
) {
  public ApiResponse(boolean success, String message, T data) {
    this(success, message, data, new HashMap<>());
  }

  public static <U> ApiResponse<U> success(String message, U data) {
    return new ApiResponse<>(true, message, data);
  }

  public static <U> ApiResponse<U> success(String message, U data, Map<String, Object> meta) {
    return new ApiResponse<>(true, message, data, meta);
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