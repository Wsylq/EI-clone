package com.customplugin.customitems.listeners;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.engine.ExecutionEngine;
import com.customplugin.customitems.model.ExecutionContext;
import com.customplugin.customitems.projectile.ProjectileManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

/**
 * Handles on-hit callbacks for custom projectiles launched via PROJECTILE action.
 */
public final class ProjectileHitListener implements Listener {

    private final CustomItemsPlugin plugin;
    private final ExecutionEngine engine;

    public ProjectileHitListener(CustomItemsPlugin plugin) {
        this.plugin = plugin;
        this.engine = new ExecutionEngine(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        ProjectileManager pm = plugin.getProjectileManager();

        if (!pm.isTracked(projectile.getUniqueId())) return;

        ProjectileManager.ProjectileEntry entry = pm.getAndRemove(projectile.getUniqueId());
        if (entry == null) return;

        if (!(projectile.getShooter() instanceof Player player)) return;

        Entity hitEntity = event.getHitEntity();
        ExecutionContext context = ExecutionContext.builder(player)
                .activatorType("PROJECTILE_HIT")
                .targetEntity(hitEntity)
                .location(projectile.getLocation())
                .build();

        engine.executeActions(entry.definition().getOnHitActions(), context);
    }
}
