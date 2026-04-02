package com.customplugin.customitems.actions.impl;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.actions.ActionExecutor;
import com.customplugin.customitems.model.ActionDefinition;
import com.customplugin.customitems.model.ExecutionContext;
import com.customplugin.customitems.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Executes a command as either the player or the console.
 *
 * YAML:
 *   type: COMMAND
 *   command: "say Hello %player_name%"
 *   executor: CONSOLE   # CONSOLE | PLAYER (default: PLAYER)
 */
public final class CommandAction implements ActionExecutor {

    private final CustomItemsPlugin plugin;

    public CommandAction(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(ActionDefinition def, ExecutionContext context) {
        Player player = context.getPlayer();
        String raw = def.getString("command", "");
        String resolved = TextUtil.resolvePlaceholders(raw, context, plugin);

        // Strip leading slash if present
        if (resolved.startsWith("/")) {
            resolved = resolved.substring(1);
        }

        String executorType = def.getString("executor", "PLAYER").toUpperCase();

        final String cmd = resolved;
        if (executorType.equals("CONSOLE")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        } else {
            Bukkit.dispatchCommand(player, cmd);
        }
    }
}
