package app.ashcon.architect.util.conversion;

import org.bson.Document;

/**
 * Converts between {@link Document}s and {@link Object}s.
 *
 * @param <T> The type of {@link Object} to convert.
 */
public interface Conversion<T> {

    /**
     * Convert a Java object into a BSON document.
     *
     * @param object The plain Java object.
     * @return The BSON document.
     */
    Document toDocument(T object);

    /**
     * Convert a BSON document into a Java object.
     *
     * @param document The BSON document.
     * @return The plain Java object.
     */
    T toObject(Document document);

}
