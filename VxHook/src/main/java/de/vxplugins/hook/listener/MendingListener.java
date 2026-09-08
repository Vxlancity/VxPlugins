package de.vxplugins.hook.listener;

import de.vxplugins.hook.item.HookItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class MendingListener implements Listener {

    private final HookItem hookItemHelper;

    public MendingListener(HookItem hookItemHelper) {
        this.hookItemHelper = hookItemHelper;
    }

    /**
     * Fired when an XP orb repairs an item equipped with Mending.
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerItemMend(PlayerItemMendEvent event) {
        ItemStack item = event.getItem();
        if (!hookItemHelper.isHook(item)) return;

        int currentUses = hookItemHelper.getUses(item);
        int maxUses = hookItemHelper.getMaxUses(item);
        if (currentUses >= maxUses) return;

        // Restore uses proportionally based on XP mend amount (min 1 use)
        int repairUses = Math.max(1, (int) Math.round(event.getRepairAmount() * (100.0 / 64.0)));
        hookItemHelper.repairUses(item, repairUses);
    }

    /**
     * Backup Mending trigger: When player receives XP and holds a damaged Mending VXHook.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        int exp = event.getAmount();
        if (exp <= 0) return;

        Player player = event.getPlayer();
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();

        ItemStack hookToRepair = null;
        if (hookItemHelper.isHook(mainHand) && mainHand.containsEnchantment(Enchantment.MENDING)) {
            if (hookItemHelper.getUses(mainHand) < hookItemHelper.getMaxUses(mainHand)) {
                hookToRepair = mainHand;
            }
        } else if (hookItemHelper.isHook(offHand) && offHand.containsEnchantment(Enchantment.MENDING)) {
            if (hookItemHelper.getUses(offHand) < hookItemHelper.getMaxUses(offHand)) {
                hookToRepair = offHand;
            }
        }

        if (hookToRepair == null) return;

        int currentUses = hookItemHelper.getUses(hookToRepair);
        int maxUses = hookItemHelper.getMaxUses(hookToRepair);
        int neededUses = maxUses - currentUses;

        // 1 XP restores 2 uses
        int restoreUses = Math.min(neededUses, exp * 2);
        if (restoreUses <= 0) return;

        int expConsumed = (int) Math.ceil((double) restoreUses / 2.0);
        event.setAmount(Math.max(0, exp - expConsumed));

        hookItemHelper.repairUses(hookToRepair, restoreUses);
    }

    /**
     * Allows repairing or combining VXHooks in an anvil.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inv = event.getInventory();
        ItemStack first = inv.getItem(0);
        ItemStack second = inv.getItem(1);

        if (first == null || !hookItemHelper.isHook(first)) return;

        // If repairing with another fishing rod, slime ball, or applying a book
        if (second != null && (second.getType() == Material.FISHING_ROD || second.getType() == Material.SLIME_BALL || second.getType() == Material.ENCHANTED_BOOK)) {
            ItemStack result = first.clone();
            // Restore uses to maximum in anvil
            hookItemHelper.repairUses(result, hookItemHelper.getMaxUses(result));

            // Copy any compatible enchantments from second item (e.g. Mending, Unbreaking)
            if (second.getType() == Material.ENCHANTED_BOOK || second.getType() == Material.FISHING_ROD) {
                ItemMeta secondMeta = second.getItemMeta();
                if (secondMeta != null && secondMeta.hasEnchants()) {
                    for (var entry : secondMeta.getEnchants().entrySet()) {
                        result.addUnsafeEnchantment(entry.getKey(), entry.getValue());
                    }
                }
            }

            event.setResult(result);
            inv.setRepairCost(Math.max(1, inv.getRepairCost()));
        }
    }
}
