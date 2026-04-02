package com.customplugin.customitems.hooks;

import com.customplugin.customitems.CustomItemsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Hook for PlaceholderAPI integration.
 * Safely degrades if PlaceholderAPI is not present on the server.
 */
public final class PlaceholderAPIHook {

    private final CustomItemsPlugin plugin;
    private boolean enabled = false;

    public PlaceholderAPIHook(CustomItemsPlugin plugin) {
        this.plugin = plugin;
        Plugin papi = plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI");
        if (papi != null && papi.isEnabled()) {
            enabled = true;
            plugin.getLogger().info("[CustomItems] PlaceholderAPI found and hooked.");
        } else {
            plugin.getLogger().info("[CustomItems] PlaceholderAPI not found. Placeholder support disabled.");
        }
    }

    /**
     * @return true if PlaceholderAPI is present and enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Applies PlaceholderAPI placeholders to the given text for the given player.
     * Falls back to returning the raw text if PAPI is unavailable.
     */
    public String setPlaceholders(Player player, String text) {
        if (!enabled || player == null || text == null) return text == null ? "" : text;
        try {
            // Reflectively invoke PlaceholderAPI to avoid a hard dependency
            Class<?> papiClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            java.lang.reflect.Method method = papiClass.getMethod("setPlaceholders", Player.class, String.class);
            Object result = method.invoke(null, player, text);
            return result != null ? result.toString() : text;
        } catch (Exception e) {
            plugin.debugLog("PlaceholderAPI reflection error: " + e.getMessage());
            return text;
        }
    }
}
