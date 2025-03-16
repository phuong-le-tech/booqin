package fr.uge.booqIn.service;

import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookCollection;
import fr.uge.booqIn.repository.AuthorRepository;
import fr.uge.booqIn.repository.BookCollectionRepository;
import fr.uge.booqIn.repository.BookRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class GlobalCollectionService {
  private final BookCollectionRepository bookCollectionRepository;
  private final BookRepository bookRepository;
  private final AuthorRepository authorRepository;

  public GlobalCollectionService(BookCollectionRepository bookCollectionRepository, BookRepository bookRepository, AuthorRepository authorRepository) {
    this.bookCollectionRepository = bookCollectionRepository;
    this.bookRepository = bookRepository;
    this.authorRepository = authorRepository;
  }

  @Scheduled(cron = "0 0 0 * * *") // Tous les jours à minuit
  @Transactional
  public void updateGlobalCollections() {
    updateAuthorCollections();
    updateRecentBooksCollection();
  }

  @Transactional
  public void updateAuthorCollections() {
    List<Author> authors = authorRepository.findAll();
    for (Author author : authors) {
      // Format du titre pour la collection de l'auteur
      String collectionTitle = "Livres de " + author.getFirstName() + " " + author.getLastName();

      // Rechercher une collection existante pour cet auteur
      Optional<BookCollection> existingCollection = bookCollectionRepository
              .findByTitleAndCollectionType(collectionTitle, BookCollection.CollectionType.GLOBAL);

      BookCollection authorCollection;
      if (existingCollection.isPresent()) {
        // Mettre à jour la collection existante
        authorCollection = existingCollection.get();
      } else {
        // Créer une nouvelle collection pour cet auteur
        authorCollection = new BookCollection();
        authorCollection.setTitle(collectionTitle);
        authorCollection.setDescription("Collection automatique des livres de " +
                author.getFirstName() + " " + author.getLastName());
        authorCollection.setPublic(true);
        authorCollection.setSmart(true);
        authorCollection.setCollectionType(BookCollection.CollectionType.GLOBAL);
        authorCollection.setCreatedAt(new Date());
      }

      // Ajouter tous les livres de l'auteur à la collection
      authorCollection.setBooks(new ArrayList<>(author.getBooks()));
      authorCollection.setUpdatedAt(new Date());

      // Sauvegarder la collection
      bookCollectionRepository.save(authorCollection);
    }
  }

  @Transactional
  public void updateRecentBooksCollection() {
    String recentBooksTitle = "Nouveautés des 30 derniers jours";

    // Rechercher la collection des nouveautés
    Optional<BookCollection> existingCollection = bookCollectionRepository
            .findByTitleAndCollectionType(recentBooksTitle, BookCollection.CollectionType.GLOBAL);

    BookCollection recentBooksCollection;
    if (existingCollection.isPresent()) {
      // Mettre à jour la collection existante
      recentBooksCollection = existingCollection.get();
    } else {
      // Créer une nouvelle collection pour les nouveautés
      recentBooksCollection = new BookCollection();
      recentBooksCollection.setTitle(recentBooksTitle);
      recentBooksCollection.setDescription("Livres ajoutés sur la plateforme au cours des 30 derniers jours");
      recentBooksCollection.setPublic(true);
      recentBooksCollection.setSmart(true);
      recentBooksCollection.setCollectionType(BookCollection.CollectionType.GLOBAL);
      recentBooksCollection.setCreatedAt(new Date());
    }

    // Définir la date limite (30 jours en arrière)
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, -30);
    Date thirtyDaysAgo = calendar.getTime();

    // Récupérer tous les livres créés après cette date
    List<Book> recentBooks = bookRepository.findByCreatedAtAfter(thirtyDaysAgo);

    // Mettre à jour la collection
    recentBooksCollection.setBooks(recentBooks);
    recentBooksCollection.setUpdatedAt(new Date());

    // Sauvegarder la collection
    bookCollectionRepository.save(recentBooksCollection);
  }
}
