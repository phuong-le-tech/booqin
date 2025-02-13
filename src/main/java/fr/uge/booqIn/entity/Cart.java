package fr.uge.booqIn.entity;

import fr.uge.booqIn.enums.Statut;
import java.util.List;

public class Cart {

    private User user;

    private Statut statut;

    private List<Book> books;

    public Cart(User user, Statut statut, List<Book> books) {
        this.user = user;
        this.statut = statut;
        this.books = books;
    }

}
