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

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

public class DynamicLevelProvider implements BukkitProvider<Level> {

    final LevelStore levelStore;

    @Inject DynamicLevelProvider(LevelStore levelStore) {
        this.levelStore = levelStore;
    }

    @Override
    public String getName() {
        return "level";
    }

    @Nullable
    @Override
    public Level get(CommandSender sender, CommandArgs args, List<? extends Annotation> mods) throws ArgumentException, ProvisionException {
        String query = args.next();
        List<Level> response = search(sender, query, role(mods));
        if (response.isEmpty()) {
            throw new ArgumentException("Could not find any levels named '" + query + "'");
        } else if (response.size() == 1 || response.get(0).getName().equalsIgnoreCase(query)) {
            return response.get(0);
        } else {
            String names = response.stream()
                                   .map(Level::getName)
                                   .collect(Collectors.joining(", "));
            throw new ArgumentException("Please provide a more specific level name: " + names);
        }
    }

    protected List<Level> search(CommandSender sender, String query, @Nullable Role role) {
        return levelStore.search(query)
                         .stream()
                         .filter(level -> role == null || level.hasRole(role, sender))
                         .collect(Collectors.toList());
    }

    protected Role role(List<? extends Annotation> mods) {
        return mods.stream()
                   .filter(mod -> mod instanceof Require)
                   .findFirst()
                   .map(mod -> ((Require) mod).value())
                   .orElse(Role.VIEWER);
    }

}
