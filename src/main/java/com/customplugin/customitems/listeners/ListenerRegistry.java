package com.customplugin.customitems.listeners;

import com.customplugin.customitems.CustomItemsPlugin;
import org.bukkit.event.HandlerList;

/**
 * Centrally registers and unregisters all plugin event listeners.
 */
public final class ListenerRegistry {

    private final CustomItemsPlugin plugin;

    private PlayerInteractListener interactListener;
    private PlayerItemConsumeListener consumeListener;
    private EntityDamageByEntityListener damageListener;
    private ProjectileHitListener projectileHitListener;
    private PlayerDropItemListener dropListener;
    private PlayerSwapHandListener swapListener;

    public ListenerRegistry(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers all event listeners with the server.
     */
    public void registerAll() {
        interactListener = new PlayerInteractListener(plugin);
        consumeListener = new PlayerItemConsumeListener(plugin);
        damageListener = new EntityDamageByEntityListener(plugin);
        projectileHitListener = new ProjectileHitListener(plugin);
        dropListener = new PlayerDropItemListener(plugin);
        swapListener = new PlayerSwapHandListener(plugin);

        var pm = plugin.getServer().getPluginManager();
        pm.registerEvents(interactListener, plugin);
        pm.registerEvents(consumeListener, plugin);
        pm.registerEvents(damageListener, plugin);
        pm.registerEvents(projectileHitListener, plugin);
        pm.registerEvents(dropListener, plugin);
        pm.registerEvents(swapListener, plugin);

        plugin.getLogger().info("[CustomItems] All listeners registered.");
    }

    /**
     * Unregisters all event listeners.
     */
    public void unregisterAll() {
        HandlerList.unregisterAll(plugin);
        plugin.getLogger().info("[CustomItems] All listeners unregistered.");
    }
}
