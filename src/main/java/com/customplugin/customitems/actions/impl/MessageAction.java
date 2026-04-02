package com.customplugin.customitems.actions.impl;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.actions.ActionExecutor;
import com.customplugin.customitems.model.ActionDefinition;
import com.customplugin.customitems.model.ExecutionContext;
import com.customplugin.customitems.util.TextUtil;

/**
 * Sends a chat message to the player.
 *
 * YAML:
 *   type: MESSAGE
 *   message: "&aHello %player_name%!"
 */
public final class MessageAction implements ActionExecutor {

    private final CustomItemsPlugin plugin;

    public MessageAction(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(ActionDefinition def, ExecutionContext context) {
        String raw = def.getString("message", "");
        String resolved = TextUtil.resolvePlaceholders(raw, context, plugin);
        context.getPlayer().sendMessage(TextUtil.parse(resolved));
    }
}
