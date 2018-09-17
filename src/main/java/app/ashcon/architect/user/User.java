package app.ashcon.architect.user;

import app.ashcon.architect.level.Level;
import app.ashcon.architect.level.type.Role;
import app.ashcon.architect.model.Model;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Represents a persistent player that has a unique ID and a cached username.
 */
public class User implements Model {

    private @SerializedName("_id")      UUID id;
    private @SerializedName("username") String username;
    private @SerializedName("level_id") String levelId;

    public User(UUID id, String username, @Nullable String levelId) {
        this.id = id;
        this.username = username;
        this.levelId = levelId;
    }

    @Override
    public String getId() {
        if(id == null) {
            id = UUID.randomUUID();
        }
        return id.toString();
    }

    @Override
    public String getName() {
        if(username == null) {
            username = getId();
        }
        return username;
    }

    @Override
    public void setName(String username) throws IllegalArgumentException {
        if(username == null) {
            throw new IllegalArgumentException("User '" + getId() + "' cannot have a null username");
        }
        this.username = username;
    }

    /**
     * Try to get the {@link Player} reference.
     *
     * @return The nullable player reference.
     */
    public @Nullable Player tryPlayer() {
        return Bukkit.getPlayer(UUID.fromString(getId()));
    }

    /**
     * Get the {@link Player} reference, or else
     * throw an {@link IllegalStateException} if offline.
     *
     * @return The player reference.
     */
    public Player needPlayer() {
        if(isOnline()) {
            return tryPlayer();
        } else {
            throw new IllegalStateException("User '" + getName() + "' is not online");
        }
    }

    /**
     * Check whether the {@link Player} is online,
     * or is just logging in.
     *
     * @return Whether the player is online.
     */
    public boolean isOnline() {
        Player player = tryPlayer();
        return player != null && player.willBeOnline();
    }

    /**
     * Try to get the ID of the {@link Level},
     * the player is currently in or was last seen in.
     *
     * @return The ID of the {@link Level} reference.
     */
    public @Nullable String tryLevelId() {
        return levelId;
    }

    /**
     * Set or clear the current {@link Level} for the user.
     *
     * @param level The level, or null to clear.
     * @return Whether the level was changed.
     */
    public boolean setLevel(@Nullable Level level) {
        if(level == null) {
            levelId = null;
        } else if(isOnline() && level.hasRole(Role.VIEWER, needPlayer())) {
            levelId = level.getId();
        } else {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof User) {
            return getId().equals(((User) obj).getId());
        }
        return false;
    }

    @Override
    public String toString() {
        return "User{id=" + getId()
                + ", username=" + getName()
                + ", levelId=" + tryLevelId() + "}";
    }

}
