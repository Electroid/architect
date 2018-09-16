package app.ashcon.architect.user;

import app.ashcon.architect.level.Level;
import app.ashcon.architect.level.LevelLoader;
import app.ashcon.architect.level.LevelStore;
import app.ashcon.architect.level.type.Role;
import org.bukkit.Physical;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Ensures that {@link User}s are sandboxed in their proper {@link Level}s.
 */
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
    void onPreJoin(final AsyncPlayerPreLoginEvent event) {
        /*if(event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            final User user = userStore.login(event.getUniqueId().toString(), event.getName());
            final Level level = levelCache.find(user.tryLevelId()).orElse(levelStore.fallback());
        }*/
    }

    @EventHandler(ignoreCancelled = true)
    void onJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final User user = userStore.login(player.getUniqueId().toString(), player.getName());
        final Level level = levelStore.findCached(user.tryLevelId())
                                      .filter(lvl -> lvl.hasRole(Role.VIEWER, player))
                                      .orElse(levelStore.fallback());
        levelLoader.load(level);
    }

    @EventHandler(ignoreCancelled = true)
    void onTeleport(final PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        tryLevel(event.getTo()).ifPresent(level -> {
            if(!level.hasRole(Role.VIEWER, player)) {
                event.setCancelled(true);
                player.sendMessage("You do not have permission to setContext to the '" + level.getName() + "' level");
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    void onPlayerDeath(final PlayerDeathEvent event) {
        final Player player = event.getEntity();
        tryLevel(player).ifPresent(level -> {
            player.setBedSpawnLocation(level.getSpawn().toLocation(level.needWorld()));
        });
    }

    @EventHandler(ignoreCancelled = true)
    void onPlayerChangedWorld(final PlayerChangedWorldEvent event) {
        userStore.find(event.getPlayer().getUniqueId().toString()).ifPresent(user -> {
            tryLevel(event.getWorld()).ifPresent(level -> {
                user.setLevel(level);
                userStore.update(user);
            });
        });
    }

}
