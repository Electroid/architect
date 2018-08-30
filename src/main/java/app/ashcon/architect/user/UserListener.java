package app.ashcon.architect.user;

import app.ashcon.architect.level.Level;
import app.ashcon.architect.level.LevelStore;
import org.bukkit.Physical;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Predicate;

public class UserListener {

    final UserStore users;
    final LevelStore levels;

    @Inject UserListener(UserStore users, LevelStore levels) {
        this.users = users;
        this.levels = levels;
    }
/*
    public Optional<Level> tryLevel(Physical physical) {
        return levels.cache(physical.getWorld().getName());
    }

    public Optional<Level> tryLevel(Physical physical, Predicate<Level> test) {
        return tryLevel(physical).filter(test);
    }

    public Level needLevel(Physical physical, Predicate<Level> test) {
        return tryLevel(physical, test).orElse(hub);
    }

    @EventHandler(ignoreCancelled = true)
    void onPreJoin(AsyncPlayerPreLoginEvent event) {
        if(event.getLoginResult() == AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            users.login(event.getUniqueId().toString(), event.getName())
                 .thenApplyAsync(user -> levels.cache(user.tryLevelId()))
                 //.thenApplyAsync(level -> level.map(Level::needWorld))
                 .join();
        }
    }

    @EventHandler(ignoreCancelled = true)
    void onJoin(PlayerJoinEvent event) {

        /*Optional<User> result = users.cache(event.getPlayer().getUniqueId().toString());
        if(result.isPresent()) {
            String levelId = result.get().tryLevelId();
            Level level;
            if(levelId == null) {
                level = hub;
            } else {
                level = levels.cache(levelId);
            }
        }*/
        /*
        AnaxWorld world = AnaxWorldManagement.getInstance().getWorld(event.getPlayer().getWorld().getName());
        AnaxPlayerManager.getInstance().addPlayer(event.getPlayer());
        Bukkit.getServer().getPluginManager().callEvent(new PermissionChangedEvent(event.getPlayer(), world));
        */
    /*}

    @EventHandler(ignoreCancelled = true)
    void onQuit(PlayerQuitEvent event) {
    }

    @EventHandler(ignoreCancelled = true)
    void onKick(PlayerKickEvent event) {
    }

    @EventHandler(ignoreCancelled = true)
    void onWorldChange(PlayerChangedWorldEvent event) {
    }

    @EventHandler(ignoreCancelled = true)
    void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Optional<Level> level = tryLevel(event.getTo(), lvl -> lvl.canView(player));
        if(!level.isPresent()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Level level = needLevel(event, lvl -> lvl.canView(player));
        player.setBedSpawnLocation(level.getSpawn().toLocation(level.needWorld()));
    }
    */

}
