package app.ashcon.architect.level;

import app.ashcon.architect.level.type.Flag;
import app.ashcon.architect.level.type.Role;
import app.ashcon.architect.level.type.Visibility;
import app.ashcon.architect.model.Model;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Represents a player-defined game level.
 */
public class Level implements Model {

    private @SerializedName("_id")        final String id;
    private @SerializedName("name")       String name;
    private @SerializedName("visibility") Visibility visibility;
    private @SerializedName("roles")      Map<String, Role> roles;
    private @SerializedName("spawn")      Vector spawn;
    private @SerializedName("flags")      EnumSet<Flag> flags;
    private @SerializedName("locked")     Boolean locked;
    private @SerializedName("default")    Boolean def;

    public Level(String id,
                 String name,
                 Visibility visibility,
                 Map<String, Role> roles,
                 Vector spawn,
                 EnumSet<Flag> flags,
                 Boolean locked,
                 Boolean def) {
        this.id = id;
        setName(name);
        setVisibility(visibility);
        setRoles(roles);
        setSpawn(spawn);
        setFlags(flags);
        setLocked(locked);
        setDefault(def);
    }

    public Level(String name, String playerId) {
        this(Long.toString(System.nanoTime()), name, null, null, null, null, null, null);
        addPlayer(playerId, Role.OWNER);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        if(name == null) {
            name = "unknown-" + getId();
        }
        return name;
    }

    @Override
    public void setName(String name) throws IllegalArgumentException {
        if(name == null) {
            throw new IllegalArgumentException("Level name cannot change from '" + getName() + "' to null");
        } else if(name.length() > 16) {
            throw new IllegalArgumentException("Level name is too long");
        } else if(name.length() < 4) {
            throw new IllegalArgumentException("Level name is too short");
        } else if(!name.matches("[A-Za-z0-9-]*")) {
            throw new IllegalArgumentException("Level name can only include letters, numbers, and dashes");
        }
        this.name = name;
        commit("name");
    }

    /**
     * Commit the current {@link Level} to the database.
     *
     * @param fields The fields that changed.
     */
    public synchronized void commit(String... fields) {
        // TODO(ashcon): Instead of sending full update, send partial update
    }

    /**
     * Try to get the {@link World} reference, or null if
     * it is not currently loaded.
     *
     * @return The world reference or null.
     */
    public @Nullable World tryWorld() {
        return Bukkit.getWorld(getId());
    }

    /**
     * Get the {@link World} reference or throw a state exception
     * if it is not currently loaded.
     *
     * @throws IllegalStateException If the world is not loaded.
     * @return The world reference.
     */
    public World needWorld() {
        if(isLoaded()) {
            return tryWorld();
        } else {
            throw new IllegalStateException("Level '" + getName() + "' is not loaded!");
        }
    }

    /**
     * Get whether the {@link World} reference is currently loaded.
     *
     * @return Whether the world is loaded.
     */
    public boolean isLoaded() {
        return tryWorld() != null;
    }

    /**
     * Get whether the level is locked.
     *
     * Players cannot modify the environment
     * of the level until it is unlocked.
     *
     * Additionally, the {@link World} is not saved to
     * avoid transient changes from being committed to disk.
     *
     * @return Whether the level is locked.
     */
    public boolean isLocked() {
        return locked != null && locked;
    }

    /**
     * Set whether the level is locked.
     *
     * @param locked Whether the level should be locked.
     */
    public void setLocked(Boolean locked) {
        this.locked = locked != null && locked ? locked : null;
        commit("locked");
    }

    /**
     * Get whether the level is the default level
     * when players join the server.
     *
     * @return Whether the level is the default level.
     */
    public boolean isDefault() {
        return def != null && def;
    }

    /**
     * Set whether the level is the default level.
     *
     * There can only be one default level, so be sure
     * that there are no others levels with this quantifier.
     *
     * @param def Whether the level is the default level.
     */
    public void setDefault(Boolean def) {
        this.def = def != null && def ? def : null;
        commit("default");
    }

    /**
     * Get the visibility of the level.
     *
     * @see Visibility
     * @return The current visibility of the level.
     */
    public Visibility getVisibility() {
        if(visibility == null) {
            visibility = Visibility.PRIVATE;
        }
        return visibility;
    }

    /**
     * Set the visibility of the level.
     *
     * @param visibility The new visibility of the level.
     */
    public void setVisibility(@Nullable Visibility visibility) {
        this.visibility = visibility;
        commit("visibility");
    }

    /**
     * Get a map of players to their role in the level.
     *
     * @return The map of players to their role.
     */
    public Map<String, Role> getRoles() {
        if(roles == null) {
            roles = new HashMap<>();
        }
        return roles;
    }

    /**
     * Set the map of players to their role in the level.
     *
     * @param roles The map of players to their role.
     */
    public void setRoles(@Nullable Map<String, Role> roles) {
        this.roles = roles;
        commit("roles");
    }

    /**
     * Get the role of a player in the level.
     *
     * @param playerId The UUID of the player to query their role.
     * @return The role of a player.
     */
    public Role getRoleExplicitly(@Nullable String playerId) {
        return getRoles().getOrDefault(playerId, Role.NONE);
    }

    /**
     * Get the implicit role of a command sender in the level.
     *
     * @param sender The command sender to query their role.
     * @return The role of the command sender.
     */
    public Role getRole(CommandSender sender) {
        if(isDefault()) {
            return Role.VIEWER;
        } else if(sender instanceof ConsoleCommandSender) {
            return Role.OWNER;
        } else if(sender instanceof Player) {
            final Player player = (Player) sender;
            if(player.isOp() || player.hasPermission("architect.owner")) {
                return Role.OWNER;
            } else {
                Role role = getRoleExplicitly(player.getUniqueId().toString());
                if(role == null && visibility != Visibility.PRIVATE) {
                    role = Role.VIEWER;
                }
                return role;
            }
        } else {
            return Role.NONE;
        }
    }

    /**
     * Check if a command sender has an implicit role in the level.
     *
     * @param role The role to query that they have.
     * @param sender The command sender to query their role.
     * @return Whether the command sender has that role, or a higher role.
     */
    public boolean hasRole(Role role, CommandSender sender) {
        final Role actual = getRole(sender);
        return actual.ordinal() <= role.ordinal();
    }

    /**
     * Get the flags that are enabled in the level.
     *
     * Any flags that are excluded from the set
     * are assumed to be disabled.
     *
     * @return The set of flags that are enabled.
     */
    public EnumSet<Flag> getFlags() {
        if(flags == null) {
            flags = EnumSet.noneOf(Flag.class);
        }
        return flags;
    }

    /**
     * Set the flags that are enabled in the level.
     *
     * @param flags The set of flags that are enabled.
     */
    public void setFlags(EnumSet<Flag> flags) {
        this.flags = flags;
        commit("flags");
    }

    /**
     * Check whether the level has a flag enabled.
     *
     * @param flag The flag to query.
     * @return Whether the flag is enabled.
     */
    public boolean hasFlag(@Nullable Flag flag) {
        return getFlags().contains(flag);
    }

    /**
     * Add or remove a flag from the level.
     *
     * @param flag The flag to add or remove.
     * @param add If true, add the flag, otherwise, remove the flag.
     */
    public void setFlag(@Nullable Flag flag, boolean add) {
        if(flag == null) {
            return;
        } else if(add) {
            flags.add(flag);
        } else {
            flags.remove(flag);
        }
        commit("flags");
    }

    /**
     * Get the initial spawn vector of the level.
     *
     * @return The initial spawn.
     */
    public Vector getSpawn() {
        if(spawn == null) {
            spawn = new Vector(0.5, 1.5, 0.5);
        }
        return spawn;
    }

    /**
     * Set the initial spawn vector of the level.
     *
     * @param spawn The new initial spawn.
     */
    public void setSpawn(Vector spawn) {
        this.spawn = spawn;
        commit("spawn");
    }

    /**
     * Get the players that have explicit roles in the level.
     *
     * @see #getRoles()
     * @return The players with explicit roles.
     */
    public Set<String> getPlayers() {
        return getRoles().keySet();
    }

    /**
     * Add a player with an explicit role to the level.
     *
     * @param playerId The UUID of the player to add.
     * @param role The role to give the player.
     */
    public void addPlayer(String playerId, @Nullable Role role) {
        if(role == null || role == Role.NONE) {
            removePlayer(playerId);
        } else {
            if(role != Role.OWNER) validatePlayer(playerId);
            getRoles().put(playerId, role);
            commit("roles");
        }
    }

    /**
     * Remove a player's explicit role from the level.
     *
     * @param playerId The UUID of the player to remove.
     * @throws IllegalArgumentException If the operation removes the last owner.
     */
    public void removePlayer(String playerId) throws IllegalArgumentException {
        validatePlayer(playerId);
        if(getRoles().remove(playerId) != null) {
            commit("roles");
        }
        if(getRoles().isEmpty()) {
            setRoles(null);
        }
    }

    /**
     * Ensure that there is always one owner membership.
     *
     * @param playerId The UUID of the player to check.
     */
    private void validatePlayer(String playerId) {
        if(getRoles().entrySet().stream().anyMatch(entry -> !entry.getKey().equals(playerId) && entry.getValue() == Role.OWNER)) {
            throw new IllegalArgumentException("Level must have at least one owner");
        }
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Level) {
            return getId().equals(((Level) obj).getId());
        }
        return false;
    }

    @Override
    public String toString() {
        return "Level{id=" + getId()
               + ", name=" + getName()
               + ", loaded=" + isLoaded()
               + ", locked=" + isLocked()
               + ", default=" + isDefault()
               + ", spawn=" + getSpawn()
               + ", visibility=" + getVisibility()
               + ", roles=" + getRoles() + "}";
    }

    /**
     * Creates the default level configuration.
     *
     * @return The default level.
     */
    public static Level createDefault() {
        return new Level(Bukkit.getWorlds().get(0).getName(), "Lobby", Visibility.PUBLIC, null, null, EnumSet.allOf(Flag.class), true, true);
    }

}
