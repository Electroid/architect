package app.ashcon.architect.level;

import app.ashcon.architect.model.ModelStore;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

public interface LevelStore extends ModelStore<Level> {

    /**
     * List the levels that a viewer can visit.
     *
     * @param playerId The ID of the player, or null for console.
     * @param page The current page to query.
     * @param perPage The number of results per page.
     * @return A future paginated level response.
     */
    List<Level> list(@Nullable String playerId, int page, int perPage);

    default List<Level> list(int page, int perPage) {
        return list(null, page, perPage);
    }

    /**
     * Upload the local copy of the {@link Level}
     * and save it to the remote store.
     *
     * @param id The ID of the level to upload.
     * @param source The local source to upload the level.
     * @return Whether the operation was successful.
     */
    boolean upload(String id, File source);

    /**
     * Download the remote copy of the {@link Level}
     * and save it to disk.
     *
     * @param id The ID of the level to download.
     * @param destination The local destination to download the level.
     * @return Whether the operation was successful.
     */
    boolean download(String id, File destination);

    /**
     * Create a new {@link Level} with a name and owner.
     *
     * @param name The level name.
     * @param playerId The initial owner of the level.
     * @throws IllegalArgumentException If there is already a level with that name.
     * @return The new level.
     */
    default Level create(String name, String playerId) throws IllegalArgumentException {
        if(search(name).stream().anyMatch(level -> level.getName().equalsIgnoreCase(name))) {
            throw new IllegalArgumentException("There is already a level named '" + name + "'");
        }
        return update(new Level(name, playerId));
    }

}
