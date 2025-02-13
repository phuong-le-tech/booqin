package fr.uge.booqIn.strategy;

import fr.uge.booqIn.entity.Book;
import org.springframework.stereotype.Component;

@Component
public class StandardBookStrategy implements BookStrategy {

    private static final double STANDARD_SHIPPING_COST = 5.99;

    @Override
    public boolean isAvailable(Book book) {
        return true;
    }

    @Override
    public double calculateShippingCost(Book book) {
        return STANDARD_SHIPPING_COST;
    }

    @Override
    public void processDelivery(Book book) {
        return;
    }
}