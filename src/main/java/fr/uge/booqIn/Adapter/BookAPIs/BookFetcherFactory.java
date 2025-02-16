package fr.uge.booqIn.Adapter.BookAPIs;

public class BookFetcherFactory {
    private BookFetcherFactory(){

    }
    public static BookFetcher adapter(String apiType) {
      return switch (apiType.toLowerCase()) {
        case "google" -> new GoogleBooksAdapter();
        //case "open" -> new OpenApiAdapter();
        default -> throw new IllegalArgumentException("Unsupported API type: " + apiType);
      };
    }
}
