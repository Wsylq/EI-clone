package com.customplugin.customitems.actions.impl;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.actions.ActionExecutor;
import com.customplugin.customitems.model.ActionDefinition;
import com.customplugin.customitems.model.ExecutionContext;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

/**
 * Spawns a vanilla entity at the player's location.
 *
 * YAML:
 *   type: SPAWN_ENTITY
 *   entity: ZOMBIE
 *   offset_x: 0.0
 *   offset_y: 0.0
 *   offset_z: 0.0
 */
public final class SpawnEntityAction implements ActionExecutor {

    private final CustomItemsPlugin plugin;

    public SpawnEntityAction(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(ActionDefinition def, ExecutionContext context) {
        String entityName = def.getString("entity", "ZOMBIE").toUpperCase();
        double offX = def.getDouble("offset_x", 0);
        double offY = def.getDouble("offset_y", 0);
        double offZ = def.getDouble("offset_z", 0);

        EntityType entityType;
        try {
            entityType = EntityType.valueOf(entityName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("[CustomItems] SPAWN_ENTITY: unknown entity '" + entityName + "'");
            return;
        }

        Location loc = context.getPlayer().getLocation().add(offX, offY, offZ);
        loc.getWorld().spawnEntity(loc, entityType);
    }
}
