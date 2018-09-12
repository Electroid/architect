package app.ashcon.architect.level;

import app.ashcon.architect.level.type.Flag;
import app.ashcon.architect.level.type.Role;
import app.ashcon.architect.level.type.Visibility;
import app.ashcon.architect.model.Model;
import com.google.gson.annotations.SerializedName;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a player-defined game level.
 *
 * Contains a weak reference to a {@link World},
 * indexed by the {@link #id}, which should be the
 * same as {@link World#getUID()}.
 *
 * All fields with the exception of {@link #id} are mutable,
 * and can be modified by a player with {@link Role#EDITOR}
 * or higher permissions.
 */
public class Level implements Model {

    @SerializedName("_id")        final String id;
    @SerializedName("name")       String name;
    @SerializedName("visibility") Visibility visibility;
    @SerializedName("roles")      Map<String, Role> roles;
    @SerializedName("spawn")      Vector spawn;
    @SerializedName("flags")      EnumSet<Flag> flags;
    @SerializedName("locked")     Boolean locked;

    public Level(String id,
          String name,
          Visibility visibility,
          Map<String, Role> roles,
          Vector spawn,
          EnumSet<Flag> flags,
          Boolean locked) {
        this.id = id;
        setName(name);
        setVisibility(visibility);
        setRoles(roles);
        setSpawn(spawn);
        setFlags(flags);
        setLocked(locked);
    }

    public Level(String name, String playerId) {
        this(UUID.randomUUID().toString(), name, null, null, null, null, null);
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
            throw new IllegalArgumentException("Level name that is too short");
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
        // TODO
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
     * Ensure the {@link World} reference is loaded.
     *
     * @return The world reference.
     */
    public World loadWorld() {
        World world = tryWorld();
        if(world == null) {
            WorldCreator creator = Bukkit.detectWorld(getId());
            if(creator == null) {
                creator = new WorldCreator(getId())
                    .environment(World.Environment.NORMAL)
                    .type(WorldType.FLAT)
                    .generateStructures(false)
                    .hardcore(false)
                    .seed(getId().hashCode());
            }
            world = creator.generator(new ChunkGenerator() {
                @Override
                public byte[] generate(World world, Random random, int x, int z) {
                    return new byte[Short.MAX_VALUE];
                }
            }).createWorld();
            world.setAutoSave(false);
            world.setKeepSpawnInMemory(false);
            world.getBlockAt(getSpawn()).getRelative(BlockFace.DOWN).setType(Material.BEDROCK);
        }
        return world;
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
     * Get whether the level is not locked.
     *
     * @see #isLocked()
     * @return Whether the level is not locked.
     */
    public boolean isUnlocked() {
        return !isLocked();
    }

    /**
     * Set whether the level is locked.
     *
     * @param locked Whether the level should be locked.
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
        commit("locked");
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
     * @return The role of a player, or null if they don't have one.
     */
    public @Nullable Role getRoleExplicitly(@Nullable String playerId) {
        return getRoles().get(playerId);
    }

    /**
     * Check if a player has a role in the level.
     *
     * @param role The role to ok that they have.
     * @param playerId The UUID of the player to query their role.
     * @return Whether the player has that role.
     */
    public boolean hasRoleExplicitly(Role role, @Nullable String playerId) {
        final Role query = getRoleExplicitly(playerId);
        return query != null && role.ordinal() <= query.ordinal();
    }

    public boolean hasRole(Role role, CommandSender sender) {
        if(sender instanceof ConsoleCommandSender) {
            return true;
        } else if(sender instanceof Player) {
            final Player player = (Player) sender;
            if(player.isOp()) {
                return true;
            } else if(role == Role.VIEWER
                      && visibility != Visibility.PRIVATE) {
                return true;
            } else {
                return hasRoleExplicitly(role, player.getUniqueId().toString());
            }
        }
        return false;
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
        getRoles().put(playerId, role == null ? Role.VIEWER : role);
        commit("roles");
    }

    /**
     * Remove a player's explicit role from the level.
     *
     * @param playerId The UUID of the player to remove.
     * @throws IllegalArgumentException If the operation removes the last owner.
     */
    public void removePlayer(String playerId) throws IllegalArgumentException {
        if(getRoles().entrySet().stream().anyMatch(entry -> !entry.getKey().equals(playerId) && entry.getValue() == Role.OWNER)) {
            throw new IllegalArgumentException("Level must have at least 1 owner");
        }
        if(getRoles().remove(playerId) != null) {
            commit("roles");
        }
        if(getRoles().isEmpty()) {
            setRoles(null);
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
               + ", spawn=" + getSpawn()
               + ", visibility=" + getVisibility()
               + ", locked=" + isLocked()
               + ", roles=" + getRoles() + "}";
    }

    public static class Lobby extends Level {

        public Lobby() {
            super(
                "world",
                "Lobby",
                Visibility.PUBLIC,
                null,
                null,
                EnumSet.allOf(Flag.class),
                true
            );
        }

        @Override
        public synchronized void commit(String... fields) {}

        @Override
        public World tryWorld() {
            return Bukkit.getWorlds().get(0);
        }

        @Override
        public boolean hasRole(Role role, CommandSender sender) {
            return role == Role.VIEWER;
        }

        @Override
        public boolean hasRoleExplicitly(Role role, @Nullable String playerId) {
            return role == Role.VIEWER;
        }

        @Override
        public Role getRoleExplicitly(@Nullable String playerId) {
            return Role.VIEWER;
        }

    }

}
