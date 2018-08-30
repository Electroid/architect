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
     * @return A future, optional model.
     */
    Optional<T> find(String id);

    /**
     * Search for a model by its name.
     *
     * @param name The model name.
     * @return A future search model response.
     */
    List<T> search(String name);

    /**
     * Update or create a model to the store.
     *
     * @param model The model.
     * @return A future, updated model.
     */
    T update(T model);

    /**
     * Delete a model from the store.
     *
     * @param id The model ID to delete.
     * @return A future when the model is deleted.
     */
    void delete(String id);

}
