package com.customplugin.customitems.listeners;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.engine.ExecutionEngine;
import com.customplugin.customitems.model.CustomItem;
import com.customplugin.customitems.model.ExecutionContext;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * Handles the EAT activator — fires when a player finishes consuming a food/potion item.
 */
public final class PlayerItemConsumeListener implements Listener {

    private final CustomItemsPlugin plugin;
    private final ExecutionEngine engine;

    public PlayerItemConsumeListener(CustomItemsPlugin plugin) {
        this.plugin = plugin;
        this.engine = new ExecutionEngine(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        Optional<CustomItem> customItemOpt = plugin.getItemRegistry().fromItemStack(item);
        if (customItemOpt.isEmpty()) return;

        CustomItem customItem = customItemOpt.get();
        if (!customItem.hasActivator("EAT")) return;

        ExecutionContext context = ExecutionContext.builder(player)
                .activatorType("EAT")
                .item(item)
                .build();

        engine.execute(customItem, context);
    }
}
