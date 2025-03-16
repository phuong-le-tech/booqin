package fr.uge.booqIn.adapter.bookApi;

import fr.uge.booqIn.entity.Author;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AuthorNameParser {
    private static final Logger logger = Logger.getLogger(AuthorNameParser.class.getName());

    // Pattern to match names with suffixes like "Jr.", "Sr.", "III", etc.
    private static final Pattern SUFFIX_PATTERN =
            Pattern.compile("^(.+?)\\s+(Jr\\.|Sr\\.|I{2,3}|IV|V|VI)$", Pattern.CASE_INSENSITIVE);

    // Maximum length for author name fields to prevent database overflow
    private static final int MAX_NAME_LENGTH = 250;

    public static Author parseAuthorName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new Author("", "");
        }

        fullName = fullName.trim();
        logger.fine("Parsing author name: " + fullName);

        // Check for comma format (Last, First)
        if (fullName.contains(",")) {
            // If there are multiple commas, this might be a list of authors
            if (fullName.indexOf(",") != fullName.lastIndexOf(",")) {
                // Take just the first author from the list
                String firstAuthor = fullName.substring(0, fullName.indexOf(","));
                logger.warning("Multiple authors detected in single string. Using only the first: " + firstAuthor);
                return parseAuthorName(firstAuthor);
            }
            return parseCommaFormat(fullName);
        }

        // Check for names with suffixes
        Matcher suffixMatcher = SUFFIX_PATTERN.matcher(fullName);
        if (suffixMatcher.matches()) {
            String nameWithoutSuffix = suffixMatcher.group(1);
            String suffix = suffixMatcher.group(2);
            return parseSpaceFormat(nameWithoutSuffix, suffix);
        }

        // Default space format (First Last)
        return parseSpaceFormat(fullName, null);
    }

    public static Set<Author> parseMultipleAuthors(String authorsString) {
        if (authorsString == null || authorsString.trim().isEmpty()) {
            return new HashSet<>();
        }

        Set<Author> authors = new HashSet<>();

        // Check if this is potentially a list of authors (contains multiple commas)
        if (authorsString.indexOf(",") != authorsString.lastIndexOf(",")) {
            String[] authorNames = authorsString.split(",\\s*");
            for (String name : authorNames) {
                if (!name.trim().isEmpty()) {
                    authors.add(parseAuthorName(name.trim()));
                }
            }
        } else {
            // Single author
            authors.add(parseAuthorName(authorsString));
        }

        return authors;
    }

    private static Author parseCommaFormat(String fullName) {
        String[] parts = fullName.split(",\\s*", 2);
        String lastName = truncateIfNeeded(parts[0].trim());
        String firstName = (parts.length > 1) ? truncateIfNeeded(parts[1].trim()) : "";

        return new Author(firstName, lastName);
    }

    private static Author parseSpaceFormat(String fullName, String suffix) {
        // Split by spaces
        String[] parts = fullName.split("\\s+");

        // Handle single name case
        if (parts.length == 1) {
            return new Author("", truncateIfNeeded(parts[0]));
        }

        // For multiple parts, assume last part is last name and everything else is first name
        StringBuilder firstNameBuilder = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            if (i > 0) {
                firstNameBuilder.append(" ");
            }
            firstNameBuilder.append(parts[i]);
        }

        String firstName = truncateIfNeeded(firstNameBuilder.toString());
        String lastName = truncateIfNeeded(parts[parts.length - 1]);

        // Add suffix to lastName if present
        if (suffix != null && !suffix.isEmpty()) {
            lastName = truncateIfNeeded(lastName + " " + suffix);
        }

        return new Author(firstName, lastName);
    }


    private static String truncateIfNeeded(String value) {
        if (value == null) {
            return "";
        }

        if (value.length() > MAX_NAME_LENGTH) {
            logger.warning("Truncating author name component that exceeds " + MAX_NAME_LENGTH +
                    " characters: " + value.substring(0, 20) + "...");
            return value.substring(0, MAX_NAME_LENGTH);
        }

        return value;
    }

}