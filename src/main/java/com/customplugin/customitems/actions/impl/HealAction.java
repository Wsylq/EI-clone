package com.customplugin.customitems.actions.impl;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.actions.ActionExecutor;
import com.customplugin.customitems.model.ActionDefinition;
import com.customplugin.customitems.model.ExecutionContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

/**
 * Heals the player by a given amount.
 *
 * YAML:
 *   type: HEAL
 *   amount: 4.0
 */
public final class HealAction implements ActionExecutor {

    private final CustomItemsPlugin plugin;

    public HealAction(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(ActionDefinition def, ExecutionContext context) {
        double amount = def.getDouble("amount", 2.0);
        Player player = context.getPlayer();

        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double maxHealth = maxHealthAttr != null ? maxHealthAttr.getValue() : 20.0;
        double newHealth = Math.min(player.getHealth() + amount, maxHealth);
        player.setHealth(newHealth);
    }
}
