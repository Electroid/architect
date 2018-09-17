package app.ashcon.architect.level;

import app.ashcon.architect.level.type.Flag;
import app.ashcon.architect.level.type.Role;
import dagger.Reusable;
import org.bukkit.Physical;
import org.bukkit.World;
import org.bukkit.entity.Animals;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.PlayerAction;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerAttackEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerPickupExperienceEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.player.PlayerSpawnEntityEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.StructureGrowEvent;

import javax.inject.Inject;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Listens to {@link World} events in a {@link Level}
 * and checks whether it should happen or not.
 */
@Reusable
public class LevelListener implements Listener {

    private final LevelStore levelStore;

    @Inject LevelListener(LevelStore levelStore) {
        this.levelStore = levelStore;
    }

    /**
     * Get whether a {@link Physical} is allowed to do something.
     *
     * If the {@link Physical} is {@link Cancellable},
     * then it will be cancelled.
     *
     * @param physical The physical object.
     * @param predicate An level predicate.
     * @return Whether the event is allowed.
     */
    public boolean ok(Physical physical, Predicate<Level> predicate) {
        boolean ok = levelStore.findCached(physical.getWorld().getName())
                               .map(level -> !level.isLocked() && predicate.test(level))
                               .orElse(true);
        if(physical instanceof Cancellable) {
            ((Cancellable) physical).setCancelled(!ok);
        }
        return ok;
    }

    /**
     * Get whether a {@link Physical} is allowed to do something.
     *
     * @see #ok(Physical, Predicate)
     * @param physical The physical object.
     * @param flags The required flags for this event.
     * @return Whether the event is allowed.
     */
    public boolean ok(Physical physical, Flag... flags) {
        return ok(physical, level -> flags.length == 0 || Stream.of(flags).anyMatch(level::hasFlag));
    }

    /**
     * Get whether a {@link Player} is allowed to do something.
     *
     * @see #ok(Physical, Predicate)
     * @param action The player action, typically an event.
     * @param predicate The level and player predicate.
     * @return Whether the event is allowed.
     */
    public boolean ok(PlayerAction action, BiPredicate<Level, Player> predicate) {
        return ok((Physical) action, level -> predicate.test(level, action.getActor()));
    }

    /**
     * Get whether a {@link Player} is allowed to do something.
     *
     * @see #ok(PlayerAction, BiPredicate)
     * @param action The player action, typically an event.
     * @param role The role required for this event.
     * @return Whether the event is allowed.
     */
    public boolean ok(PlayerAction action, Role role) {
        return ok(action, (level, player) -> level.hasRole(role, action.getActor()));
    }

    @EventHandler(ignoreCancelled = true)
    void blockPhysics(final BlockPhysicsEvent event) {
        ok(event, Flag.PHYSICS);
    }

    @EventHandler(ignoreCancelled = true)
    void blockForm(final BlockFromToEvent event) {
        ok(event, Flag.PHYSICS);
    }

    @EventHandler(ignoreCancelled = true)
    void blockChange(final EntityChangeBlockEvent event) {
        ok(event, Flag.PHYSICS);
    }

    @EventHandler(ignoreCancelled = true)
    void iceForm(final BlockFormEvent event) {
        ok(event, Flag.WORLD);
    }

    @EventHandler(ignoreCancelled = true)
    void blockFade(final BlockFadeEvent event) {
        ok(event, Flag.WORLD);
    }

    @EventHandler(ignoreCancelled = true)
    void blockGrow(final BlockGrowEvent event) {
        ok(event, Flag.WORLD);
    }

    @EventHandler(ignoreCancelled = true)
    void blockSpread(final BlockSpreadEvent event) {
        ok(event, Flag.WORLD);
    }

    @EventHandler(ignoreCancelled = true)
    void blockMove(final BlockFromToEvent event) {
        ok(event, Flag.WORLD);
    }

    @EventHandler(ignoreCancelled = true)
    void blockLeaf(final LeavesDecayEvent event) {
        ok(event, Flag.WORLD);
    }

    @EventHandler(ignoreCancelled = true)
    void structureGrow(final StructureGrowEvent event) {
        ok(event, Flag.WORLD);
    }

    @EventHandler(ignoreCancelled = true)
    void entityInteract(final EntityInteractEvent event) {
        ok(event, Flag.WORLD);
    }

    @EventHandler(ignoreCancelled = true)
    void entityFormBlock(final EntityBlockFormEvent event) {
        ok(event, Flag.WORLD);
    }

    @EventHandler(ignoreCancelled = true)
    void blockIgnite(final BlockIgniteEvent event) {
        ok(event, Flag.FIRE);
    }

    @EventHandler(ignoreCancelled = true)
    void blockBurn(final BlockBurnEvent event) {
        ok(event, Flag.FIRE);
    }

    @EventHandler(ignoreCancelled = true)
    void weatherChange(final WeatherChangeEvent event) {
        if(event.toWeatherState()) {
            ok(event, Flag.WEATHER);
        }
    }

    @EventHandler(ignoreCancelled = true)
    void entityExplode(final EntityExplodeEvent event) {
        ok(event, Flag.EXPLOSIONS);
    }

    @EventHandler(ignoreCancelled = true)
    void blockExplode(final BlockExplodeEvent event) {
        ok(event, Flag.EXPLOSIONS);
    }

    @EventHandler(ignoreCancelled = true)
    void hangingBreak(final HangingBreakEvent event) {
        if(event.getCause().equals(HangingBreakEvent.RemoveCause.EXPLOSION)) {
            ok(event, Flag.EXPLOSIONS);
        }
    }

    @EventHandler(ignoreCancelled = true)
    void creatureSpawn(final CreatureSpawnEvent event) {
        final LivingEntity entity = event.getEntity();
        if(entity instanceof Monster) {
            ok(event, Flag.MONSTERS);
        } else if(entity instanceof Animals) {
            ok(event, Flag.ANIMALS);
        }
    }

    @EventHandler(ignoreCancelled = true)
    void vehicleEnter(final VehicleEnterEvent event) {
        ok(event);
    }

    @EventHandler(ignoreCancelled = true)
    void vehicleCreate(final VehicleCreateEvent event) {
        if(!ok(event)) {
            event.getVehicle().remove();
        }
    }

    @EventHandler(ignoreCancelled = true)
    void vehicleDamage(final VehicleDamageEvent event) {
        ok(event);
    }

    @EventHandler(ignoreCancelled = true)
    void vehicleDestroy(final VehicleDestroyEvent event) {
        ok(event);
    }

    @EventHandler(ignoreCancelled = true)
    void shootProjectile(final ProjectileLaunchEvent event) {
        ok(event);
    }

    @EventHandler(ignoreCancelled = true)
    void inventoryClick(final InventoryClickEvent event) {
        final InventoryType type = event.getInventory().getType();
        if(type.equals(InventoryType.CREATIVE) || type.equals(InventoryType.PLAYER)) {
            ok(event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    void itemSpawn(final ItemSpawnEvent event) {
        ok(event);
    }

    @EventHandler(ignoreCancelled = true)
    void entityTarget(final EntityTargetEvent event) {
        ok(event);
    }

    @EventHandler(ignoreCancelled = true)
    void entityTame(final EntityTameEvent event) {
        ok(event);
    }

    @EventHandler(ignoreCancelled = true)
    void entityDamage(final EntityDamageByEntityEvent event) {
        ok(event);
    }

    @EventHandler(ignoreCancelled = true)
    void playerInteract(final PlayerInteractEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void playerInteract(final PlayerAttackEntityEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void playerInteract(final PlayerInteractEntityEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void playerInteract(final PlayerDropItemEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void playerInteract(final PlayerArmorStandManipulateEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void blockBreak(final BlockBreakEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void blockPlace(final BlockPlaceEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void blockPlace(final BlockMultiPlaceEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void bucketEmpty(final PlayerBucketEmptyEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void bucketFill(final PlayerBucketFillEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void itemDrop(final PlayerDropItemEvent event) {
        if(!ok(event, Role.EDITOR)) {
            event.getItemDrop().remove();
        }
    }

    @EventHandler(ignoreCancelled = true)
    void itemPickup(final PlayerPickupItemEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void editBook(final PlayerEditBookEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void itemBreak(final PlayerItemBreakEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void pickupExperience(final PlayerPickupExperienceEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void spawnEntity(final PlayerSpawnEntityEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void shearEntity(final PlayerShearEntityEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void fish(final PlayerFishEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void arrowPickup(final PlayerPickupArrowEvent event) {
        ok(event, Role.EDITOR);
    }

}
