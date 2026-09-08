package de.vxplugins.hook.listener;

import de.vxplugins.hook.config.PluginConfig;
import de.vxplugins.hook.item.HookItem;
import de.vxplugins.hook.manager.HookPhysicsManager;
import org.bukkit.Location;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class HookListener implements Listener {

    private final PluginConfig config;
    private final HookItem hookItemHelper;
    private final HookPhysicsManager physicsManager;

    public HookListener(PluginConfig config, HookItem hookItemHelper, HookPhysicsManager physicsManager) {
        this.config = config;
        this.hookItemHelper = hookItemHelper;
        this.physicsManager = physicsManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();

        // Determine which hand holds the hook
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        ItemStack activeHook = null;
        boolean isMainHand = true;

        if (hookItemHelper.isHook(mainHand)) {
            activeHook = mainHand;
            isMainHand = true;
        } else if (hookItemHelper.isHook(offHand)) {
            activeHook = offHand;
            isMainHand = false;
        }

        if (activeHook == null) return;

        // Permission check
        if (config.isRequirePermissionToUse() && !player.hasPermission("vxhook.use")) {
            player.sendMessage(config.getNoPermissionMessage());
            event.setCancelled(true);
            return;
        }

        PlayerFishEvent.State state = event.getState();

        // Launch Sound on casting
        if (state == PlayerFishEvent.State.FISHING) {
            player.playSound(player.getLocation(), config.getLaunchSound(), config.getLaunchVolume(), config.getLaunchPitch());
            return;
        }

        // Pull action on IN_GROUND, CAUGHT_ENTITY, or REEL_IN
        FishHook hook = event.getHook();
        Location targetLocation = null;

        if (state == PlayerFishEvent.State.CAUGHT_ENTITY && event.getCaught() != null) {
            targetLocation = event.getCaught().getLocation();
        } else if (state == PlayerFishEvent.State.IN_GROUND || state == PlayerFishEvent.State.REEL_IN) {
            if (hook.isValid()) {
                targetLocation = hook.getLocation();
            }
        }

        if (targetLocation == null) return;

        // Cooldown check
        if (physicsManager.isOnCooldown(player)) {
            String msg = config.getCooldownMessage().replace("{time}", String.valueOf(physicsManager.getRemainingCooldown(player)));
            player.sendMessage(msg);
            return;
        }

        // Execute true vector physics pull
        boolean pulled = physicsManager.pullPlayer(player, targetLocation);

        if (pulled) {
            physicsManager.setCooldown(player);

            // Decrement durability / handle break
            boolean broke = hookItemHelper.decrementUses(activeHook, player);
            if (broke) {
                if (isMainHand) {
                    player.getInventory().setItemInMainHand(null);
                } else {
                    player.getInventory().setItemInOffHand(null);
                }
            }
        }
    }
}
