package app.ashcon.architect.level.command;

import app.ashcon.architect.level.Level;
import app.ashcon.architect.level.LevelLoader;
import app.ashcon.architect.level.LevelStore;
import app.ashcon.architect.level.command.annotation.Current;
import app.ashcon.architect.level.command.annotation.Require;
import app.ashcon.architect.level.command.provider.LevelCurrentProvider;
import app.ashcon.architect.level.type.Action;
import app.ashcon.architect.level.type.Flag;
import app.ashcon.architect.level.type.Role;
import app.ashcon.architect.level.type.Status;
import app.ashcon.intake.Command;
import app.ashcon.intake.bukkit.parametric.annotation.Sender;
import app.ashcon.intake.group.At;
import app.ashcon.intake.group.Group;
import app.ashcon.intake.parametric.annotation.Switch;
import dagger.Reusable;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents user-commands to interact with {@link Level}s.
 */
// TODO(ashcon): Add list of levels
// TODO(ashcon): Fix intake public-only methods
@Reusable
@Group({@At("level"), @At("lvl")})
public class LevelCommands {

    private final LevelStore levelStore;
    private final LevelLoader levelLoader;
    private final LevelCurrentProvider levelProvider;

    @Inject LevelCommands(LevelStore levelStore, LevelLoader levelLoader, LevelCurrentProvider levelProvider) {
        this.levelStore = levelStore;
        this.levelLoader = levelLoader;
        this.levelProvider = levelProvider;
    }

    @Command(
        aliases = {"name"},
        desc = "Get or set the name of the level"
    )
    public void name(CommandSender sender, @Current Level level, @Nullable String name) {
        update(sender, level, "name", Level::getName, Level::setName, name, Role.OWNER);
    }

    @Command(
        aliases = {"lock"},
        desc = "Get or set the lock state of the level",
        usage = "[true|false]"
    )
    public void lock(CommandSender sender, @Current Level level, @Nullable Boolean on) {
        update(sender, level, "lock", Level::isLocked, (lvl, yes) -> {
            if(yes) levelLoader.save(lvl);
            lvl.setLocked(yes);
        }, on, Role.OWNER);
    }

    @Command(
        aliases = {"spawn"},
        desc = "Get or set the spawn of the level",
        usage = "[x,y,z]"
    )
    public void spawn(CommandSender sender, @Current Level level, @Nullable Vector spawn) {
        update(sender, level, "spawn", Level::getSpawn, Level::setSpawn, spawn, Role.EDITOR);
    }

    @Command(
        aliases = {"status"},
        desc = "Get or set the status of the level",
        usage = "[public|unlisted|private]"
    )
    public void status(CommandSender sender, @Current Level level, @Nullable Status status) {
        update(sender, level, "status", Level::getStatus, Level::setStatus, status, Role.EDITOR);
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
        desc = "Get or modify a membership for the level",
        usage = "<player> [promote|demote]"
    )
    public void member(CommandSender sender, @Current Level level, Player player, @Nullable Action action) {
        final Role current = level.getRole(player);
        final Role next;
        if(action == Action.PROMOTE) {
            next = current.getParent();
        } else if(action == Action.DEMOTE) {
            next = current.getChild();
        } else {
            next = null;
        }
        final Role require = next == null ? Role.VIEWER : next.getParent();
        update(sender, level, "membership for " + player.getDisplayName(sender), lvl -> lvl.getRole(player), (lvl, rol) -> lvl.setRole(player, rol), next, require);
    }

    @Command(
        aliases = {"delete"},
        desc = "Delete the level forever"
    )
    public void delete(CommandSender sender, @Current @Require(Role.OWNER) Level level, @Switch('c') boolean confirm) {
        if(!confirm) {
            throw new IllegalArgumentException("Confirm you want to delete the level with the '-c' flag");
        }
        levelLoader.unload(level);
        levelStore.delete(level.getId());
    }

    @Command(
        aliases = {"create"},
        desc = "Create a new level",
        usage = "<name>"
    )
    public void create(@Sender Player sender, String name) {
        final boolean exists = levelStore.search(name).stream().anyMatch(lvl -> lvl.getName().equalsIgnoreCase(name));
        if(exists) {
            throw new IllegalArgumentException("That level name is already taken");
        }
        final Level level = levelStore.create(name, sender.getUniqueId().toString());
        teleport(sender, level);
    }

    @Command(
        aliases = {"tp"},
        desc = "Teleport to another level"
    )
    public void teleport(CommandSender sender, @Require(Role.VIEWER) Level level) {
        levelLoader.load(level);
        if(sender instanceof Player) {
            final Player player = (Player) sender;
            if(!level.needWorld().equals(player.getWorld())) {
                player.teleport(level.getSpawnLocation());
            }
        } else {
            levelProvider.setContext(sender, level);
        }
        sender.sendMessage(ChatColor.YELLOW + "Teleported you to " + level.getName());
    }

    private <T> void update(CommandSender sender, Level level, String name, Function<Level, T> getter, BiConsumer<Level, T> setter, T value, Role role) {
        final T oldValue = getter.apply(level);
        final boolean isSame = Objects.equals(oldValue, value);
        final boolean isEdit = value != null;
        final boolean canEdit = level.hasRole(role, sender);
        final boolean canView = level.hasRole(Role.VIEWER, sender);
        update(
            sender,
            () -> (isEdit && canEdit && !isSame) || (!isEdit && canView),
            () -> {
                if(value != null && canEdit) {
                    setter.accept(level, value);
                    levelStore.update(level);
                }
            },
            () -> {
                if(value == null) {
                    return "The " + name + " is currently set to " + oldValue;
                } else {
                    return "You changed the " + name + " from " + oldValue + " to " + value;
                }
            },
            () -> {
                if(!canView) {
                    return "You need the " + Role.VIEWER + " role to edit the " + name;
                } else if(!canEdit) {
                    return "You need the " + role + " role to edit the " + name;
                } else {
                    return "The " + name + " already is set to " + oldValue;
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

}
