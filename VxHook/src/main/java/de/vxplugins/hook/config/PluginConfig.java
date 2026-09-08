package de.vxplugins.hook.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class PluginConfig {

    private final JavaPlugin plugin;

    private int customModelData;
    private String itemName;
    private List<String> itemLore;

    private int maxUses;
    private Sound breakSound;
    private String breakMessage;

    private double pullStrength;
    private double verticalBoost;
    private double maxDistance;
    private double minDistance;

    private boolean preventFallDamage;
    private int immunityTimeoutSeconds;

    private boolean cooldownEnabled;
    private double cooldownSeconds;
    private String cooldownMessage;

    private Sound pullSound;
    private float pullVolume;
    private float pullPitch;

    private Sound launchSound;
    private float launchVolume;
    private float launchPitch;

    private Sound impactSound;
    private float impactVolume;
    private float impactPitch;

    private boolean particlesEnabled;
    private Particle particleType;
    private int particleCount;

    private boolean recipeEnabled;
    private boolean recipeShapeless;
    private List<String> recipeIngredients;
    private boolean requirePermissionToUse;

    private String prefix;
    private String noPermissionMessage;
    private String reloadMessage;
    private String giveSuccessMessage;
    private String receivedHookMessage;
    private String playerNotFoundMessage;
    private String onlyPlayersMessage;
    private String invalidAmountMessage;
    private String usageMessage;

    public PluginConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        this.customModelData = config.getInt("custom-model-data", 10001);

        this.itemName = color(config.getString("item.name", "&b&lVX&3&lHook"));
        this.itemLore = new ArrayList<>();
        for (String line : config.getStringList("item.lore")) {
            this.itemLore.add(color(line));
        }

        this.maxUses = config.getInt("durability.max-uses", 100);
        this.breakSound = parseSound(config.getString("durability.break-sound", "ENTITY_ITEM_BREAK"), Sound.ENTITY_ITEM_BREAK);
        this.breakMessage = color(config.getString("durability.break-message", "&cDein VXHook ist zerbrochen!"));

        this.pullStrength = config.getDouble("physics.pull-strength", 3.0);
        this.verticalBoost = config.getDouble("physics.vertical-boost", 0.9);
        this.maxDistance = config.getDouble("physics.max-distance", 120.0);
        this.minDistance = config.getDouble("physics.min-distance", 2.0);

        this.preventFallDamage = config.getBoolean("fall-damage.prevent-fall-damage", true);
        this.immunityTimeoutSeconds = config.getInt("fall-damage.immunity-timeout-seconds", 15);

        this.cooldownEnabled = config.getBoolean("cooldown.enabled", false);
        this.cooldownSeconds = config.getDouble("cooldown.seconds", 1.0);
        this.cooldownMessage = color(config.getString("cooldown.message", "&cBitte warte noch {time}s vor dem nächsten Einsatz!"));

        this.pullSound = parseSound(config.getString("effects.sounds.pull-sound", "ENTITY_ENDER_PEARL_THROW"), Sound.ENTITY_ENDER_PEARL_THROW);
        this.pullVolume = (float) config.getDouble("effects.sounds.pull-volume", 1.0);
        this.pullPitch = (float) config.getDouble("effects.sounds.pull-pitch", 1.2);

        this.launchSound = parseSound(config.getString("effects.sounds.launch-sound", "ENTITY_ARROW_SHOOT"), Sound.ENTITY_ARROW_SHOOT);
        this.launchVolume = (float) config.getDouble("effects.sounds.launch-volume", 0.8);
        this.launchPitch = (float) config.getDouble("effects.sounds.launch-pitch", 1.0);

        this.impactSound = parseSound(config.getString("effects.sounds.impact-sound", "BLOCK_IRON_TRAPDOOR_OPEN"), Sound.BLOCK_IRON_TRAPDOOR_OPEN);
        this.impactVolume = (float) config.getDouble("effects.sounds.impact-volume", 0.7);
        this.impactPitch = (float) config.getDouble("effects.sounds.impact-pitch", 1.4);

        this.particlesEnabled = config.getBoolean("effects.particles.enabled", true);
        this.particleType = parseParticle(config.getString("effects.particles.type", "CRIT"), Particle.CRIT);
        this.particleCount = config.getInt("effects.particles.count", 12);

        this.recipeEnabled = config.getBoolean("recipe.enabled", true);
        this.recipeShapeless = config.getBoolean("recipe.shapeless", true);
        List<String> ingredients = config.getStringList("recipe.ingredients");
        if (ingredients == null || ingredients.isEmpty()) {
            this.recipeIngredients = new ArrayList<>(List.of("FISHING_ROD", "SLIME_BALL"));
        } else {
            this.recipeIngredients = new ArrayList<>(ingredients);
        }
        this.requirePermissionToUse = config.getBoolean("permissions.require-permission-to-use", false);

        this.prefix = color(config.getString("messages.prefix", "&8[&bVX&3Hook&8] &r"));
        this.noPermissionMessage = color(prefix + config.getString("messages.no-permission", "&cDazu hast du keine Berechtigung!"));
        this.reloadMessage = color(prefix + config.getString("messages.reload", "&aKonfiguration wurde erfolgreich neu geladen!"));
        this.giveSuccessMessage = color(prefix + config.getString("messages.give-success", "&aVXHook an &e{player} &a({amount}x) gegeben!"));
        this.receivedHookMessage = color(prefix + config.getString("messages.received-hook", "&aDu hast einen &bVX&3Hook &aerhalten!"));
        this.playerNotFoundMessage = color(prefix + config.getString("messages.player-not-found", "&cSpieler &e{player} &cwurde nicht gefunden!"));
        this.onlyPlayersMessage = color(prefix + config.getString("messages.only-players", "&cNur Spieler können diesen Befehl ausführen!"));
        this.invalidAmountMessage = color(prefix + config.getString("messages.invalid-amount", "&cUngültige Anzahl!"));
        this.usageMessage = color(prefix + config.getString("messages.usage", "&cVerwendung: /vxhook [give <spieler> [anzahl] | reload]"));
    }

    public static String color(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static Component toComponent(String message) {
        if (message == null) return Component.empty();
        return LegacyComponentSerializer.legacySection().deserialize(message);
    }

    private Sound parseSound(String name, Sound def) {
        try {
            return Sound.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Unbekannter Sound: " + name + " - Fallback: " + def.name());
            return def;
        }
    }

    private Particle parseParticle(String name, Particle def) {
        try {
            return Particle.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Unbekannter Partikel: " + name + " - Fallback: " + def.name());
            return def;
        }
    }

    public int getCustomModelData() { return customModelData; }
    public String getItemName() { return itemName; }
    public List<String> getItemLore() { return itemLore; }
    public int getMaxUses() { return maxUses; }
    public Sound getBreakSound() { return breakSound; }
    public String getBreakMessage() { return breakMessage; }
    public double getPullStrength() { return pullStrength; }
    public double getVerticalBoost() { return verticalBoost; }
    public double getMaxDistance() { return maxDistance; }
    public double getMinDistance() { return minDistance; }
    public boolean isPreventFallDamage() { return preventFallDamage; }
    public int getImmunityTimeoutSeconds() { return immunityTimeoutSeconds; }
    public boolean isCooldownEnabled() { return cooldownEnabled; }
    public double getCooldownSeconds() { return cooldownSeconds; }
    public String getCooldownMessage() { return cooldownMessage; }
    public Sound getPullSound() { return pullSound; }
    public float getPullVolume() { return pullVolume; }
    public float getPullPitch() { return pullPitch; }
    public Sound getLaunchSound() { return launchSound; }
    public float getLaunchVolume() { return launchVolume; }
    public float getLaunchPitch() { return launchPitch; }
    public Sound getImpactSound() { return impactSound; }
    public float getImpactVolume() { return impactVolume; }
    public float getImpactPitch() { return impactPitch; }
    public boolean isParticlesEnabled() { return particlesEnabled; }
    public Particle getParticleType() { return particleType; }
    public int getParticleCount() { return particleCount; }
    public boolean isRecipeEnabled() { return recipeEnabled; }
    public boolean isRecipeShapeless() { return recipeShapeless; }
    public List<String> getRecipeIngredients() { return recipeIngredients; }
    public boolean isRequirePermissionToUse() { return requirePermissionToUse; }
    public String getPrefix() { return prefix; }
    public String getNoPermissionMessage() { return noPermissionMessage; }
    public String getReloadMessage() { return reloadMessage; }
    public String getGiveSuccessMessage() { return giveSuccessMessage; }
    public String getReceivedHookMessage() { return receivedHookMessage; }
    public String getPlayerNotFoundMessage() { return playerNotFoundMessage; }
    public String getOnlyPlayersMessage() { return onlyPlayersMessage; }
    public String getInvalidAmountMessage() { return invalidAmountMessage; }
    public String getUsageMessage() { return usageMessage; }
}
