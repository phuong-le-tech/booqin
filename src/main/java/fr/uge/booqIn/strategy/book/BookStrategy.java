package fr.uge.booqIn.strategy.book;

import fr.uge.booqIn.entity.Book;

public interface BookStrategy {
    double calculateShippingCost(Book book);

    boolean isAvailable(Book book);

    void processDelivery(Book book);
}