package app.ashcon.architect.level.command;

import app.ashcon.architect.level.Level;
import app.ashcon.architect.level.LevelStore;
import app.ashcon.architect.level.command.annotation.Current;
import app.ashcon.architect.level.command.annotation.Require;
import app.ashcon.architect.level.command.provider.CurrentLevelProvider;
import app.ashcon.architect.level.type.Action;
import app.ashcon.architect.level.type.Flag;
import app.ashcon.architect.level.type.Role;
import app.ashcon.architect.level.type.Visibility;
import app.ashcon.intake.Command;
import app.ashcon.intake.group.At;
import app.ashcon.intake.group.Group;
import app.ashcon.intake.parametric.annotation.Switch;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Singleton
@Group({@At("level"), @At("lvl")})
// TODO: List and search
public class LevelCommands {

    private final LevelStore levelStore;
    private final CurrentLevelProvider levelProvider;

    @Inject LevelCommands(LevelStore levelStore, CurrentLevelProvider levelProvider) {
        this.levelStore = levelStore;
        this.levelProvider = levelProvider;
    }

    private <T> void update(CommandSender sender, Level level, String name, Function<Level, T> getter, BiConsumer<Level, T> setter, T value, Role role) {
        final T oldValue = getter.apply(level);
        final boolean isSame = Objects.equals(oldValue, value);
        final boolean isEdit = value != null;
        final boolean canEdit = level.hasRole(role, sender);
        final boolean canView = level.hasRole(Role.VIEWER, sender);
        update(
            sender,
            () -> canView || (isEdit && (canEdit || !isSame)),
            () -> {
                if(value != null) {
                    setter.accept(level, value);
                    levelStore.update(level);
                }
            },
            () -> {
                if(value == null) {
                    return "Level " + name + " is " + oldValue;
                } else {
                    return "Level " + name + " changed from " + oldValue + " to " + value;
                }
            },
            () -> {
                if(!canView) {
                    return "Level " + name + " requires the " + Role.VIEWER + " role";
                } else if(!canEdit) {
                    return "Level " + name + " requires the " + role + " role";
                } else {
                    return "Level " + name + " already is " + oldValue;
                }
            }
        );
    }

    private void update(CommandSender sender, Supplier<Boolean> filter, Runnable update, Supplier<String> yes, Supplier<String> no) {
        final String response;
        if(filter.get()) {
            update.run();
            response = ChatColor.YELLOW + yes.get();
        } else {
            response = ChatColor.RED + no.get();
        }
        sender.sendMessage(response);
    }

    @Command(
        aliases = {"lock"},
        desc = "Prevent further changes in the level",
        usage = "<true|false>"
    )
    public void lock(CommandSender sender, @Current Level level, @Nullable Boolean on) {
        update(sender, level, "lock", Level::isLocked, Level::setLocked, on, Role.OWNER);
    }

    @Command(
        aliases = {"spawn"},
        desc = "Get or set the spawn of the level"
    )
    public void spawn(CommandSender sender, @Current Level level) {
        update(sender, level, "spawn", Level::getSpawn, Level::setSpawn, null, Role.EDITOR); // TODO
    }

    @Command(
        aliases = {"name"},
        desc = "Get or set the name of the level"
    )
    public void name(CommandSender sender, @Current Level level, @Nullable String name) {
        update(sender, level, "name", Level::getName, Level::setName, name, Role.OWNER);
    }

    @Command(
        aliases = {"visibility"},
        desc = "Get or set the visibility of the level"
    )
    public void visibility(CommandSender sender, @Current Level level, @Nullable Visibility visibility) {
        update(sender, level, "visibility", Level::getVisibility, Level::setVisibility, visibility, Role.EDITOR);
    }

    @Command(
        aliases = {"flag"},
        desc = "Get or set a flag of the level"
    )
    public void flag(CommandSender sender, @Current Level level, Flag flag, @Nullable Boolean value) {
        update(sender, level, flag.name().toLowerCase() + " flag", lvl -> lvl.hasFlag(flag), (lvl, val) -> lvl.setFlag(flag, val), value, Role.EDITOR);
    }

    @Command(
        aliases = {"member"},
        desc = "Get or modify a membership for the level"
    )
    public void member(CommandSender sender, @Current Level level, Player player, Action action, @Nullable Role role) {
        final String playerId = player.getUniqueId().toString();
        update(sender, level, "membership for " + player.getDisplayName(sender), lvl -> lvl.getRoleExplicitly(playerId), (lvl, rol) -> {
            if(rol == null) return;
            if(lvl.hasRole(rol.up(), sender)) {
                switch(action) {
                    case ADD:
                        lvl.addPlayer(playerId, rol);
                        break;
                    case REMOVE:
                        lvl.removePlayer(playerId);
                        break;
                    case PROMOTE:
                        lvl.addPlayer(playerId, rol.up());
                        break;
                    case DEMOTE:
                        lvl.addPlayer(playerId, rol.down());
                        break;
                }
            } else {
                throw new IllegalArgumentException("You do not have permission to modify the " + rol + " group");
            }
        } , role, Role.EDITOR);
    }

    @Command(
        aliases = {"save"},
        desc = "Save the progress of the level"
    )
    public void save(CommandSender sender, @Current @Require(Role.EDITOR) Level level) {
        final World world = level.needWorld();
        world.save();
        levelStore.upload(level.getId(), world.getWorldFolder());
        sender.sendMessage(ChatColor.YELLOW + "Saved the level with " + world.getLoadedChunks().length + " chunks");
    }

    @Command(
        aliases = {"unload"},
        desc = "Unload the level from the server"
    )
    public void unload(CommandSender sender, @Current @Require(Role.EDITOR) Level level, @Switch('c') boolean confirm) {
        final World world = level.needWorld();
        final int others = world.getPlayerCount() - 1;
        if(others > 0 && !confirm) {
            throw new IllegalArgumentException("Confirm you want to unload the level and kick " + others + " other player" + (others > 1 ? "s" : "") + " with the '-c' flag");
        }
        final Location destination = Bukkit.getWorlds().get(0).getSpawnLocation();
        world.getPlayers().forEach(player -> {
            player.teleport(destination);
            if(!player.equals(sender)) {
                player.sendMessage(ChatColor.RED + "The level you were in was unloaded from the server");
            }
        });
        final int unloaded = world.unloadAllChunks();
        Bukkit.unloadWorld(world, false);
        sender.sendMessage(ChatColor.YELLOW + "Unloaded the level with " + unloaded + " chunks");
    }

    @Command(
        aliases = {"load"},
        desc = "Load the level to the server"
    )
    public void load(CommandSender sender, @Require(Role.VIEWER) Level level) {
        if(level.isLoaded()) {
            sender.sendMessage(ChatColor.RED + "The level you requested is already loaded");
        } else {
            final File folder = new File(Bukkit.getWorldContainer(), level.getId());
            levelStore.download(level.getId(), folder);
            level.loadWorld();
            sender.sendMessage(ChatColor.YELLOW + "Successfully downloaded and loaded the level");
        }
    }

    @Command(
        aliases = {"delete"},
        desc = "Delete the level forever"
    )
    public void delete(CommandSender sender, @Current @Require(Role.OWNER) Level level, @Switch('c') boolean confirm) {
        if(!confirm) {
            throw new IllegalArgumentException("Confirm you want to permanently delete the level with the '-c' flag");
        }
        unload(sender, level, true);
        levelStore.delete(level.getId());
        sender.sendMessage(ChatColor.YELLOW + "Successfully deleted the level '" + level.getName() + "'");
    }

    @Command(
        aliases = {"create"},
        desc = "Create a new level with an owner",
        usage = "<name> [owner]"
    )
    public void create(CommandSender sender, String name, @Nullable Player player) {
        final String owner;
        if(player == null) {
            if(sender instanceof Player) {
                owner = ((Player) sender).getUniqueId().toString();
            } else {
                throw new IllegalArgumentException("You must provide an owner of the level");
            }
        } else {
            owner = player.getUniqueId().toString();
        }
        final Level level = levelStore.create(name, owner);
        level.loadWorld();
        teleport(sender, level);
    }

    @Command(
        aliases = {"teleport", "tp"},
        desc = "Teleport to a level"
    )
    public void teleport(CommandSender sender, @Require(Role.VIEWER) Level level) {
        if(sender instanceof Player) {
            final Player player = (Player) sender;
            if(player.getWorld().equals(level.tryWorld())) {
                throw new IllegalArgumentException("You are already in " + level.getName());
            } else if(!level.isLoaded()) {
                load(sender, level);
            }
            player.teleport(level.getSpawn().toLocation(level.needWorld()));
        } else {
            levelProvider.teleport(sender, level);
        }
        sender.sendMessage(ChatColor.YELLOW + "Teleporting you to " + level.getName());
    }

}
