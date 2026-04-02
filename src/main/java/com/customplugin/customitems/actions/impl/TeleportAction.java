package com.customplugin.customitems.actions.impl;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.actions.ActionExecutor;
import com.customplugin.customitems.model.ActionDefinition;
import com.customplugin.customitems.model.ExecutionContext;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Teleports the player to a fixed location or to the target block/entity.
 *
 * YAML:
 *   type: TELEPORT
 *   target: BLOCK        # BLOCK | ENTITY | LOCATION
 *   # If LOCATION:
 *   world: world
 *   x: 0.0
 *   y: 64.0
 *   z: 0.0
 *   yaw: 0.0
 *   pitch: 0.0
 */
public final class TeleportAction implements ActionExecutor {

    private final CustomItemsPlugin plugin;

    public TeleportAction(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(ActionDefinition def, ExecutionContext context) {
        Player player = context.getPlayer();
        String target = def.getString("target", "LOCATION").toUpperCase();

        switch (target) {
            case "BLOCK" -> {
                if (context.getTargetBlock() != null) {
                    player.teleport(context.getTargetBlock().getLocation().add(0.5, 1, 0.5));
                }
            }
            case "ENTITY" -> {
                if (context.getTargetEntity() != null) {
                    player.teleport(context.getTargetEntity().getLocation());
                }
            }
            default -> {
                String worldName = def.getString("world", player.getWorld().getName());
                World world = plugin.getServer().getWorld(worldName);
                if (world == null) {
                    plugin.getLogger().warning("[CustomItems] TELEPORT: unknown world '" + worldName + "'");
                    return;
                }
                double x     = def.getDouble("x", 0);
                double y     = def.getDouble("y", 64);
                double z     = def.getDouble("z", 0);
                float yaw    = (float) def.getDouble("yaw", 0);
                float pitch  = (float) def.getDouble("pitch", 0);
                player.teleport(new Location(world, x, y, z, yaw, pitch));
            }
        }
    }
}
