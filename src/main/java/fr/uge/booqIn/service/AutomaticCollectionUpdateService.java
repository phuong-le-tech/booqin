/*
package fr.uge.booqIn.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.uge.booqIn.entity.Author;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.GlobalBookCollection;
import fr.uge.booqIn.entity.SmartBookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.factory.CriteriaStrategyFactory;
import fr.uge.booqIn.repository.AuthorRepository;
import fr.uge.booqIn.repository.BookCollectionRepository;
import fr.uge.booqIn.repository.BookRepository;
import fr.uge.booqIn.repository.UserRepository;
import fr.uge.booqIn.strategy.collection.CollectionCriteriaStrategy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.logging.Logger;

*/
/**
 * Service responsable de la mise à jour automatique des collections.
 * Cette classe respecte le principe de responsabilité unique en centralisant 
 * la logique de mise à jour des collections globales et intelligentes.
 *//*

@Service
public class AutomaticCollectionUpdateService {
    private static final Logger logger = Logger.getLogger(AutomaticCollectionUpdateService.class.getName());
    
    private final BookCollectionRepository bookCollectionRepository;
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final UserRepository userRepository;
    private final CriteriaStrategyFactory strategyFactory;
    private final ObjectMapper objectMapper;
    
    public AutomaticCollectionUpdateService(
            BookCollectionRepository bookCollectionRepository,
            BookRepository bookRepository,
            AuthorRepository authorRepository,
            UserRepository userRepository,
            CriteriaStrategyFactory strategyFactory,
            ObjectMapper objectMapper) {
        this.bookCollectionRepository = bookCollectionRepository;
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.userRepository = userRepository;
        this.strategyFactory = strategyFactory;
        this.objectMapper = objectMapper;
    }
    
    */
/**
     * Met à jour toutes les collections globales.
     * Cette méthode est planifiée pour s'exécuter quotidiennement.
     *//*

    @Scheduled(cron = "0 0 0 * * *") // Tous les jours à minuit
    @Transactional
    public void updateAllGlobalCollections() {
        logger.info("Updating all global collections");
        updateAuthorCollections();
        updateRecentBooksCollection();
        logger.info("Global collections updated successfully");
    }
    
    */
/**
     * Met à jour les collections pour chaque auteur dans le système.
     *//*

    @Transactional
    public void updateAuthorCollections() {
        // Récupération de l'utilisateur admin pour posséder les collections globales
        User adminUser = userRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("Admin user not found"));

        List<Author> authors = authorRepository.findAll();
        for (Author author : authors) {
            // Format du titre pour la collection de l'auteur
            String collectionTitle = "Livres de " + author.getFirstName() + " " + author.getLastName();

            // Recherche d'une collection existante pour cet auteur
            Optional<GlobalBookCollection> existingCollection = bookCollectionRepository
                    .findGlobalCollectionByTitle(collectionTitle);

            GlobalBookCollection authorCollection;
            if (existingCollection.isPresent()) {
                // Mise à jour de la collection existante
                authorCollection = existingCollection.get();
            } else {
                // Création d'une nouvelle collection pour cet auteur
                authorCollection = new GlobalBookCollection.Builder()
                        .title(collectionTitle)
                        .description("Collection automatique des livres de " +
                                author.getFirstName() + " " + author.getLastName())
                        .owner(adminUser)
                        .build();
            }

            // Ajout de tous les livres de l'auteur à la collection
            authorCollection.setBooks(new ArrayList<>(author.getBooks()));
            authorCollection.setUpdatedAt(new Date());

            // Sauvegarde de la collection
            bookCollectionRepository.save(authorCollection);
            logger.info("Updated author collection: " + collectionTitle);
        }
    }
    
    */
/**
     * Met à jour la collection des livres récemment ajoutés.
     *//*

    @Transactional
    public void updateRecentBooksCollection() {
        String recentBooksTitle = "Nouveautés des 30 derniers jours";
        User adminUser = userRepository.findById(1L)
                .orElseThrow(() -> new IllegalStateException("Admin user not found"));

        // Recherche de la collection existante des nouveautés
        Optional<GlobalBookCollection> existingCollection = bookCollectionRepository
                .findGlobalCollectionByTitle(recentBooksTitle);

        GlobalBookCollection recentBooksCollection;
        if (existingCollection.isPresent()) {
            // Mise à jour de la collection existante
            recentBooksCollection = existingCollection.get();
        } else {
            // Création d'une nouvelle collection pour les nouveautés
            recentBooksCollection = new GlobalBookCollection.Builder()
                    .title(recentBooksTitle)
                    .description("Livres ajoutés sur la plateforme au cours des 30 derniers jours")
                    .owner(adminUser)
                    .build();
        }

        // Définition de la date limite (30 jours en arrière)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -30);
        Date thirtyDaysAgo = calendar.getTime();

        // Récupération de tous les livres créés après cette date
        List<Book> recentBooks = bookRepository.findByCreatedAtAfter(thirtyDaysAgo);

        // Mise à jour de la collection
        recentBooksCollection.setBooks(recentBooks);
        recentBooksCollection.setUpdatedAt(new Date());

        // Sauvegarde de la collection
        bookCollectionRepository.save(recentBooksCollection);
        logger.info("Updated recent books collection with " + recentBooks.size() + " books");
    }
    
    */
/**
     * Met à jour toutes les collections intelligentes avec les derniers livres
     * correspondant à leurs critères.
     *//*

    @Scheduled(fixedRate = 24 * 3600 * 1000)  // Tous les jours
    @Transactional
    public void updateAllSmartCollections() {
        logger.info("Updating all smart collections");
        List<SmartBookCollection> smartCollections = bookCollectionRepository.findAllSmartCollections();
        
        List<SmartBookCollection> updatedCollections = smartCollections.stream()
                .map(this::updateSmartCollection)
                .toList();
        
        bookCollectionRepository.saveAll(updatedCollections);
        logger.info("Updated " + updatedCollections.size() + " smart collections");
    }
    
    */
/**
     * Met à jour une collection intelligente spécifique avec les derniers livres correspondants.
     *
     * @param collection La collection intelligente à mettre à jour
     * @return La collection mise à jour
     *//*

    @Transactional
    public SmartBookCollection updateSmartCollection(SmartBookCollection collection) {
        try {
            // Récréation de la stratégie de critères à partir du JSON stocké
            if (collection.getCriteriaJson() != null && !collection.getCriteriaJson().isEmpty()) {
                Map<String, Object> criteria = objectMapper.readValue(
                        collection.getCriteriaJson(),
                        objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class)
                );
                
                CollectionCriteriaStrategy strategy = strategyFactory.createCriteriaStrategy(criteria);
                collection.setCriteriaStrategy(strategy);
            }
            
            // Mise à jour des livres en fonction des critères
            if (collection.getCriteriaStrategy() != null) {
                collection.updateBooks(bookRepository.findAll());
                collection.setUpdatedAt(new java.util.Date());
            } else {
                logger.warning("Cannot update collection without criteria strategy: " + collection.getId());
            }
            
            return collection;
        } catch (Exception e) {
            logger.severe("Failed to update smart collection: " + e.getMessage());
            throw new RuntimeException("Failed to update smart collection: " + collection.getTitle(), e);
        }
    }
}*/
