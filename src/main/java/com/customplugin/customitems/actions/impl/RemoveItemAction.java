package com.customplugin.customitems.actions.impl;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.actions.ActionExecutor;
import com.customplugin.customitems.model.ActionDefinition;
import com.customplugin.customitems.model.ExecutionContext;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Removes items from the player's inventory by material type.
 *
 * YAML:
 *   type: REMOVE_ITEM
 *   item: DIAMOND
 *   amount: 1
 */
public final class RemoveItemAction implements ActionExecutor {

    private final CustomItemsPlugin plugin;

    public RemoveItemAction(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(ActionDefinition def, ExecutionContext context) {
        Player player = context.getPlayer();
        String itemName = def.getString("item", "STONE").toUpperCase();
        int amount = Math.max(1, def.getInt("amount", 1));

        Material material;
        try {
            material = Material.valueOf(itemName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("[CustomItems] REMOVE_ITEM: unknown material '" + itemName + "'");
            return;
        }

        int toRemove = amount;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null || stack.getType() != material) continue;
            if (stack.getAmount() <= toRemove) {
                toRemove -= stack.getAmount();
                stack.setAmount(0);
            } else {
                stack.setAmount(stack.getAmount() - toRemove);
                toRemove = 0;
            }
            if (toRemove <= 0) break;
        }
    }
}
