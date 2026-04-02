package com.customplugin.customitems.listeners;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.engine.ExecutionEngine;
import com.customplugin.customitems.model.CustomItem;
import com.customplugin.customitems.model.ExecutionContext;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * Handles DROP activator — fires when a player drops a custom item.
 */
public final class PlayerDropItemListener implements Listener {

    private final CustomItemsPlugin plugin;
    private final ExecutionEngine engine;

    public PlayerDropItemListener(CustomItemsPlugin plugin) {
        this.plugin = plugin;
        this.engine = new ExecutionEngine(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItemDrop().getItemStack();

        Optional<CustomItem> customItemOpt = plugin.getItemRegistry().fromItemStack(item);
        if (customItemOpt.isEmpty()) return;

        CustomItem customItem = customItemOpt.get();
        if (!customItem.hasActivator("DROP")) return;

        ExecutionContext context = ExecutionContext.builder(player)
                .activatorType("DROP")
                .item(item)
                .build();

        boolean executed = engine.execute(customItem, context);
        if (executed && plugin.getConfig().getBoolean("cancel_drop_event", false)) {
            event.setCancelled(true);
        }
    }
}
