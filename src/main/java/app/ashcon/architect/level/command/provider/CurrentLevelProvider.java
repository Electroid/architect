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

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Singleton
public class CurrentLevelProvider implements BukkitProvider<Level> {

    private final LevelStore levelStore;
    private final Cache<String, String> levelIds;

    @Inject CurrentLevelProvider(LevelStore levelStore) {
        this.levelStore = levelStore;
        this.levelIds = CacheBuilder.newBuilder()
                                    .expireAfterWrite(1, TimeUnit.HOURS)
                                    .initialCapacity(16)
                                    .build();
    }

    @Override
    public boolean isProvided() {
        return true;
    }

    @Nullable
    @Override
    public Level get(CommandSender sender, CommandArgs args, List<? extends Annotation> mods) throws ArgumentException, ProvisionException {
        final String levelId;
        if(sender instanceof Physical) {
            levelId = ((Physical) sender).getWorld().getName();
        } else {
            levelId = levelIds.getIfPresent(sender.getName());
        }
        final Level level = levelStore.find(levelId)
                                      .orElseThrow(() -> new ArgumentException("Could not find your current level, try teleporting to a level"));
        final Role role = getRole(mods);
        if(level.hasRole(role, sender)) {
            return level;
        } else {
            throw new ArgumentException("You must have at least the " + role.name().toLowerCase() + " role to use this command");
        }
    }

    public void teleport(CommandSender sender, Level level) {
        if(!(sender instanceof Physical)) {
            levelIds.put(sender.getName(), level.getId());
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
