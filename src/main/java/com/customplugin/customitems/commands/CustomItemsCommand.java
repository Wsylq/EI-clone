package com.customplugin.customitems.commands;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.model.CustomItem;
import com.customplugin.customitems.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Main command handler for /ci (CustomItems).
 *
 * Sub-commands:
 *   /ci give <player> <item-id> [amount]  — Give a custom item to a player
 *   /ci reload                             — Reload all items and config
 *   /ci list                               — List all loaded custom items
 *   /ci debug                              — Toggle debug mode
 *   /ci info <item-id>                     — Show info about a custom item
 */
public final class CustomItemsCommand implements CommandExecutor, TabCompleter {

    private final CustomItemsPlugin plugin;

    public CustomItemsCommand(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "give"   -> handleGive(sender, args);
            case "reload" -> handleReload(sender);
            case "list"   -> handleList(sender);
            case "debug"  -> handleDebug(sender);
            case "info"   -> handleInfo(sender, args);
            default       -> sendHelp(sender);
        }
        return true;
    }

    // ---------------------------------------------------------------------------
    // Sub-command handlers
    // ---------------------------------------------------------------------------

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("customitems.admin")) {
            sender.sendMessage(TextUtil.colorize("&cYou don't have permission to use this command."));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /ci give <player> <item-id> [amount]"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(TextUtil.colorize("&cPlayer '" + args[1] + "' not found or not online."));
            return;
        }

        String itemId = args[2];
        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Math.max(1, Math.min(64, Integer.parseInt(args[3])));
            } catch (NumberFormatException e) {
                sender.sendMessage(TextUtil.colorize("&cInvalid amount: " + args[3]));
                return;
            }
        }

        Optional<CustomItem> opt = plugin.getItemRegistry().getById(itemId);
        if (opt.isEmpty()) {
            sender.sendMessage(TextUtil.colorize("&cUnknown item id: &e" + itemId));
            return;
        }

        // Fix: was getItemStack() — now correctly calls getItemStack() which exists on CustomItem
        ItemStack stack = opt.get().getItemStack().clone();
        stack.setAmount(amount);
        target.getInventory().addItem(stack).values().forEach(leftover ->
                target.getWorld().dropItemNaturally(target.getLocation(), leftover));

        sender.sendMessage(TextUtil.colorize(
                "&aGave &e" + amount + "x &a[" + itemId + "] &ato &e" + target.getName() + "&a."));
        target.sendMessage(TextUtil.colorize(
                "&aYou received &e" + amount + "x &a[" + itemId + "&a]."));
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("customitems.admin")) {
            sender.sendMessage(TextUtil.colorize("&cYou don't have permission to use this command."));
            return;
        }
        plugin.reload();
        sender.sendMessage(TextUtil.colorize(
                "&aCustomItems reloaded. Loaded &e"
                        + plugin.getItemRegistry().getLoadedCount() + " &aitem(s)."));
    }

    private void handleList(CommandSender sender) {
        if (!sender.hasPermission("customitems.admin")) {
            sender.sendMessage(TextUtil.colorize("&cYou don't have permission to use this command."));
            return;
        }
        Collection<CustomItem> items = plugin.getItemRegistry().getAll();
        if (items.isEmpty()) {
            sender.sendMessage(TextUtil.colorize("&eNo custom items loaded."));
            return;
        }
        sender.sendMessage(TextUtil.colorize("&6--- Custom Items (" + items.size() + ") ---"));
        for (CustomItem item : items) {
            // Fix: was getItemStack() — now correctly calls getItemStack() which exists on CustomItem
            sender.sendMessage(TextUtil.colorize(
                    "&7- &e" + item.getId()
                            + " &7(" + item.getItemStack().getType().name() + ")"
                            + " &7Activators: &a" + item.getActivators().keySet()));
        }
    }

    private void handleDebug(CommandSender sender) {
        if (!sender.hasPermission("customitems.admin")) {
            sender.sendMessage(TextUtil.colorize("&cYou don't have permission to use this command."));
            return;
        }
        boolean newState = !plugin.isDebugMode();
        plugin.setDebugMode(newState);
        sender.sendMessage(TextUtil.colorize(
                "&aDebug mode " + (newState ? "&2enabled" : "&cdisabled") + "&a."));
    }

    private void handleInfo(CommandSender sender, String[] args) {
        if (!sender.hasPermission("customitems.admin")) {
            sender.sendMessage(TextUtil.colorize("&cYou don't have permission to use this command."));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(TextUtil.colorize("&cUsage: /ci info <item-id>"));
            return;
        }
        String itemId = args[1];
        Optional<CustomItem> opt = plugin.getItemRegistry().getById(itemId);
        if (opt.isEmpty()) {
            sender.sendMessage(TextUtil.colorize("&cUnknown item id: &e" + itemId));
            return;
        }
        CustomItem item = opt.get();
        // Fix: was getItemStack() — now correctly calls getItemStack() which exists on CustomItem
        sender.sendMessage(TextUtil.colorize("&6--- Info: &e" + item.getId() + " &6---"));
        sender.sendMessage(TextUtil.colorize("&7Material: &e" + item.getItemStack().getType().name()));
        sender.sendMessage(TextUtil.colorize("&7Activators: &e" + item.getActivators().keySet()));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(TextUtil.colorize("&6--- CustomItems Help ---"));
        sender.sendMessage(TextUtil.colorize("&e/ci give <player> <item-id> [amount] &7- Give a custom item"));
        sender.sendMessage(TextUtil.colorize("&e/ci reload &7- Reload config and items"));
        sender.sendMessage(TextUtil.colorize("&e/ci list &7- List all loaded items"));
        sender.sendMessage(TextUtil.colorize("&e/ci debug &7- Toggle debug mode"));
        sender.sendMessage(TextUtil.colorize("&e/ci info <item-id> &7- Show item info"));
    }

    // ---------------------------------------------------------------------------
    // Tab completion
    // ---------------------------------------------------------------------------

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (!sender.hasPermission("customitems.admin")) return completions;

        if (args.length == 1) {
            List<String> subCommands = List.of("give", "reload", "list", "debug", "info");
            for (String sub : subCommands) {
                if (sub.startsWith(args[0].toLowerCase())) {
                    completions.add(sub);
                }
            }
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("give"))) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(p.getName());
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")
                || args.length == 2 && args[0].equalsIgnoreCase("info")) {
            String prefix = args[args.length - 1].toLowerCase();
            for (CustomItem item : plugin.getItemRegistry().getAll()) {
                if (item.getId().toLowerCase().startsWith(prefix)) {
                    completions.add(item.getId());
                }
            }
        }

        return completions;
    }
}
