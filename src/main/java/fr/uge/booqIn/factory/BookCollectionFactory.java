package fr.uge.booqIn.factory;

import fr.uge.booqIn.entity.GlobalBookCollection;
import fr.uge.booqIn.entity.NormalBookCollection;
import fr.uge.booqIn.entity.SmartBookCollection;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.strategy.collection.CollectionCriteriaStrategy;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Factory for creating different types of book collections.
 * This factory simplifies the creation of collection instances with the
 * appropriate type and configuration.
 */
@Component
public class BookCollectionFactory {

    private final CriteriaStrategyFactory criteriaStrategyFactory;

    public BookCollectionFactory(CriteriaStrategyFactory criteriaStrategyFactory) {
        this.criteriaStrategyFactory = criteriaStrategyFactory;
    }

    /**
     * Creates a normal book collection.
     *
     * @param title       The title of the collection
     * @param description The description of the collection
     * @param owner       The owner of the collection
     * @param isPublic    Whether the collection is public
     * @return A new normal book collection
     */
    public NormalBookCollection createNormalCollection(String title, String description, User owner, boolean isPublic) {
        return new NormalBookCollection.Builder()
                .title(title)
                .description(description)
                .owner(owner)
                .isPublic(isPublic)
                .build();
    }

    /**
     * Creates a smart book collection based on criteria.
     *
     * @param title       The title of the collection
     * @param description The description of the collection
     * @param owner       The owner of the collection
     * @param isPublic    Whether the collection is public
     * @param criteria    Map of criteria for automatically selecting books
     * @return A new smart book collection
     */
    public SmartBookCollection createSmartCollection(
            String title, 
            String description, 
            User owner, 
            boolean isPublic, 
            Map<String, Object> criteria) {
        
        CollectionCriteriaStrategy strategy = criteriaStrategyFactory.createCriteriaStrategy(criteria);
        
        return new SmartBookCollection.Builder()
                .title(title)
                .description(description)
                .owner(owner)
                .isPublic(isPublic)
                .criteriaStrategy(strategy)
                .build();
    }

    /**
     * Creates a global book collection.
     *
     * @param title       The title of the collection
     * @param description The description of the collection
     * @param owner       The owner of the collection (typically admin)
     * @return A new global book collection
     */
    public GlobalBookCollection createGlobalCollection(String title, String description, User owner) {
        return new GlobalBookCollection.Builder()
                .title(title)
                .description(description)
                .owner(owner)
                .build();
    }
}