package de.vxplugins.hook;

import de.vxplugins.hook.command.VXHookCommand;
import de.vxplugins.hook.config.PluginConfig;
import de.vxplugins.hook.item.HookItem;
import de.vxplugins.hook.listener.FallDamageListener;
import de.vxplugins.hook.listener.HookListener;
import de.vxplugins.hook.listener.MendingListener;
import de.vxplugins.hook.manager.HookPhysicsManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.TimeUnit;

public class VXHookPlugin extends JavaPlugin {

    private static VXHookPlugin instance;

    private PluginConfig pluginConfig;
    private HookItem hookItem;
    private HookPhysicsManager physicsManager;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize configuration
        this.pluginConfig = new PluginConfig(this);

        // Initialize item and physics manager
        this.hookItem = new HookItem(this, pluginConfig);
        this.physicsManager = new HookPhysicsManager(pluginConfig);

        // Register event listeners
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new HookListener(pluginConfig, hookItem, physicsManager), this);
        pm.registerEvents(new FallDamageListener(physicsManager), this);
        pm.registerEvents(new MendingListener(hookItem), this);

        // Register commands and tab completion
        PluginCommand command = getCommand("vxhook");
        if (command != null) {
            VXHookCommand cmdExecutor = new VXHookCommand(pluginConfig, hookItem);
            command.setExecutor(cmdExecutor);
            command.setTabCompleter(cmdExecutor);
        }

        // Register crafting recipes
        hookItem.registerRecipes();

        // Setup Folia & Paper compatible background cleanup task
        setupCleanupTask();

        getLogger().info("==========================================");
        getLogger().info("  🪝 VXHook v" + getDescription().getVersion() + " initialized successfully!");
        getLogger().info("  True Vector Physics, Mending & Multi-Version Native");
        getLogger().info("==========================================");
    }

    @Override
    public void onDisable() {
        if (hookItem != null) {
            hookItem.unregisterRecipes();
        }
        getLogger().info("VXHook disabled successfully.");
    }

    private void setupCleanupTask() {
        try {
            // Folia check
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            Bukkit.getAsyncScheduler().runAtFixedRate(this, task -> {
                if (physicsManager != null) physicsManager.cleanExpired();
            }, 30, 30, TimeUnit.SECONDS);
        } catch (Throwable t) {
            // Paper / Spigot
            Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
                if (physicsManager != null) physicsManager.cleanExpired();
            }, 20L * 30, 20L * 30);
        }
    }

    public static VXHookPlugin getInstance() {
        return instance;
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public HookItem getHookItem() {
        return hookItem;
    }

    public HookPhysicsManager getPhysicsManager() {
        return physicsManager;
    }
}
