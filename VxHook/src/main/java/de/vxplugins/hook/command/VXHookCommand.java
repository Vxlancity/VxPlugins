package de.vxplugins.hook.command;

import de.vxplugins.hook.config.PluginConfig;
import de.vxplugins.hook.item.HookItem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VXHookCommand implements CommandExecutor, TabCompleter {

    private final PluginConfig config;
    private final HookItem hookItemHelper;

    public VXHookCommand(PluginConfig config, HookItem hookItemHelper) {
        this.config = config;
        this.hookItemHelper = hookItemHelper;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(config.getUsageMessage());
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("give")) {
            if (!sender.hasPermission("vxhook.admin.give") && !sender.hasPermission("vxhook.admin")) {
                sender.sendMessage(config.getNoPermissionMessage());
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(PluginConfig.color(config.getPrefix() + "&cVerwendung: /" + label + " give <spieler> [anzahl]"));
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null || !target.isOnline()) {
                String msg = config.getPlayerNotFoundMessage().replace("{player}", args[1]);
                sender.sendMessage(msg);
                return true;
            }

            int amount = 1;
            if (args.length >= 3) {
                try {
                    amount = Integer.parseInt(args[2]);
                    if (amount <= 0 || amount > 64) {
                        sender.sendMessage(config.getInvalidAmountMessage());
                        return true;
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(config.getInvalidAmountMessage());
                    return true;
                }
            }

            ItemStack hook = hookItemHelper.createHookItem(amount);
            var leftover = target.getInventory().addItem(hook);
            if (!leftover.isEmpty()) {
                for (ItemStack drop : leftover.values()) {
                    target.getWorld().dropItemNaturally(target.getLocation(), drop);
                }
            }

            String giveMsg = config.getGiveSuccessMessage()
                    .replace("{player}", target.getName())
                    .replace("{amount}", String.valueOf(amount));
            sender.sendMessage(giveMsg);

            target.sendMessage(config.getReceivedHookMessage());
            return true;
        }

        if (sub.equals("reload")) {
            if (!sender.hasPermission("vxhook.admin.reload") && !sender.hasPermission("vxhook.admin")) {
                sender.sendMessage(config.getNoPermissionMessage());
                return true;
            }

            config.load();
            hookItemHelper.registerRecipes();
            sender.sendMessage(config.getReloadMessage());
            return true;
        }

        sender.sendMessage(config.getUsageMessage());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("vxhook.admin.give") || sender.hasPermission("vxhook.admin")) {
                completions.add("give");
            }
            if (sender.hasPermission("vxhook.admin.reload") || sender.hasPermission("vxhook.admin")) {
                completions.add("reload");
            }
            return filter(completions, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            if (sender.hasPermission("vxhook.admin.give") || sender.hasPermission("vxhook.admin")) {
                List<String> playerNames = new ArrayList<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    playerNames.add(player.getName());
                }
                return filter(playerNames, args[1]);
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            if (sender.hasPermission("vxhook.admin.give") || sender.hasPermission("vxhook.admin")) {
                return filter(Arrays.asList("1", "16", "64"), args[2]);
            }
        }

        return Collections.emptyList();
    }

    private List<String> filter(List<String> list, String prefix) {
        List<String> result = new ArrayList<>();
        String lowerPrefix = prefix.toLowerCase();
        for (String item : list) {
            if (item.toLowerCase().startsWith(lowerPrefix)) {
                result.add(item);
            }
        }
        return result;
    }
}
