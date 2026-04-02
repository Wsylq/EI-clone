package com.customplugin.customitems.actions.impl;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.actions.ActionExecutor;
import com.customplugin.customitems.model.ActionDefinition;
import com.customplugin.customitems.model.ExecutionContext;
import com.customplugin.customitems.util.TextUtil;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

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
        Player player = context.getPlayer();
        String raw = def.getString("message", "");
        String resolved = TextUtil.resolvePlaceholders(raw, context, plugin);
        String colorized = TextUtil.colorize(resolved);

        // Use Spigot/BungeeCord API for action bar (compatible with older Bukkit versions)
        player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(colorized)
        );
    }
}
