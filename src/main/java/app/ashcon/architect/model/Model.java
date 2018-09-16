package app.ashcon.architect.model;

/**
 * Represents an object with a unique ID and mutable name.
 */
public interface Model {

    /**
     * Get the unique ID of the model.
     *
     * @return A unique ID.
     */
    String getId();

    /**
     * Get the name of the model, which could change.
     *
     * @return The name of the model.
     */
    String getName();

    /**
     * Set the name of the model, or throw an exception
     * if the operation is not allowed or request is malformed.
     *
     * @param name The new name of the model.
     * @throws IllegalArgumentException If an error occurs.
     */
    void setName(String name) throws IllegalArgumentException;

}
