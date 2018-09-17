package app.ashcon.architect.level.command.provider;

import app.ashcon.architect.level.Level;
import app.ashcon.architect.level.LevelStore;
import app.ashcon.architect.level.command.annotation.Require;
import app.ashcon.architect.level.type.Role;
import app.ashcon.intake.argument.ArgumentException;
import app.ashcon.intake.argument.CommandArgs;
import app.ashcon.intake.bukkit.parametric.provider.BukkitProvider;
import app.ashcon.intake.parametric.ProvisionException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.bukkit.Physical;
import org.bukkit.command.CommandSender;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Provides the current {@link Level} for any {@link CommandSender}
 * that is also a {@link Physical} object.
 *
 * Also allows non-{@link Physical}s to manually set their context,
 * so they can interact with a current {@link Level}.
 */
@Singleton
public class LevelCurrentProvider implements BukkitProvider<Level> {

    private final LevelStore levelStore;
    private final Cache<String, String> consoleContext;

    @Inject
    LevelCurrentProvider(LevelStore levelStore) {
        this.levelStore = levelStore;
        this.consoleContext = CacheBuilder.newBuilder()
                                          .expireAfterWrite(1, TimeUnit.HOURS)
                                          .initialCapacity(16)
                                          .build();
    }

    @Override
    public boolean isProvided() {
        return true;
    }

    @Override
    public Level get(CommandSender sender, CommandArgs args, List<? extends Annotation> mods) throws ArgumentException, ProvisionException {
        final String levelId;
        if(sender instanceof Physical) {
            levelId = ((Physical) sender).getWorld().getName();
        } else {
            levelId = consoleContext.getIfPresent(sender.getName());
        }
        final Level level = levelStore.find(levelId)
                                      .orElseThrow(() -> new ArgumentException("Could not find your current level, try teleporting to one"));
        final Role role = getRole(mods);
        if(level.hasRole(role, sender)) {
            return level;
        } else {
            throw new ArgumentException("You need the " + role + " role to use this command");
        }
    }

    /**
     * Sets the current {@link Level} for a non-{@link Physical} object.
     *
     * @param sender Any command sender.
     * @param level The level to set their context to.
     */
    public void setContext(CommandSender sender, Level level) {
        if(!(sender instanceof Physical)) {
            consoleContext.put(sender.getName(), level.getId());
        }
    }

    private Role getRole(List<? extends Annotation> mods) {
        return mods.stream()
                   .filter(mod -> mod instanceof Require)
                   .findFirst()
                   .map(mod -> ((Require) mod).value())
                   .orElse(Role.VIEWER);
    }

}
