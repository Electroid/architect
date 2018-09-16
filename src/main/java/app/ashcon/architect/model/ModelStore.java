package app.ashcon.architect.model;

import java.util.List;
import java.util.Optional;

/**
 * Represents a mechanism to query for {@link Model}s from a store.
 */
public interface ModelStore<T extends Model> {

    /**
     * Find a model by its unique ID.
     *
     * @param id The unique model ID.
     * @return An optional model.
     */
    Optional<T> find(String id);

    /**
     * Find a model by its unique ID in cache.
     *
     * @param id The unique model ID.
     * @return An optional model.
     */
    Optional<T> findCached(String id);

    /**
     * Search for a model by its name.
     *
     * @param name The model name.
     * @return A list of models.
     */
    List<T> search(String name);

    /**
     * Update or create a model to the store.
     *
     * @param model The model.
     * @return An updated model.
     */
    T update(T model);

    /**
     * Delete a model from the store.
     *
     * @param id The model ID to delete.
     */
    void delete(String id);

}
