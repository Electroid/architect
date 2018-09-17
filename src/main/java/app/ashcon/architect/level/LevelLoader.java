package app.ashcon.architect.level;

import dagger.Reusable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.ChunkGenerator;

import javax.inject.Inject;
import java.io.File;
import java.util.Random;

/**
 * Synchronizes {@link Level} and {@link World} states.
 */
@Reusable
public class LevelLoader {

    private final LevelStore levelStore;

    @Inject LevelLoader(LevelStore levelStore) {
        this.levelStore = levelStore;
    }

    /**
     * Save the level and write to disk.
     *
     * @param level The level to save.
     */
    public void save(Level level) {
        if(level.isLocked() || !level.isLoaded()) return;
        final World world = level.needWorld();
        world.save();
        levelStore.upload(level.getId(), world.getWorldFolder());
    }

    /**
     * Load the level onto the server.
     *
     * @param level The level to load.
     */
    public void load(Level level) {
        if(level.isLoaded()) return;
        final File folder = new File(Bukkit.getWorldContainer(), level.getId());
        levelStore.download(level.getId(), folder);
        WorldCreator creator = Bukkit.detectWorld(level.getId());
        final boolean initalize = creator == null;
        if(initalize) {
            creator = new WorldCreator(level.getId())
                .environment(World.Environment.NORMAL)
                .type(WorldType.FLAT)
                .generateStructures(false)
                .hardcore(false)
                .seed(level.getId().hashCode());
        }
        final World world = creator.generator(new ChunkGenerator() {
            @Override
            public byte[] generate(World world, Random random, int x, int z) {
                return new byte[Short.MAX_VALUE];
            }
        }).createWorld();
        world.setAutoSave(false);
        world.setKeepSpawnInMemory(false);
        world.setSpawnLocation(level.getSpawn().toLocation(world));
        if(initalize) {
            world.setGameRuleValue("doGiveCredit", "architect");
            world.setGameRuleValue("doDaylightCycle", "false");
            world.setTime(0L);
            world.getBlockAt(level.getSpawn()).getRelative(BlockFace.DOWN).setType(Material.BEDROCK);
        }
    }

    /**
     * Unload the level and remove it from the server.
     *
     * @param level The level to unload.
     */
    public void unload(Level level) {
        if(!level.isLoaded() || level.isDefault()) return;
        final World world = level.needWorld();
        final Level fallback = levelStore.fallback();
        world.getPlayers().forEach(player -> {
            player.teleport(fallback.getSpawnLocation());
            player.sendMessage(ChatColor.RED + "Level " + level.getName() + " was deleted, teleported to " + fallback.getName());
        });
        save(level);
        Bukkit.unloadWorld(world, false);
    }

}
