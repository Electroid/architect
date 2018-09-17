package app.ashcon.architect.level.command.provider;

import app.ashcon.architect.level.Level;
import app.ashcon.architect.level.LevelStore;
import app.ashcon.architect.level.command.annotation.Require;
import app.ashcon.architect.level.type.Role;
import app.ashcon.intake.argument.ArgumentException;
import app.ashcon.intake.argument.CommandArgs;
import app.ashcon.intake.bukkit.parametric.provider.BukkitProvider;
import app.ashcon.intake.parametric.ProvisionException;
import dagger.Reusable;
import org.bukkit.command.CommandSender;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides {@link Level}s given a query for its name.
 */
@Reusable
public class LevelNamedProvider implements BukkitProvider<Level> {

    private final LevelStore levelStore;

    @Inject
    LevelNamedProvider(LevelStore levelStore) {
        this.levelStore = levelStore;
    }

    @Override
    public String getName() {
        return "level";
    }

    @Override
    public Level get(CommandSender sender, CommandArgs args, List<? extends Annotation> mods) throws ArgumentException, ProvisionException {
        String query = args.next();
        List<Level> response = search(sender, query, getRole(mods));
        if (response.isEmpty()) {
            throw new ArgumentException("Could not find any levels named: "+ query);
        } else if (response.size() == 1 || response.get(0).getName().equalsIgnoreCase(query)) {
            return response.get(0);
        } else {
            String names = response.stream()
                                   .map(Level::getName)
                                   .collect(Collectors.joining(", "));
            throw new ArgumentException("Please provide a more specific level name: " + names);
        }
    }

    private List<Level> search(CommandSender sender, String query, Role role) {
        return levelStore.search(query)
                         .stream()
                         .filter(level -> level.hasRole(role, sender))
                         .collect(Collectors.toList());
    }

    private Role getRole(List<? extends Annotation> mods) {
        return mods.stream()
                   .filter(mod -> mod instanceof Require)
                   .findFirst()
                   .map(mod -> ((Require) mod).value())
                   .orElse(Role.VIEWER);
    }

}
