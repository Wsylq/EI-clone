package com.customplugin.customitems.listeners;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.engine.ExecutionEngine;
import com.customplugin.customitems.model.CustomItem;
import com.customplugin.customitems.model.ExecutionContext;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * Handles HIT activator — fires when a player hits an entity with a custom item.
 */
public final class EntityDamageByEntityListener implements Listener {

    private final CustomItemsPlugin plugin;
    private final ExecutionEngine engine;

    public EntityDamageByEntityListener(CustomItemsPlugin plugin) {
        this.plugin = plugin;
        this.engine = new ExecutionEngine(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        Optional<CustomItem> customItemOpt = plugin.getItemRegistry().fromItemStack(item);
        if (customItemOpt.isEmpty()) return;

        CustomItem customItem = customItemOpt.get();
        if (!customItem.hasActivator("HIT")) return;

        Entity target = event.getEntity();
        ExecutionContext context = ExecutionContext.builder(player)
                .activatorType("HIT")
                .item(item)
                .targetEntity(target)
                .location(target.getLocation())
                .build();

        engine.execute(customItem, context);
    }
}
