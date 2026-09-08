package de.vxplugins.hook.item;

import de.vxplugins.hook.config.PluginConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HookItem {

    private final JavaPlugin plugin;
    private final PluginConfig config;

    public final NamespacedKey KEY_VXHOOK;
    public final NamespacedKey KEY_USES;
    public final NamespacedKey KEY_MAX_USES;

    private final NamespacedKey RECIPE_SHAPED_1;
    private final NamespacedKey RECIPE_SHAPED_2;
    private final NamespacedKey RECIPE_SHAPELESS;

    public HookItem(JavaPlugin plugin, PluginConfig config) {
        this.plugin = plugin;
        this.config = config;

        this.KEY_VXHOOK = new NamespacedKey(plugin, "is_vxhook");
        this.KEY_USES = new NamespacedKey(plugin, "uses");
        this.KEY_MAX_USES = new NamespacedKey(plugin, "max_uses");

        this.RECIPE_SHAPED_1 = new NamespacedKey(plugin, "vxhook_shaped_1");
        this.RECIPE_SHAPED_2 = new NamespacedKey(plugin, "vxhook_shaped_2");
        this.RECIPE_SHAPELESS = new NamespacedKey(plugin, "vxhook_shapeless");
    }

    public ItemStack createHookItem(int amount) {
        ItemStack item = new ItemStack(Material.FISHING_ROD, Math.max(1, amount));
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        int maxUses = config.getMaxUses();
        int uses = maxUses;

        // Custom Model Data
        meta.setCustomModelData(config.getCustomModelData());

        // Display Name & Lore (Paper Adventure with Spigot legacy fallback)
        applyText(meta, uses, maxUses);

        // Persistent Data Container
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(KEY_VXHOOK, PersistentDataType.BYTE, (byte) 1);
        pdc.set(KEY_USES, PersistentDataType.INTEGER, uses);
        pdc.set(KEY_MAX_USES, PersistentDataType.INTEGER, maxUses);

        // Vanilla Durability Bar (clean 0 damage)
        if (meta instanceof Damageable damageable) {
            damageable.setDamage(0);
        }

        item.setItemMeta(meta);
        return item;
    }

    public boolean isHook(ItemStack item) {
        if (item == null || item.getType() != Material.FISHING_ROD) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(KEY_VXHOOK, PersistentDataType.BYTE);
    }

    public int getUses(ItemStack item) {
        if (!isHook(item)) return 0;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.getOrDefault(KEY_USES, PersistentDataType.INTEGER, config.getMaxUses());
    }

    public int getMaxUses(ItemStack item) {
        if (!isHook(item)) return config.getMaxUses();
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.getOrDefault(KEY_MAX_USES, PersistentDataType.INTEGER, config.getMaxUses());
    }

    public void setUses(ItemStack item, int newUses) {
        if (!isHook(item)) return;
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        int maxUses = pdc.getOrDefault(KEY_MAX_USES, PersistentDataType.INTEGER, config.getMaxUses());
        int clampedUses = Math.max(0, Math.min(newUses, maxUses));

        pdc.set(KEY_USES, PersistentDataType.INTEGER, clampedUses);
        applyText(meta, clampedUses, maxUses);

        // Sync vanilla durability bar
        if (meta instanceof Damageable damageable) {
            short maxDmg = Material.FISHING_ROD.getMaxDurability();
            int damage = (int) Math.round(((double) (maxUses - clampedUses) / maxUses) * maxDmg);
            damageable.setDamage(Math.min(damage, maxDmg - 1));
        }

        item.setItemMeta(meta);
    }

    public int repairUses(ItemStack item, int restoreAmount) {
        int current = getUses(item);
        int max = getMaxUses(item);
        int target = Math.min(max, current + restoreAmount);
        setUses(item, target);
        return target;
    }

    public boolean decrementUses(ItemStack item, Player player) {
        int uses = getUses(item) - 1;
        if (uses <= 0) {
            // Break item
            player.playSound(player.getLocation(), config.getBreakSound(), 1.0f, 1.0f);
            String breakMsg = config.getBreakMessage();
            if (breakMsg != null && !breakMsg.isEmpty()) {
                player.sendMessage(breakMsg);
            }
            return true; // Broke
        }

        setUses(item, uses);
        return false;
    }

    private void applyText(ItemMeta meta, int uses, int maxUses) {
        try {
            meta.displayName(PluginConfig.toComponent(config.getItemName()));
            List<Component> lore = new ArrayList<>();
            for (String line : config.getItemLore()) {
                String formatted = line.replace("{uses}", String.valueOf(uses))
                        .replace("{max_uses}", String.valueOf(maxUses));
                lore.add(PluginConfig.toComponent(formatted));
            }
            meta.lore(lore);
        } catch (Throwable t) {
            // Fallback for Spigot/CraftBukkit without Paper Adventure
            meta.setDisplayName(config.getItemName());
            List<String> lore = new ArrayList<>();
            for (String line : config.getItemLore()) {
                lore.add(line.replace("{uses}", String.valueOf(uses))
                        .replace("{max_uses}", String.valueOf(maxUses)));
            }
            meta.setLore(lore);
        }
    }

    public void registerRecipes() {
        if (!config.isRecipeEnabled()) return;

        // Remove existing recipes to avoid duplicates
        unregisterRecipes();

        ItemStack result = createHookItem(1);

        List<Material> materials = new ArrayList<>();
        for (String ingredientName : config.getRecipeIngredients()) {
            Material mat = Material.matchMaterial(ingredientName.trim());
            if (mat != null) {
                materials.add(mat);
            } else {
                plugin.getLogger().warning("Unbekanntes Material im Crafting-Rezept: " + ingredientName);
            }
        }

        if (materials.isEmpty()) {
            materials.add(Material.FISHING_ROD);
            materials.add(Material.SLIME_BALL);
        }

        if (config.isRecipeShapeless()) {
            // Formloses Rezept (Shapeless): 1x Fishing Rod + 1x Slimeball ➔ 1x VXHook
            ShapelessRecipe shapeless = new ShapelessRecipe(RECIPE_SHAPELESS, result);
            for (Material mat : materials) {
                shapeless.addIngredient(mat);
            }
            Bukkit.addRecipe(shapeless);
        } else {
            // Geformtes Rezept (Shaped)
            if (materials.size() == 2) {
                ShapedRecipe shaped1 = new ShapedRecipe(RECIPE_SHAPED_1, result);
                shaped1.shape("AB");
                shaped1.setIngredient('A', materials.get(0));
                shaped1.setIngredient('B', materials.get(1));
                Bukkit.addRecipe(shaped1);

                ShapedRecipe shaped2 = new ShapedRecipe(RECIPE_SHAPED_2, result);
                shaped2.shape("BA");
                shaped2.setIngredient('B', materials.get(1));
                shaped2.setIngredient('A', materials.get(0));
                Bukkit.addRecipe(shaped2);
            } else {
                ShapelessRecipe shapeless = new ShapelessRecipe(RECIPE_SHAPELESS, result);
                for (Material mat : materials) {
                    shapeless.addIngredient(mat);
                }
                Bukkit.addRecipe(shapeless);
            }
        }
    }

    public void unregisterRecipes() {
        Iterator<org.bukkit.inventory.Recipe> it = Bukkit.recipeIterator();
        while (it.hasNext()) {
            org.bukkit.inventory.Recipe recipe = it.next();
            if (recipe instanceof org.bukkit.Keyed keyed) {
                if (keyed.getKey().equals(RECIPE_SHAPED_1) ||
                    keyed.getKey().equals(RECIPE_SHAPED_2) ||
                    keyed.getKey().equals(RECIPE_SHAPELESS)) {
                    it.remove();
                }
            }
        }
    }
}
