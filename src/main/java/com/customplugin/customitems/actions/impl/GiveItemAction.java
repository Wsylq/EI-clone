package com.customplugin.customitems.actions.impl;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.actions.ActionExecutor;
import com.customplugin.customitems.model.ActionDefinition;
import com.customplugin.customitems.model.CustomItem;
import com.customplugin.customitems.model.ExecutionContext;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

/**
 * Gives the player an item — either a vanilla material or a registered custom item.
 *
 * YAML:
 *   type: GIVE_ITEM
 *   item: DIAMOND            # material name OR custom item id
 *   amount: 1
 *   is_custom: false         # set true if 'item' is a custom item id
 */
public final class GiveItemAction implements ActionExecutor {

    private final CustomItemsPlugin plugin;

    public GiveItemAction(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(ActionDefinition def, ExecutionContext context) {
        Player player = context.getPlayer();
        String itemId   = def.getString("item", "STONE");
        int amount      = Math.max(1, def.getInt("amount", 1));
        boolean isCustom = def.getBoolean("is_custom", false);

        if (isCustom) {
            Optional<CustomItem> ci = plugin.getItemRegistry().getById(itemId);
            if (ci.isEmpty()) {
                plugin.getLogger().warning("[CustomItems] GIVE_ITEM: unknown custom item '" + itemId + "'");
                return;
            }
            ItemStack stack = ci.get().getBaseItem();
            stack.setAmount(amount);
            player.getInventory().addItem(stack);
        } else {
            Material material;
            try {
                material = Material.valueOf(itemId.toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("[CustomItems] GIVE_ITEM: unknown material '" + itemId + "'");
                return;
            }
            player.getInventory().addItem(new ItemStack(material, amount));
        }
    }
}
