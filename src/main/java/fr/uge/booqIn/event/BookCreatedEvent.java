package fr.uge.booqIn.event;

import fr.uge.booqIn.entity.Book;
import org.springframework.context.ApplicationEvent;

/**
 * Événement déclenché lorsqu'un nouveau livre est créé
 */
public class BookCreatedEvent extends ApplicationEvent {
    private final Book book;
    
    public BookCreatedEvent(Object source, Book book) {
        super(source);
        this.book = book;
    }
    
    public Book getBook() {
        return book;
    }
}