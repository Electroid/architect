package app.ashcon.architect.level.command.provider;

import app.ashcon.architect.level.Level;
import app.ashcon.architect.level.LevelStore;
import app.ashcon.architect.level.command.annotation.Require;
import app.ashcon.architect.level.type.Role;
import app.ashcon.intake.argument.ArgumentException;
import app.ashcon.intake.argument.CommandArgs;
import app.ashcon.intake.bukkit.parametric.provider.BukkitProvider;
import app.ashcon.intake.parametric.ProvisionException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.List;

public class StaticLevelProvider implements BukkitProvider<Level> {

    final LevelStore levelStore;

    @Inject StaticLevelProvider(LevelStore levelStore) {
        this.levelStore = levelStore;
    }

    @Override
    public boolean isProvided() {
        return true;
    }

    @Nullable
    @Override
    public Level get(CommandSender sender, CommandArgs args, List<? extends Annotation> mods) throws ArgumentException, ProvisionException {
        if(sender instanceof Player) {
            Player player = (Player) sender;
            Level level = levelStore.find(player.getWorld().getName())
                                    .filter(Level::isLoaded)
                                    .orElseThrow(() -> new ArgumentException("Could not find the level from your world"));
            Role role = role(mods);
            if(level.hasRole(role, sender)) {
                return level;
            } else {
                throw new ArgumentException("You must have at least the " + role + " role to use this command");
            }
        }
        throw new ArgumentException("You must be a player in a level to use this command");
    }

    protected Role role(List<? extends Annotation> mods) {
        return mods.stream()
                   .filter(mod -> mod instanceof Require)
                   .findFirst()
                   .map(mod -> ((Require) mod).value())
                   .orElse(Role.VIEWER);
    }

}
