package com.customplugin.customitems.engine;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.actions.ActionExecutor;
import com.customplugin.customitems.actions.ActionRegistry;
import com.customplugin.customitems.conditions.ConditionEvaluator;
import com.customplugin.customitems.cooldown.CooldownManager;
import com.customplugin.customitems.model.ActionDefinition;
import com.customplugin.customitems.model.Activator;
import com.customplugin.customitems.model.CustomItem;
import com.customplugin.customitems.model.ExecutionContext;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Central execution pipeline.
 *
 * Flow:
 *   1. Resolve CustomItem from player hand via PDC
 *   2. Match activator
 *   3. Evaluate conditions
 *   4. Check / apply cooldown
 *   5. Execute action chain sequentially
 */
public final class ExecutionEngine {

    private final CustomItemsPlugin plugin;
    private final ActionRegistry actionRegistry;
    private final ConditionEvaluator conditionEvaluator;
    private final CooldownManager cooldownManager;

    public ExecutionEngine(CustomItemsPlugin plugin) {
        this.plugin = plugin;
        this.actionRegistry = new ActionRegistry(plugin);
        this.conditionEvaluator = new ConditionEvaluator(plugin);
        this.cooldownManager = plugin.getCooldownManager();
    }

    // -------------------------------------------------------------------------
    // Pipeline entry point
    // -------------------------------------------------------------------------

    /**
     * Runs the full pipeline for a given item + activator combination.
     *
     * @param item      resolved CustomItem
     * @param context   pre-built execution context
     * @return true if execution completed (conditions passed, not on cooldown)
     */
    public boolean execute(CustomItem item, ExecutionContext context) {
        Player player = context.getPlayer();
        String activatorType = context.getActivatorType();

        plugin.debugLog("Pipeline start: item=" + item.getId()
                + " activator=" + activatorType + " player=" + player.getName());

        // --- Permission check ---
        String usePermission = "customitems.use." + item.getId();
        if (plugin.getServer().getPluginManager().getPlugin("CustomItems") != null) {
            if (!player.hasPermission(usePermission) && !player.hasPermission("customitems.admin")) {
                plugin.debugLog("Player lacks permission: " + usePermission);
                return false;
            }
        }

        // --- Activator resolution ---
        Activator activator = item.getActivator(activatorType);
        if (activator == null) {
            plugin.debugLog("No activator found for type: " + activatorType);
            return false;
        }

        // --- Condition evaluation ---
        for (String condition : activator.getConditions()) {
            boolean passed = conditionEvaluator.evaluate(condition, context);
            if (!passed) {
                plugin.debugLog("Condition failed: " + condition);
                return false;
            }
        }

        // --- Cooldown check ---
        if (activator.hasCooldown() && !player.hasPermission("customitems.bypass.cooldown")) {
            long remaining = cooldownManager.getRemainingTicks(player, item.getId(), activatorType);
            if (remaining > 0) {
                String msg = activator.getCooldownMessage()
                        .replace("%remaining%", String.valueOf((int) Math.ceil(remaining / 20.0)));
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(msg));
                plugin.debugLog("On cooldown: " + remaining + " ticks remaining.");
                return false;
            }
            cooldownManager.setCooldown(player, item.getId(), activatorType, activator.getCooldownTicks());
        }

        // --- Action chain ---
        executeActions(activator.getActions(), context);

        plugin.debugLog("Pipeline complete: item=" + item.getId());
        return true;
    }

    // -------------------------------------------------------------------------
    // Action chain execution
    // -------------------------------------------------------------------------

    /**
     * Executes a list of actions sequentially.
     * DELAY actions schedule the rest of the chain asynchronously.
     */
    public void executeActions(List<ActionDefinition> actions, ExecutionContext context) {
        for (int i = 0; i < actions.size(); i++) {
            ActionDefinition def = actions.get(i);

            if (def.getType().equals("DELAY")) {
                long ticks = def.getInt("ticks", 20);
                final List<ActionDefinition> remaining = actions.subList(i + 1, actions.size());
                final ExecutionContext ctx = context;
                plugin.getServer().getScheduler().runTaskLater(plugin, () ->
                        executeActions(remaining, ctx), ticks);
                return; // chain continues after delay via scheduler
            }

            if (def.getType().equals("IF")) {
                handleIf(def, context);
                continue;
            }

            ActionExecutor executor = actionRegistry.getExecutor(def.getType());
            if (executor == null) {
                plugin.getLogger().warning("[CustomItems] Unknown action type: " + def.getType());
                continue;
            }

            try {
                executor.execute(def, context);
                plugin.debugLog("Executed action: " + def.getType());
            } catch (Exception e) {
                plugin.getLogger().warning("[CustomItems] Error executing action "
                        + def.getType() + ": " + e.getMessage());
            }
        }
    }

    private void handleIf(ActionDefinition def, ExecutionContext context) {
        String condition = def.getString("condition", "");
        boolean result = conditionEvaluator.evaluate(condition, context);
        plugin.debugLog("IF condition '" + condition + "' → " + result);

        if (result) {
            if (!def.getThenActions().isEmpty()) {
                executeActions(def.getThenActions(), context);
            }
        } else {
            if (!def.getElseActions().isEmpty()) {
                executeActions(def.getElseActions(), context);
            }
        }
    }

    public ActionRegistry getActionRegistry() {
        return actionRegistry;
    }

    public ConditionEvaluator getConditionEvaluator() {
        return conditionEvaluator;
    }
}
