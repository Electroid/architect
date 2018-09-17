package app.ashcon.architect.user;

import app.ashcon.architect.level.Level;
import app.ashcon.architect.level.LevelLoader;
import app.ashcon.architect.level.LevelStore;
import app.ashcon.architect.level.type.Role;
import dagger.Reusable;
import org.bukkit.Physical;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Ensures that {@link User}s are sandboxed in their proper {@link Level}s.
 */
@Reusable
public class UserListener implements Listener {

    private final UserStore userStore;
    private final LevelStore levelStore;
    private final LevelLoader levelLoader;

    @Inject UserListener(UserStore userStore, LevelStore levelStore, LevelLoader levelLoader) {
        this.userStore = userStore;
        this.levelStore = levelStore;
        this.levelLoader = levelLoader;
    }

    public Optional<Level> tryLevel(Physical physical) {
        return levelStore.findCached(physical.getWorld().getName());
    }

    @EventHandler(ignoreCancelled = true)
    void join(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final User user = userStore.login(player.getUniqueId().toString(), player.getName());
        final Level level = levelStore.findCached(user.tryLevelId())
                                      .filter(lvl -> lvl.hasRole(Role.VIEWER, player))
                                      .orElse(levelStore.fallback());
        levelLoader.load(level);
        player.teleport(level.getSpawnLocation());
    }

    @EventHandler(ignoreCancelled = true)
    void quit(final PlayerQuitEvent event) {
        tryLevel(event.getPlayer()).ifPresent(level -> {
            if(level.needWorld().getPlayerCount() <= 1) {
                levelLoader.unload(level);
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    void teleport(final PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        tryLevel(event.getTo()).ifPresent(level -> {
            if(!level.hasRole(Role.VIEWER, player)) {
                event.setCancelled(true);
                player.sendMessage("You do not have permission to teleport to the '" + level.getName() + "' level");
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    void death(final PlayerDeathEvent event) {
        final Player player = event.getEntity();
        tryLevel(player).ifPresent(level -> player.setBedSpawnLocation(level.getSpawnLocation()));
    }

    @EventHandler(ignoreCancelled = true)
    void changedWorld(final PlayerChangedWorldEvent event) {
        userStore.find(event.getPlayer().getUniqueId().toString()).ifPresent(user -> {
            tryLevel(event.getWorld()).ifPresent(level -> {
                user.setLevel(level);
                userStore.update(user);
            });
            tryLevel(event.getFrom()).ifPresent(level -> {
                if(level.needWorld().getPlayerCount() <= 0) {
                    levelLoader.unload(level);
                }
            });
        });
    }

}
