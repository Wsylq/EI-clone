package com.customplugin.customitems.actions.impl;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.actions.ActionExecutor;
import com.customplugin.customitems.model.ActionDefinition;
import com.customplugin.customitems.model.ExecutionContext;
import com.customplugin.customitems.projectile.ProjectileDefinition;
import com.customplugin.customitems.projectile.ProjectileManager;

/**
 * Launches a projectile defined in the item's projectile section or inline.
 *
 * YAML:
 *   type: PROJECTILE
 *   projectile: fireball     # references a key in the item's projectiles: map
 *   # OR inline definition:
 *   entity: SMALL_FIREBALL
 *   speed: 1.5
 *   gravity: false
 */
public final class ProjectileAction implements ActionExecutor {

    private final CustomItemsPlugin plugin;

    public ProjectileAction(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(ActionDefinition def, ExecutionContext context) {
        ProjectileManager mgr = plugin.getProjectileManager();

        String projectileId = def.getString("projectile", null);

        if (projectileId != null) {
            // Named projectile — looked up from the registry
            ProjectileDefinition projDef = mgr.getDefinition(projectileId);
            if (projDef == null) {
                plugin.getLogger().warning("[CustomItems] PROJECTILE: unknown projectile '" + projectileId + "'");
                return;
            }
            mgr.launch(projDef, context);
        } else {
            // Inline definition
            String entityName = def.getString("entity", "SNOWBALL").toUpperCase();
            double speed   = def.getDouble("speed", 1.5);
            boolean gravity = def.getBoolean("gravity", true);
            boolean homing  = def.getBoolean("homing", false);

            ProjectileDefinition inline = new ProjectileDefinition(
                    "_inline_", entityName, speed, gravity, homing, null
            );
            mgr.launch(inline, context);
        }
    }
}
