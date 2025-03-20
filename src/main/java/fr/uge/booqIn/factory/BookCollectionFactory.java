package fr.uge.booqIn.factory;

import fr.uge.booqIn.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Logger;

/**
 * Factory for creating different types of book collections.
 * This factory simplifies the creation of collection instances with the
 * appropriate type and configuration.
 */
@Component
public class BookCollectionFactory {
    private static final Logger logger = Logger.getLogger(BookCollectionFactory.class.getName());
    private static final CriteriaStrategyFactory criteriaFactory = new CriteriaStrategyFactory();

    /**
     * Creates a normal book collection.
     *
     * @param title       The title of the collection
     * @param description The description of the collection
     * @param owner       The owner of the collection
     * @param isPublic    Whether the collection is public
     * @return A new normal book collection
     */
    public static NormalBookCollection createNormalCollection(String title, String description, User owner, boolean isPublic) {
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
     * @return A new smart book collection
     */
    public static SmartBookCollection createSmartCollection(
            String title, String description, boolean isPublic,
            String criteriaString, User owner, List<Book> allBooks) {

        SmartBookCollection collection = new SmartBookCollection.Builder()
                .title(title)
                .description(description)
                .isPublic(isPublic)
                .criteria(criteriaString)
                .owner(owner)
                .build();

        if (collection.getCriteriaStrategy() == null) {
            collection.initializeCriteriaStrategy(criteriaFactory);
        }
        collection.updateBooks(allBooks);

        logger.info("Created smart collection: " + collection.getTitle());
        logger.info("Criteria: " + collection.getCriteriaString());
        logger.info("Criteria strategy: " + collection.getCriteriaStrategy().getClass().getSimpleName());
        return collection;
    }

    /**
     * Creates a global book collection.
     *
     * @param title       The title of the collection
     * @param description The description of the collection
     * @param owner       The owner of the collection (typically admin)
     * @return A new global book collection
     */
    public static GlobalBookCollection createGlobalCollection(String title, String description, User owner) {
        return new GlobalBookCollection.Builder()
                .title(title)
                .description(description)
                .owner(owner)
                .build();
    }

    /**
     * Creates a global book collection with criteria.
     *
     * @param title          The title of the collection
     * @param description    The description of the collection
     * @param owner          The owner of the collection (typically admin)
     * @param criteriaString The criteria string for filtering books
     * @param allBooks       All books to filter against criteria
     * @return A new global book collection
     */
    public static GlobalBookCollection createGlobalCollection(
            String title, String description, User owner,
            String criteriaString, List<Book> allBooks) {

        GlobalBookCollection collection = new GlobalBookCollection.Builder()
                .title(title)
                .description(description)
                .owner(owner)
                .criteria(criteriaString)
                .build();

        if (criteriaString != null && !criteriaString.isEmpty()) {
            collection.initializeCriteriaStrategy(criteriaFactory);

            if (allBooks != null) {
                collection.updateBooks(allBooks);
            }
        }

        logger.info("Created global collection: " + collection.getTitle());
        if (criteriaString != null && !criteriaString.isEmpty()) {
            logger.info("Criteria: " + collection.getCriteriaString());
        }

        return collection;
    }
}