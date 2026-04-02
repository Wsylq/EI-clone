package com.customplugin.customitems.actions.impl;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.actions.ActionExecutor;
import com.customplugin.customitems.model.ActionDefinition;
import com.customplugin.customitems.model.ExecutionContext;
import com.customplugin.customitems.util.TextUtil;

/**
 * Sets a context variable that can be referenced in subsequent actions.
 *
 * YAML:
 *   type: SET_VARIABLE
 *   name: myVar
 *   value: "hello %player_name%"
 */
public final class SetVariableAction implements ActionExecutor {

    private final CustomItemsPlugin plugin;

    public SetVariableAction(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(ActionDefinition def, ExecutionContext context) {
        String name  = def.getString("name", "");
        String value = def.getString("value", "");

        if (name.isBlank()) {
            plugin.getLogger().warning("[CustomItems] SET_VARIABLE: missing 'name' field.");
            return;
        }

        String resolved = TextUtil.resolvePlaceholders(value, context, plugin);
        context.setVariable(name, resolved);
        plugin.debugLog("Set variable '" + name + "' = '" + resolved + "'");
    }
}
