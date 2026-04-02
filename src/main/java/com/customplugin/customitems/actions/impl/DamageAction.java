package com.customplugin.customitems.actions.impl;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.actions.ActionExecutor;
import com.customplugin.customitems.model.ActionDefinition;
import com.customplugin.customitems.model.ExecutionContext;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * Deals damage to the player or the target entity.
 *
 * YAML:
 *   type: DAMAGE
 *   amount: 4.0
 *   target: SELF    # SELF | TARGET (default: SELF)
 */
public final class DamageAction implements ActionExecutor {

    private final CustomItemsPlugin plugin;

    public DamageAction(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(ActionDefinition def, ExecutionContext context) {
        double amount = def.getDouble("amount", 1.0);
        String target = def.getString("target", "SELF").toUpperCase();

        if (target.equals("TARGET")) {
            Entity entity = context.getTargetEntity();
            if (entity instanceof LivingEntity living) {
                living.damage(amount, context.getPlayer());
            }
        } else {
            context.getPlayer().damage(amount);
        }
    }
}
