package com.customplugin.customitems.data;

import com.customplugin.customitems.CustomItemsPlugin;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Manages SQLite (default) or MySQL persistence.
 *
 * Tables:
 *   cooldowns  (cooldown_key TEXT PK, expiry_ms INTEGER)
 */
public final class DatabaseManager {

    private final CustomItemsPlugin plugin;
    private Connection connection;
    private boolean useMysql;

    public DatabaseManager(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    // -------------------------------------------------------------------------
    // Initialization
    // -------------------------------------------------------------------------

    public void initialize() throws Exception {
        useMysql = plugin.getConfig().getBoolean("database.mysql.enabled", false);

        if (useMysql) {
            initMySQL();
        } else {
            initSQLite();
        }

        createTables();
        plugin.getLogger().info("[CustomItems] Database initialized (" + (useMysql ? "MySQL" : "SQLite") + ").");
    }

    private void initSQLite() throws Exception {
        File dataDir = new File(plugin.getDataFolder(), "data");
        dataDir.mkdirs();
        File dbFile = new File(dataDir, "customitems.db");

        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

        // WAL mode for better concurrent read performance
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA journal_mode=WAL;");
        }
    }

    private void initMySQL() throws Exception {
        String host     = plugin.getConfig().getString("database.mysql.host", "localhost");
        int port        = plugin.getConfig().getInt("database.mysql.port", 3306);
        String database = plugin.getConfig().getString("database.mysql.database", "customitems");
        String username = plugin.getConfig().getString("database.mysql.username", "root");
        String password = plugin.getConfig().getString("database.mysql.password", "");

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                + "?useSSL=false&autoReconnect=true&characterEncoding=utf8";

        Class.forName("com.mysql.cj.jdbc.Driver");
        connection = DriverManager.getConnection(url, username, password);
    }

    private void createTables() throws SQLException {
        String createCooldowns = """
                CREATE TABLE IF NOT EXISTS cooldowns (
                    cooldown_key TEXT NOT NULL PRIMARY KEY,
                    expiry_ms    INTEGER NOT NULL
                );
                """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createCooldowns);
        }
    }

    // -------------------------------------------------------------------------
    // Cooldown persistence
    // -------------------------------------------------------------------------

    public void saveCooldownAsync(String key, long expiryMs) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                saveCooldown(key, expiryMs);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "[CustomItems] Failed to save cooldown: " + key, e);
            }
        });
    }

    private void saveCooldown(String key, long expiryMs) throws SQLException {
        String sql = "INSERT OR REPLACE INTO cooldowns (cooldown_key, expiry_ms) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setLong(2, expiryMs);
            ps.executeUpdate();
        }
    }

    public Map<String, Long> loadAllCooldowns() {
        Map<String, Long> result = new HashMap<>();
        String sql = "SELECT cooldown_key, expiry_ms FROM cooldowns WHERE expiry_ms > ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, System.currentTimeMillis());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("cooldown_key"), rs.getLong("expiry_ms"));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "[CustomItems] Failed to load cooldowns.", e);
        }
        return result;
    }

    public void deleteCooldown(String key) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String sql = "DELETE FROM cooldowns WHERE cooldown_key = ?";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, key);
                    ps.executeUpdate();
                }
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "[CustomItems] Failed to delete cooldown: " + key, e);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    public void close() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("[CustomItems] Database connection closed.");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "[CustomItems] Error closing database.", e);
            }
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
