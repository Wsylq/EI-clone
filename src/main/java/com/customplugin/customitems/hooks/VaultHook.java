package com.customplugin.customitems.hooks;

import com.customplugin.customitems.CustomItemsPlugin;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Hook for Vault economy integration.
 * Safely degrades if Vault is not present on the server.
 */
public final class VaultHook {

    private final CustomItemsPlugin plugin;
    private Object economy = null; // net.milkbowl.vault.economy.Economy
    private boolean enabled = false;

    public VaultHook(CustomItemsPlugin plugin) {
        this.plugin = plugin;
        Plugin vault = plugin.getServer().getPluginManager().getPlugin("Vault");
        if (vault != null && vault.isEnabled()) {
            setupEconomy();
        } else {
            plugin.getLogger().info("[CustomItems] Vault not found. Economy support disabled.");
        }
    }

    private void setupEconomy() {
        try {
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            RegisteredServiceProvider<?> rsp =
                    plugin.getServer().getServicesManager().getRegistration(
                            (Class) economyClass);
            if (rsp != null) {
                economy = rsp.getProvider();
                enabled = true;
                plugin.getLogger().info("[CustomItems] Vault economy hooked successfully.");
            } else {
                plugin.getLogger().warning("[CustomItems] Vault found but no economy provider registered.");
            }
        } catch (ClassNotFoundException e) {
            plugin.getLogger().warning("[CustomItems] Vault classes not found: " + e.getMessage());
        }
    }

    /**
     * @return true if Vault economy is available.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the balance of a player.
     *
     * @param player the player
     * @return balance, or 0 if Vault is unavailable
     */
    public double getBalance(Player player) {
        if (!enabled || economy == null) return 0;
        try {
            java.lang.reflect.Method method = economy.getClass().getMethod("getBalance", Player.class);
            Object result = method.invoke(economy, player);
            return result instanceof Number ? ((Number) result).doubleValue() : 0;
        } catch (Exception e) {
            plugin.debugLog("Vault getBalance error: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Withdraws an amount from a player's balance.
     *
     * @param player the player
     * @param amount the amount to withdraw
     * @return true if successful
     */
    public boolean withdraw(Player player, double amount) {
        if (!enabled || economy == null) return false;
        try {
            java.lang.reflect.Method method = economy.getClass().getMethod("withdrawPlayer", Player.class, double.class);
            Object result = method.invoke(economy, player, amount);
            // EconomyResponse.transactionSuccess()
            java.lang.reflect.Method successMethod = result.getClass().getMethod("transactionSuccess");
            return (boolean) successMethod.invoke(result);
        } catch (Exception e) {
            plugin.debugLog("Vault withdraw error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deposits an amount to a player's balance.
     *
     * @param player the player
     * @param amount the amount to deposit
     * @return true if successful
     */
    public boolean deposit(Player player, double amount) {
        if (!enabled || economy == null) return false;
        try {
            java.lang.reflect.Method method = economy.getClass().getMethod("depositPlayer", Player.class, double.class);
            Object result = method.invoke(economy, player, amount);
            java.lang.reflect.Method successMethod = result.getClass().getMethod("transactionSuccess");
            return (boolean) successMethod.invoke(result);
        } catch (Exception e) {
            plugin.debugLog("Vault deposit error: " + e.getMessage());
            return false;
        }
    }
}
