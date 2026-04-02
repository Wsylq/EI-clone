package com.customplugin.customitems.actions.impl;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.actions.ActionExecutor;
import com.customplugin.customitems.model.ActionDefinition;
import com.customplugin.customitems.model.ExecutionContext;
import com.customplugin.customitems.util.TextUtil;
import org.bukkit.entity.Player;

/**
 * Displays a title/subtitle to the player.
 *
 * YAML:
 *   type: TITLE
 *   title: "&6Welcome!"
 *   subtitle: "&7Enjoy your stay"
 *   fade_in: 10      # ticks
 *   stay: 60         # ticks
 *   fade_out: 10     # ticks
 */
public final class TitleAction implements ActionExecutor {

    private final CustomItemsPlugin plugin;

    public TitleAction(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(ActionDefinition def, ExecutionContext context) {
        Player player = context.getPlayer();

        String rawTitle    = TextUtil.resolvePlaceholders(def.getString("title", ""), context, plugin);
        String rawSubtitle = TextUtil.resolvePlaceholders(def.getString("subtitle", ""), context, plugin);

        String title    = TextUtil.colorize(rawTitle);
        String subtitle = TextUtil.colorize(rawSubtitle);

        int fadeIn  = def.getInt("fade_in", 10);
        int stay    = def.getInt("stay", 60);
        int fadeOut = def.getInt("fade_out", 10);

        // Use Bukkit's sendTitle (compatible with all Spigot/Paper versions)
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }
}
