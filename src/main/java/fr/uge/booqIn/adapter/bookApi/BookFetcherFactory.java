package fr.uge.booqIn.adapter.bookApi;

public class BookFetcherFactory {
    private BookFetcherFactory(){

    }
    public static BookFetcher adapter(String apiType) {
      return switch (apiType.toLowerCase()) {
        case "google" -> new GoogleBooksAdapter();
        case "open" -> new OpenApiAdapter();
        default -> throw new IllegalArgumentException("Unsupported API type: " + apiType);
      };
    }
}
