package com.customplugin.customitems;

import com.customplugin.customitems.commands.CustomItemsCommand;
import com.customplugin.customitems.config.ItemRegistry;
import com.customplugin.customitems.cooldown.CooldownManager;
import com.customplugin.customitems.data.DatabaseManager;
import com.customplugin.customitems.hooks.PlaceholderAPIHook;
import com.customplugin.customitems.hooks.VaultHook;
import com.customplugin.customitems.listeners.ListenerRegistry;
import com.customplugin.customitems.projectile.ProjectileManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

/**
 * Main plugin class for CustomItems.
 * Orchestrates startup, shutdown, and all subsystem initialization.
 */
public final class CustomItemsPlugin extends JavaPlugin {

    private static CustomItemsPlugin instance;

    private ItemRegistry itemRegistry;
    private CooldownManager cooldownManager;
    private DatabaseManager databaseManager;
    private ProjectileManager projectileManager;
    private PlaceholderAPIHook placeholderAPIHook;
    private VaultHook vaultHook;
    private ListenerRegistry listenerRegistry;

    private boolean debugMode = false;

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("CustomItems is starting up...");

        // Create default directories
        createPluginDirectories();

        // Save default config
        saveDefaultConfig();

        // Read debug mode from config
        debugMode = getConfig().getBoolean("debug", false);

        // Initialize database
        databaseManager = new DatabaseManager(this);
        try {
            databaseManager.initialize();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize database. Disabling plugin.", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize cooldown manager
        cooldownManager = new CooldownManager(this, databaseManager);

        // Initialize projectile manager
        projectileManager = new ProjectileManager(this);

        // Hook into optional dependencies
        placeholderAPIHook = new PlaceholderAPIHook(this);
        vaultHook = new VaultHook(this);

        // Initialize item registry and load all items
        itemRegistry = new ItemRegistry(this);
        itemRegistry.loadAll();

        // Register all event listeners
        listenerRegistry = new ListenerRegistry(this);
        listenerRegistry.registerAll();

        // Register commands
        CustomItemsCommand command = new CustomItemsCommand(this);
        var cmd = getCommand("ci");
        if (cmd != null) {
            cmd.setExecutor(command);
            cmd.setTabCompleter(command);
        }

        getLogger().info("CustomItems enabled successfully. Loaded "
                + itemRegistry.getLoadedCount() + " item(s).");
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomItems is shutting down...");

        if (cooldownManager != null) {
            cooldownManager.saveAll();
        }

        if (databaseManager != null) {
            databaseManager.close();
        }

        if (listenerRegistry != null) {
            listenerRegistry.unregisterAll();
        }

        instance = null;
        getLogger().info("CustomItems disabled.");
    }

    private void createPluginDirectories() {
        File itemsDir = new File(getDataFolder(), "items");
        if (!itemsDir.exists()) {
            itemsDir.mkdirs();
        }
        File dataDir = new File(getDataFolder(), "data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public static CustomItemsPlugin getInstance() {
        return instance;
    }

    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ProjectileManager getProjectileManager() {
        return projectileManager;
    }

    public PlaceholderAPIHook getPlaceholderAPIHook() {
        return placeholderAPIHook;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public void debugLog(String message) {
        if (debugMode) {
            getLogger().info("[DEBUG] " + message);
        }
    }

    /**
     * Reloads all plugin configuration and items.
     */
    public void reload() {
        reloadConfig();
        debugMode = getConfig().getBoolean("debug", false);
        itemRegistry.loadAll();
        getLogger().info("CustomItems reloaded. Loaded "
                + itemRegistry.getLoadedCount() + " item(s).");
    }
}
