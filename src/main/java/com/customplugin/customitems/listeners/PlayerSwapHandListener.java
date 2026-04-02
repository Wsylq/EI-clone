package com.customplugin.customitems.listeners;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.engine.ExecutionEngine;
import com.customplugin.customitems.model.CustomItem;
import com.customplugin.customitems.model.ExecutionContext;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * Handles SWAP_HAND activator — fires when a player presses F to swap items between hands.
 */
public final class PlayerSwapHandListener implements Listener {

    private final CustomItemsPlugin plugin;
    private final ExecutionEngine engine;

    public PlayerSwapHandListener(CustomItemsPlugin plugin) {
        this.plugin = plugin;
        this.engine = new ExecutionEngine(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = event.getMainHandItem();

        Optional<CustomItem> customItemOpt = plugin.getItemRegistry().fromItemStack(mainHandItem);
        if (customItemOpt.isEmpty()) return;

        CustomItem customItem = customItemOpt.get();
        if (!customItem.hasActivator("SWAP_HAND")) return;

        ExecutionContext context = ExecutionContext.builder(player)
                .activatorType("SWAP_HAND")
                .item(mainHandItem)
                .build();

        boolean executed = engine.execute(customItem, context);
        if (executed && plugin.getConfig().getBoolean("cancel_swap_event", true)) {
            event.setCancelled(true);
        }
    }
}
