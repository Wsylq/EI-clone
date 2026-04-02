package com.customplugin.customitems.actions.impl;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.actions.ActionExecutor;
import com.customplugin.customitems.model.ActionDefinition;
import com.customplugin.customitems.model.ExecutionContext;
import org.bukkit.Location;
import org.bukkit.Particle;

/**
 * Spawns particles at the player's location.
 *
 * YAML:
 *   type: PARTICLE
 *   particle: FLAME
 *   count: 20
 *   offset_x: 0.5
 *   offset_y: 0.5
 *   offset_z: 0.5
 *   speed: 0.05
 */
public final class ParticleAction implements ActionExecutor {

    private final CustomItemsPlugin plugin;

    public ParticleAction(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(ActionDefinition def, ExecutionContext context) {
        String particleName = def.getString("particle", "FLAME").toUpperCase();
        int count    = def.getInt("count", 10);
        double offX  = def.getDouble("offset_x", 0.5);
        double offY  = def.getDouble("offset_y", 0.5);
        double offZ  = def.getDouble("offset_z", 0.5);
        double speed = def.getDouble("speed", 0.05);

        Particle particle;
        try {
            particle = Particle.valueOf(particleName);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("[CustomItems] Unknown particle: " + particleName);
            return;
        }

        Location loc = context.getPlayer().getLocation().add(0, 1, 0);
        loc.getWorld().spawnParticle(particle, loc, count, offX, offY, offZ, speed);
    }
}
