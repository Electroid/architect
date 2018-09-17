package app.ashcon.architect;

import app.ashcon.architect.level.Level;
import app.ashcon.architect.level.command.annotation.Current;
import app.ashcon.architect.level.command.provider.VectorProvider;
import app.ashcon.architect.level.type.Action;
import app.ashcon.architect.level.type.Flag;
import app.ashcon.architect.level.type.Role;
import app.ashcon.architect.level.type.Status;
import app.ashcon.intake.bukkit.BukkitIntake;
import app.ashcon.intake.parametric.AbstractModule;
import app.ashcon.intake.parametric.provider.EnumProvider;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class ArchitectApp extends JavaPlugin {

    private ArchitectComponent component;

    @Override
    public void onLoad() {
        super.onLoad();
        component = DaggerArchitectComponent.create();
        new BukkitIntake(this, graph -> {
            graph.getBuilder().getInjector().install(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Role.class).toProvider(new EnumProvider<>(Role.class));
                    bind(Status.class).toProvider(new EnumProvider<>(Status.class));
                    bind(Flag.class).toProvider(new EnumProvider<>(Flag.class));
                    bind(Action.class).toProvider(new EnumProvider<>(Action.class));
                    bind(Vector.class).toProvider(new VectorProvider());
                    bind(Level.class).annotatedWith(Current.class).toProvider(component.levelCurrentProvider());
                    bind(Level.class).toProvider(component.levelNamedProvider());
                }
            });
            graph.groupedCommands().registerGrouped(component.levelCommands());
        });
    }

    @Override
    public void onEnable() {
        super.onEnable();
        final PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(component.levelListener(), this);
        pluginManager.registerEvents(component.userListener(), this);
    }

}
