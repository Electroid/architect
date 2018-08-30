package app.ashcon.architect.level.listener;

import app.ashcon.architect.level.Level;
import app.ashcon.architect.level.LevelCache;
import app.ashcon.architect.level.type.Flag;
import app.ashcon.architect.level.type.Role;
import org.bukkit.Physical;
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
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.StructureGrowEvent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Singleton
public class LevelInteractListener implements Listener {

    final LevelCache levelCache;

    @Inject LevelInteractListener(LevelCache levelCache) {
        this.levelCache = levelCache;
    }

    public boolean ok(Physical physical, Predicate<Level> predicate) {
        boolean ok = levelCache.find(physical.getWorld().getName())
                               .map(level -> !level.isLocked() && predicate.test(level))
                               .orElse(true);
        if(physical instanceof Cancellable) {
            ((Cancellable) physical).setCancelled(!ok);
        }
        return ok;
    }

    public boolean ok(Physical physical, Flag... flags) {
        return ok(physical, level -> Stream.of(flags).noneMatch(level::hasFlag));
    }

    public boolean ok(PlayerAction action, BiPredicate<Level, Player> predicate) {
        return ok((Physical) action, level -> predicate.test(level, action.getActor()));
    }

    public boolean ok(PlayerAction action, Role role) {
        return ok(action, (level, player) -> level.hasRole(role, action.getActor()));
    }

    @EventHandler(ignoreCancelled = true)
    void blockPhysics(BlockPhysicsEvent event) {
        ok(event, Flag.PHYSICS);
    }

    @EventHandler(ignoreCancelled = true)
    void blockForm(BlockFromToEvent event) {
        ok(event, Flag.PHYSICS);
    }

    @EventHandler(ignoreCancelled = true)
    void blockChange(EntityChangeBlockEvent event) {
        ok(event, Flag.PHYSICS);
    }

    @EventHandler(ignoreCancelled = true)
    void iceForm(BlockFormEvent event) {
        ok(event, Flag.WORLD);
    }

    @EventHandler(ignoreCancelled = true)
    void blockFade(BlockFadeEvent event) {
        ok(event, Flag.WORLD);
    }

    @EventHandler(ignoreCancelled = true)
    void blockGrow(BlockGrowEvent event) {
        ok(event, Flag.WORLD);
    }

    @EventHandler(ignoreCancelled = true)
    void blockSpread(BlockSpreadEvent event) {
        ok(event, Flag.WORLD);
    }

    @EventHandler(ignoreCancelled = true)
    void blockMove(BlockFromToEvent event) {
        ok(event, Flag.WORLD);
    }

    @EventHandler(ignoreCancelled = true)
    void blockLeaf(LeavesDecayEvent event) {
        ok(event, Flag.WORLD);
    }

    @EventHandler(ignoreCancelled = true)
    void structureGrow(StructureGrowEvent event) {
        ok(event, Flag.WORLD);
    }

    @EventHandler(ignoreCancelled = true)
    void entityInteract(EntityInteractEvent event) {
        ok(event, Flag.WORLD);
    }

    @EventHandler(ignoreCancelled = true)
    void entityFormBlock(EntityBlockFormEvent event) {
        ok(event, Flag.WORLD);
    }

    @EventHandler(ignoreCancelled = true)
    void blockIgnite(BlockIgniteEvent event) {
        ok(event, Flag.FIRE);
    }

    @EventHandler(ignoreCancelled = true)
    void blockBurn(BlockBurnEvent event) {
        ok(event, Flag.FIRE);
    }

    @EventHandler(ignoreCancelled = true)
    void weatherChange(WeatherChangeEvent event) {
        if(event.toWeatherState()) {
            ok(event, Flag.WEATHER);
        }
    }

    @EventHandler(ignoreCancelled = true)
    void entityExplode(EntityExplodeEvent event) {
        ok(event, Flag.EXPLOSIONS);
    }

    @EventHandler(ignoreCancelled = true)
    void blockExplode(BlockExplodeEvent event) {
        ok(event, Flag.EXPLOSIONS);
    }

    @EventHandler(ignoreCancelled = true)
    void hangingBreak(HangingBreakEvent event) {
        if(event.getCause().equals(HangingBreakEvent.RemoveCause.EXPLOSION)) {
            ok(event, Flag.EXPLOSIONS);
        }
    }

    @EventHandler(ignoreCancelled = true)
    void creatureSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        if(entity instanceof Monster) {
            ok(event, Flag.MONSTERS);
        } else if(entity instanceof Animals) {
            ok(event, Flag.ANIMALS);
        }
    }

    @EventHandler(ignoreCancelled = true)
    void bucketEmpty(PlayerBucketEmptyEvent event) {
        ok(event);
    }

    @EventHandler(ignoreCancelled = true)
    void bucketFill(PlayerBucketFillEvent event) {
        ok(event);
    }

    @EventHandler(ignoreCancelled = true)
    void itemDrop(PlayerDropItemEvent event) {
        if(!ok(event)) {
            event.getItemDrop().remove();
        }
    }

    @EventHandler(ignoreCancelled = true)
    void itemPickup(PlayerPickupItemEvent event) {
        ok(event);
    }

    @EventHandler(ignoreCancelled = true)
    void arrowPickup(PlayerPickupArrowEvent event) {
        ok(event);
    }

    @EventHandler(ignoreCancelled = true)
    void vehicleEnter(VehicleEnterEvent event) {
        ok(event);
    }

    @EventHandler(ignoreCancelled = true)
    void vehicleCreate(VehicleCreateEvent event) {
        if(!ok(event)) {
            event.getVehicle().remove();
        }
    }

    @EventHandler(ignoreCancelled = true)
    void vehicleDamage(VehicleDamageEvent event) {
        ok(event);
    }

    @EventHandler(ignoreCancelled = true)
    void vehicleDestroy(VehicleDestroyEvent event) {
        ok(event);
    }

    @EventHandler(ignoreCancelled = true)
    void shootProjectile(ProjectileLaunchEvent event) {
        ok(event);
    }

    @EventHandler(ignoreCancelled = true)
    void inventoryClick(InventoryClickEvent event) {
        InventoryType type = event.getInventory().getType();
        if(type.equals(InventoryType.CREATIVE) || type.equals(InventoryType.PLAYER)) {
            ok(event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    void itemSpawn(ItemSpawnEvent event) {
        ok(event);
    }

    @EventHandler(ignoreCancelled = true)
    void entityTarget(EntityTargetEvent event) {
        ok(event);
    }

    @EventHandler(ignoreCancelled = true)
    void entityTame(EntityTameEvent event) {
        ok(event);
    }

    @EventHandler(ignoreCancelled = true)
    void entityDamage(EntityDamageByEntityEvent event) {
        ok(event);
    }

    @EventHandler(ignoreCancelled = true)
    void playerInteract(PlayerInteractEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void playerInteract(PlayerAttackEntityEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void playerInteract(PlayerInteractEntityEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void playerInteract(PlayerDropItemEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void playerInteract(PlayerArmorStandManipulateEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void blockBreak(BlockBreakEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void blockPlace(BlockPlaceEvent event) {
        ok(event, Role.EDITOR);
    }

    @EventHandler(ignoreCancelled = true)
    void blockPlace(BlockMultiPlaceEvent event) {
        ok(event, Role.EDITOR);
    }

}
