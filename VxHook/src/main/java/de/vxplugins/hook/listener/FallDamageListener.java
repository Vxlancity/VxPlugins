package de.vxplugins.hook.listener;

import de.vxplugins.hook.manager.HookPhysicsManager;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class FallDamageListener implements Listener {

    private final HookPhysicsManager physicsManager;

    public FallDamageListener(HookPhysicsManager physicsManager) {
        this.physicsManager = physicsManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            if (physicsManager.consumeFallImmunity(player.getUniqueId())) {
                event.setCancelled(true);

                // Polish: subtle landing cloud & sound
                player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 8, 0.2, 0.1, 0.2, 0.05);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_SMALL_FALL, 0.6f, 1.2f);
            }
        }
    }
}
