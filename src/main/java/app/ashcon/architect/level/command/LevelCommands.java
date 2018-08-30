package app.ashcon.architect.level.command;

import app.ashcon.architect.level.Level;
import app.ashcon.architect.level.LevelStore;
import app.ashcon.architect.level.command.annotation.Current;
import app.ashcon.architect.level.command.annotation.Require;
import app.ashcon.architect.level.type.Role;
import app.ashcon.architect.level.type.Visibility;
import app.ashcon.intake.Command;
import app.ashcon.intake.bukkit.parametric.Type;
import app.ashcon.intake.bukkit.parametric.annotation.Fallback;
import app.ashcon.intake.bukkit.parametric.annotation.Sender;
import app.ashcon.intake.group.At;
import app.ashcon.intake.group.Group;
import app.ashcon.intake.parametric.annotation.Default;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Singleton
@Group({@At("level"), @At("lvl")})
public class LevelCommands {

    /**
     *  /lvl info
     *  /lvl lock
     *  /lvl spawn [...]
     *  /lvl name [...]
     *  /lvl visibility [...]
     *  /lvl toggle [...]
     *  /lvl add [player] [...]
     *  /lvl rm [player]
     *  /lvl save
     *  /lvl discard -c
     *  /lvl delete -c
     *
     *  /lvl list -p
     *  /lvl tp [name]
     *  /lvl create [name] [owner]
     */

    final LevelStore levelStore;

    @Inject LevelCommands(LevelStore levelStore) {
        this.levelStore = levelStore;
    }

    public boolean update(CommandSender sender, Level level, Consumer<Level> updater) {
        updater.accept(level);
        try {
            levelStore.update(level);
            return true;
        } catch(Throwable err) {
            sender.sendMessage(ChatColor.RED + "Unable to update level to database");
            err.printStackTrace();
        }
        return false;
    }

    @Command(
        aliases = "lock",
        desc = "Lock the level and prevent changes"
    )
    public void add(CommandSender sender, @Current @Require(Role.OWNER) Level level) {
        if(level.isLocked()) {
            sender.sendMessage(ChatColor.RED + "The level is already locked");
        } else if(update(sender, level, lvl -> lvl.setLocked(true))) {
            sender.sendMessage(ChatColor.YELLOW + "Level is now set to locked");
        }
    }

}
