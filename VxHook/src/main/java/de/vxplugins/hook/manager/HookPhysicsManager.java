package de.vxplugins.hook.manager;

import de.vxplugins.hook.config.PluginConfig;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class HookPhysicsManager {

    private final PluginConfig config;

    // Thread-safe Folia / Paper compatible state tracking
    private final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final Map<UUID, Long> fallImmunity = new ConcurrentHashMap<>();

    public HookPhysicsManager(PluginConfig config) {
        this.config = config;
    }

    /**
     * Executes the true vector physics pull of the player towards target location.
     */
    public boolean pullPlayer(Player player, Location target) {
        if (player == null || target == null) return false;

        Location playerLoc = player.getLocation();
        double distance = playerLoc.distance(target);

        if (distance > config.getMaxDistance()) {
            return false;
        }

        if (distance < config.getMinDistance()) {
            return false;
        }

        // True Vector calculation
        Vector direction = target.toVector().subtract(playerLoc.toVector());
        direction.normalize();

        // Calculate smooth vector velocity with configurable pull strength and vertical boost
        Vector velocity = direction.multiply(config.getPullStrength());
        velocity.setY(Math.max(velocity.getY() + config.getVerticalBoost(), 0.5));

        player.setVelocity(velocity);

        // Smart fall damage immunity registration
        if (config.isPreventFallDamage()) {
            long expireAt = System.currentTimeMillis() + (config.getImmunityTimeoutSeconds() * 1000L);
            fallImmunity.put(player.getUniqueId(), expireAt);
        }

        // Visual Particle Trail
        if (config.isParticlesEnabled()) {
            spawnParticleTrail(player.getEyeLocation(), target);
        }

        // Audio Polish: Pull Sound & Impact Sound
        player.playSound(playerLoc, config.getPullSound(), config.getPullVolume(), config.getPullPitch());
        player.playSound(target, config.getImpactSound(), config.getImpactVolume(), config.getImpactPitch());

        return true;
    }

    /**
     * Spawns a sleek particle line between player eye location and target.
     */
    private void spawnParticleTrail(Location start, Location end) {
        World world = start.getWorld();
        if (world == null || !world.equals(end.getWorld())) return;

        double distance = start.distance(end);
        if (distance <= 0) return;

        Vector dir = end.toVector().subtract(start.toVector()).normalize();
        double step = 0.5; // Density of particle line
        int count = Math.min((int) (distance / step), 120);

        Particle particle = config.getParticleType();
        for (int i = 0; i <= count; i++) {
            Vector point = start.toVector().add(dir.clone().multiply(i * step));
            world.spawnParticle(particle, point.getX(), point.getY(), point.getZ(), 1, 0, 0, 0, 0);
        }
    }

    public boolean isOnCooldown(Player player) {
        if (!config.isCooldownEnabled()) return false;
        if (player.hasPermission("vxhook.bypass.cooldown")) return false;

        Long expireAt = cooldowns.get(player.getUniqueId());
        if (expireAt == null) return false;

        return System.currentTimeMillis() < expireAt;
    }

    public double getRemainingCooldown(Player player) {
        Long expireAt = cooldowns.get(player.getUniqueId());
        if (expireAt == null) return 0.0;

        long diff = expireAt - System.currentTimeMillis();
        if (diff <= 0) return 0.0;

        return Math.round((diff / 1000.0) * 10.0) / 10.0;
    }

    public void setCooldown(Player player) {
        if (!config.isCooldownEnabled() || player.hasPermission("vxhook.bypass.cooldown")) return;
        long expireAt = System.currentTimeMillis() + (long) (config.getCooldownSeconds() * 1000L);
        cooldowns.put(player.getUniqueId(), expireAt);
    }

    public boolean hasFallImmunity(UUID uuid) {
        Long expireAt = fallImmunity.get(uuid);
        if (expireAt == null) return false;
        if (System.currentTimeMillis() > expireAt) {
            fallImmunity.remove(uuid);
            return false;
        }
        return true;
    }

    public boolean consumeFallImmunity(UUID uuid) {
        Long expireAt = fallImmunity.remove(uuid);
        if (expireAt == null) return false;
        return System.currentTimeMillis() <= expireAt;
    }

    public void cleanExpired() {
        long now = System.currentTimeMillis();
        cooldowns.entrySet().removeIf(entry -> entry.getValue() <= now);
        fallImmunity.entrySet().removeIf(entry -> entry.getValue() <= now);
    }
}
