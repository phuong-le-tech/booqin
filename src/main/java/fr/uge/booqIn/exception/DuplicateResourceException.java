package fr.uge.booqIn.exception;

/**
 * Exception thrown when attempting to create a resource with a field value that already exists
 */
public class DuplicateResourceException extends RuntimeException {
  public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
    super("Resource " + resourceName + " with " + fieldName + " " + fieldValue + " already exists");
  }
}