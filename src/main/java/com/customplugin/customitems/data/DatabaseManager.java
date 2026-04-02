package com.customplugin.customitems.data;

import com.customplugin.customitems.CustomItemsPlugin;
import org.bukkit.Bukkit;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Manages persistent cooldown storage using a plain binary file.
 *
 * This replaces the original SQLite-based implementation which caused
 * UnsatisfiedLinkError when sqlite-jdbc's native library was relocated
 * via the Maven Shade plugin (the native extractor cannot find itself
 * under the remapped package name).
 *
 * Storage format: Java serialized HashMap<String, Long> written to
 *   plugins/CustomItems/data/cooldowns.dat
 *
 * All disk I/O is performed asynchronously so the main thread is never
 * blocked.
 */
public final class DatabaseManager {

    private final CustomItemsPlugin plugin;

    /** In-memory store — always authoritative during runtime. */
    private final ConcurrentHashMap<String, Long> cooldowns = new ConcurrentHashMap<>();

    private File dataFile;

    public DatabaseManager(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    // ---------------------------------------------------------------------------
    // Initialization
    // ---------------------------------------------------------------------------

    public void initialize() throws Exception {
        File dataDir = new File(plugin.getDataFolder(), "data");
        if (!dataDir.exists() && !dataDir.mkdirs()) {
            throw new IOException("Could not create data directory: " + dataDir.getAbsolutePath());
        }

        dataFile = new File(dataDir, "cooldowns.dat");

        // Load persisted cooldowns (prune expired ones immediately)
        loadFromDisk();

        plugin.getLogger().info("[CustomItems] Database initialized (flat-file).");
    }

    // ---------------------------------------------------------------------------
    // Cooldown persistence (public API — mirrors the old SQLite API)
    // ---------------------------------------------------------------------------

    /**
     * Persists a cooldown entry asynchronously.
     *
     * @param key      composite cooldown key (e.g. "playerUUID:itemId:trigger")
     * @param expiryMs absolute expiry timestamp in milliseconds
     */
    public void saveCooldownAsync(String key, long expiryMs) {
        cooldowns.put(key, expiryMs);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveToDisk);
    }

    /**
     * Returns all cooldowns that have not yet expired.
     *
     * @return mutable map of key → expiry-ms
     */
    public Map<String, Long> loadAllCooldowns() {
        long now = System.currentTimeMillis();
        Map<String, Long> active = new HashMap<>();
        for (Map.Entry<String, Long> entry : cooldowns.entrySet()) {
            if (entry.getValue() > now) {
                active.put(entry.getKey(), entry.getValue());
            }
        }
        return active;
    }

    /**
     * Removes a cooldown entry asynchronously.
     *
     * @param key the cooldown key to remove
     */
    public void deleteCooldown(String key) {
        cooldowns.remove(key);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, this::saveToDisk);
    }

    // ---------------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------------

    /**
     * Flush all cooldowns to disk synchronously.
     * Called from {@code onDisable()} on the main thread after all async
     * tasks have had a chance to complete.
     */
    public void close() {
        saveToDisk();
        plugin.getLogger().info("[CustomItems] Database connection closed.");
    }

    // ---------------------------------------------------------------------------
    // Internal disk I/O
    // ---------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private void loadFromDisk() {
        if (dataFile == null || !dataFile.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
            Object obj = ois.readObject();
            if (obj instanceof Map<?, ?> raw) {
                long now = System.currentTimeMillis();
                for (Map.Entry<?, ?> entry : raw.entrySet()) {
                    if (entry.getKey() instanceof String key
                            && entry.getValue() instanceof Long expiry
                            && expiry > now) {
                        cooldowns.put(key, expiry);
                    }
                }
            }
        } catch (EOFException | FileNotFoundException ignored) {
            // Empty / missing file — perfectly normal on first run
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING,
                    "[CustomItems] Could not load cooldowns.dat — starting fresh.", e);
        }
    }

    private void saveToDisk() {
        if (dataFile == null) return;

        // Snapshot only the non-expired entries
        long now = System.currentTimeMillis();
        HashMap<String, Long> snapshot = new HashMap<>();
        for (Map.Entry<String, Long> entry : cooldowns.entrySet()) {
            if (entry.getValue() > now) {
                snapshot.put(entry.getKey(), entry.getValue());
            }
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))) {
            oos.writeObject(snapshot);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING,
                    "[CustomItems] Could not save cooldowns.dat.", e);
        }
    }
}
