package com.customplugin.customitems.actions.impl;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.actions.ActionExecutor;
import com.customplugin.customitems.model.ActionDefinition;
import com.customplugin.customitems.model.ExecutionContext;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Applies a potion effect to the player.
 *
 * YAML:
 *   type: POTION_EFFECT
 *   effect: SPEED
 *   duration: 100    # ticks
 *   amplifier: 1     # 0-indexed (0 = level I)
 *   ambient: false
 *   particles: true
 *   icon: true
 */
public final class PotionEffectAction implements ActionExecutor {

    private final CustomItemsPlugin plugin;

    public PotionEffectAction(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(ActionDefinition def, ExecutionContext context) {
        String effectName = def.getString("effect", "SPEED").toUpperCase();
        int duration  = def.getInt("duration", 100);
        int amplifier = def.getInt("amplifier", 0);
        boolean ambient   = def.getBoolean("ambient", false);
        boolean particles = def.getBoolean("particles", true);
        boolean icon      = def.getBoolean("icon", true);

        PotionEffectType type = PotionEffectType.getByName(effectName);
        if (type == null) {
            plugin.getLogger().warning("[CustomItems] Unknown potion effect: " + effectName);
            return;
        }

        Player player = context.getPlayer();
        PotionEffect effect = new PotionEffect(type, duration, amplifier, ambient, particles, icon);
        player.addPotionEffect(effect);
    }
}
