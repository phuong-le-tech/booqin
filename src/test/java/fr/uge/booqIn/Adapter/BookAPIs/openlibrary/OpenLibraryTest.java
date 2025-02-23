package fr.uge.booqIn.Adapter.BookAPIs.openlibrary;

import fr.uge.booqIn.entity.Book;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class OpenLibraryTest {

    @Mock
    private OpenAPI openLibrary;


    @Test
    public void testSearchByTitle() {
        List<Book> books = openLibrary.searchByTitle("Some Title");
        assertNotNull(books);
        assertEquals("Some Title", books.getFirst().getTitle());
    }

    @Test
    public void testSearchByAuthor() {
        List<Book> books = openLibrary.searchByAuthor("J.R.R. Tolkien");
    }

}