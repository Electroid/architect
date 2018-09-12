package app.ashcon.architect;

import app.ashcon.architect.level.Level;
import app.ashcon.architect.level.command.annotation.Current;
import app.ashcon.architect.level.type.Action;
import app.ashcon.architect.level.type.Flag;
import app.ashcon.architect.level.type.Role;
import app.ashcon.architect.level.type.Visibility;
import app.ashcon.intake.bukkit.BukkitIntake;
import app.ashcon.intake.parametric.AbstractModule;
import app.ashcon.intake.parametric.provider.EnumProvider;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class ArchitectApp extends JavaPlugin {

    ArchitectComponent component;

    @Override
    public void onLoad() {
        super.onLoad();
        this.component = DaggerArchitectComponent.create();
        new BukkitIntake(this, graph -> {
            graph.getBuilder().getInjector().install(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(Role.class).toProvider(new EnumProvider<>(Role.class));
                    bind(Visibility.class).toProvider(new EnumProvider<>(Visibility.class));
                    bind(Flag.class).toProvider(new EnumProvider<>(Flag.class));
                    bind(Action.class).toProvider(new EnumProvider<>(Action.class));
                    bind(Level.class).annotatedWith(Current.class).toProvider(component.staticProvider());
                    bind(Level.class).toProvider(component.dynamicProvider());
                }
            });
            graph.groupedCommands().registerGrouped(component.commands());
        });
    }

    @Override
    public void onEnable() {
        super.onEnable();
        Bukkit.getPluginManager().registerEvents(component.interactListener(), this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }



    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return super.onCommand(sender, command, label, args);
    }

}
