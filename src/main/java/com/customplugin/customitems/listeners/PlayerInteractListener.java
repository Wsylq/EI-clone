package com.customplugin.customitems.listeners;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.engine.ExecutionEngine;
import com.customplugin.customitems.model.CustomItem;
import com.customplugin.customitems.model.ExecutionContext;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * Handles right-click and left-click activators:
 *   RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK, LEFT_CLICK_AIR, LEFT_CLICK_BLOCK
 */
public final class PlayerInteractListener implements Listener {

    private final CustomItemsPlugin plugin;
    private final ExecutionEngine engine;

    public PlayerInteractListener(CustomItemsPlugin plugin) {
        this.plugin = plugin;
        this.engine = new ExecutionEngine(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        Optional<CustomItem> customItemOpt = plugin.getItemRegistry().fromItemStack(item);
        if (customItemOpt.isEmpty()) return;

        CustomItem customItem = customItemOpt.get();
        Action action = event.getAction();

        String activatorType = switch (action) {
            case RIGHT_CLICK_AIR   -> "RIGHT_CLICK";
            case RIGHT_CLICK_BLOCK -> "RIGHT_CLICK";
            case LEFT_CLICK_AIR    -> "LEFT_CLICK";
            case LEFT_CLICK_BLOCK  -> "LEFT_CLICK";
            default -> null;
        };

        if (activatorType == null) return;
        if (!customItem.hasActivator(activatorType)) return;

        Block clickedBlock = event.getClickedBlock();
        ExecutionContext context = ExecutionContext.builder(player)
                .activatorType(activatorType)
                .item(item)
                .targetBlock(clickedBlock)
                .build();

        boolean executed = engine.execute(customItem, context);
        if (executed && plugin.getConfig().getBoolean("cancel_interact_event", true)) {
            event.setCancelled(true);
        }
    }
}
