package com.customplugin.customitems.util;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.model.ExecutionContext;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

/**
 * Utility class for text parsing, color-code translation, and placeholder resolution.
 */
public final class TextUtil {

    private TextUtil() {}

    /**
     * Parses a string with legacy '&' color codes into a Component.
     */
    public static Component parse(String raw) {
        if (raw == null) return Component.empty();
        return LegacyComponentSerializer.legacyAmpersand().deserialize(raw);
    }

    /**
     * Translates legacy '&' color codes into Bukkit '§' codes for sendMessage(String).
     */
    public static String colorize(String raw) {
        if (raw == null) return "";
        return raw.replace("&", "\u00a7");
    }

    /**
     * Resolves PlaceholderAPI placeholders (if available) and context variables
     * in the given string for the player in the context.
     */
    public static String resolvePlaceholders(String text, ExecutionContext context, CustomItemsPlugin plugin) {
        if (text == null) return "";

        // Replace context variables {varName}
        for (var entry : context.getVariables().entrySet()) {
            // Fix: convert Object value to String before passing to replace()
            text = text.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }

        // Replace built-in placeholders
        if (context.getPlayer() != null) {
            text = text.replace("%player_name%", context.getPlayer().getName());
            text = text.replace("%player%", context.getPlayer().getName());
            text = text.replace("%world%", context.getPlayer().getWorld().getName());
            text = text.replace("%player_health%",
                    String.valueOf((int) context.getPlayer().getHealth()));
            text = text.replace("%player_level%",
                    String.valueOf(context.getPlayer().getLevel()));
            text = text.replace("%player_food%",
                    String.valueOf(context.getPlayer().getFoodLevel()));
            text = text.replace("%player_is_sneaking%",
                    String.valueOf(context.getPlayer().isSneaking()));
            text = text.replace("%player_is_flying%",
                    String.valueOf(context.getPlayer().isFlying()));
        }

        // Hook into PlaceholderAPI if available
        if (plugin.getPlaceholderAPIHook() != null && plugin.getPlaceholderAPIHook().isEnabled()) {
            text = plugin.getPlaceholderAPIHook().setPlaceholders(context.getPlayer(), text);
        }

        return text;
    }
}
