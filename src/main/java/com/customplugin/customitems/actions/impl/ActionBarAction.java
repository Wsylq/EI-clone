package com.customplugin.customitems.actions.impl;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.actions.ActionExecutor;
import com.customplugin.customitems.model.ActionDefinition;
import com.customplugin.customitems.model.ExecutionContext;
import com.customplugin.customitems.util.TextUtil;
import net.kyori.adventure.text.Component;

/**
 * Sends an action bar message to the player.
 *
 * YAML:
 *   type: ACTIONBAR
 *   message: "&eThis is an action bar"
 */
public final class ActionBarAction implements ActionExecutor {

    private final CustomItemsPlugin plugin;

    public ActionBarAction(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(ActionDefinition def, ExecutionContext context) {
        String raw = def.getString("message", "");
        String resolved = TextUtil.resolvePlaceholders(raw, context, plugin);
        Component component = TextUtil.parse(resolved);
        context.getPlayer().sendActionBar(component);
    }
}
