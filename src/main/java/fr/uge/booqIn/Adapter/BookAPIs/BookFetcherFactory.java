package fr.uge.booqIn.Adapter.BookAPIs;

public class BookFetcherFactory {
    public static BookFetcher adapter(String apiType) {
        switch (apiType.toLowerCase()) {
            case "google":
                return new GoogleBooksAdapter();
            default:
                throw new IllegalArgumentException("Unsupported API type: " + apiType);
        }
    }
}
