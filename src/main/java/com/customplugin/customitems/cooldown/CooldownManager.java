package com.customplugin.customitems.cooldown;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.data.DatabaseManager;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages per-player, per-item, per-activator cooldowns.
 *
 * Storage:
 *   In-memory ConcurrentHashMap (primary)
 *   Optional persistence via DatabaseManager
 *
 * Key format: uuid:itemId:activatorType
 */
public final class CooldownManager {

    private final CustomItemsPlugin plugin;
    private final DatabaseManager databaseManager;

    /**
     * Maps cooldown key → expiry time in milliseconds (System.currentTimeMillis).
     */
    private final Map<String, Long> cooldowns = new ConcurrentHashMap<>();

    public CooldownManager(CustomItemsPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;

        // Load persisted cooldowns from DB
        if (plugin.getConfig().getBoolean("persist_cooldowns", false)) {
            loadFromDatabase();
        }
    }

    // -------------------------------------------------------------------------
    // Core API
    // -------------------------------------------------------------------------

    /**
     * Returns remaining ticks on the cooldown, or 0 if not on cooldown.
     */
    public long getRemainingTicks(Player player, String itemId, String activatorType) {
        String key = buildKey(player.getUniqueId(), itemId, activatorType);
        Long expiry = cooldowns.get(key);
        if (expiry == null) return 0;

        long remainingMs = expiry - System.currentTimeMillis();
        if (remainingMs <= 0) {
            cooldowns.remove(key);
            return 0;
        }
        return msToTicks(remainingMs);
    }

    /**
     * Sets the cooldown for the given player/item/activator combination.
     *
     * @param durationTicks duration in server ticks
     */
    public void setCooldown(Player player, String itemId, String activatorType, long durationTicks) {
        String key = buildKey(player.getUniqueId(), itemId, activatorType);
        long expiryMs = System.currentTimeMillis() + ticksToMs(durationTicks);
        cooldowns.put(key, expiryMs);

        if (plugin.getConfig().getBoolean("persist_cooldowns", false)) {
            databaseManager.saveCooldownAsync(key, expiryMs);
        }
    }

    /**
     * Clears the cooldown for the given player/item/activator combination.
     */
    public void clearCooldown(Player player, String itemId, String activatorType) {
        cooldowns.remove(buildKey(player.getUniqueId(), itemId, activatorType));
    }

    /**
     * Clears all cooldowns for a player.
     */
    public void clearAll(Player player) {
        String prefix = player.getUniqueId().toString() + ":";
        cooldowns.keySet().removeIf(k -> k.startsWith(prefix));
    }

    public boolean isOnCooldown(Player player, String itemId, String activatorType) {
        return getRemainingTicks(player, itemId, activatorType) > 0;
    }

    // -------------------------------------------------------------------------
    // Persistence
    // -------------------------------------------------------------------------

    /**
     * Persists all in-memory cooldowns to the database on shutdown.
     */
    public void saveAll() {
        if (!plugin.getConfig().getBoolean("persist_cooldowns", false)) return;
        long now = System.currentTimeMillis();
        cooldowns.forEach((key, expiry) -> {
            if (expiry > now) { // only persist still-active cooldowns
                databaseManager.saveCooldownAsync(key, expiry);
            }
        });
    }

    private void loadFromDatabase() {
        Map<String, Long> persisted = databaseManager.loadAllCooldowns();
        long now = System.currentTimeMillis();
        persisted.forEach((key, expiry) -> {
            if (expiry > now) {
                cooldowns.put(key, expiry);
            }
        });
        plugin.getLogger().info("[CustomItems] Loaded " + cooldowns.size() + " persisted cooldown(s).");
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    private String buildKey(UUID uuid, String itemId, String activatorType) {
        return uuid.toString() + ":" + itemId + ":" + activatorType.toUpperCase();
    }

    private static long ticksToMs(long ticks) {
        return ticks * 50L; // 1 tick = 50ms at 20 TPS
    }

    private static long msToTicks(long ms) {
        return ms / 50L;
    }
}
