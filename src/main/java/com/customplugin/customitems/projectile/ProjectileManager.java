package com.customplugin.customitems.projectile;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.model.ExecutionContext;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages custom projectile definitions and launching.
 * Tracks live projectiles to apply on-hit callbacks.
 */
public final class ProjectileManager {

    private final CustomItemsPlugin plugin;

    /** Registry of named projectile definitions (loaded from item YAMLs). */
    private final Map<String, ProjectileDefinition> definitions = new HashMap<>();

    /**
     * Tracks launched projectile entities → (definition, context).
     * Used by ProjectileListener to fire on_hit actions.
     */
    private final Map<UUID, ProjectileEntry> activeProjectiles = new ConcurrentHashMap<>();

    public ProjectileManager(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    // -------------------------------------------------------------------------
    // Definition registry
    // -------------------------------------------------------------------------

    public void register(ProjectileDefinition def) {
        definitions.put(def.getId(), def);
    }

    public ProjectileDefinition getDefinition(String id) {
        return definitions.get(id);
    }

    public void clearDefinitions() {
        definitions.clear();
    }

    // -------------------------------------------------------------------------
    // Launching
    // -------------------------------------------------------------------------

    /**
     * Launches a projectile for the given definition and execution context.
     */
    public void launch(ProjectileDefinition def, ExecutionContext context) {
        Player player = context.getPlayer();
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection().normalize().multiply(def.getSpeed());

        Class<? extends Projectile> projectileClass = resolveProjectileClass(def.getEntityType());
        if (projectileClass == null) {
            plugin.getLogger().warning("[CustomItems] Unknown projectile entity type: " + def.getEntityType());
            return;
        }

        Projectile projectile = player.launchProjectile(projectileClass, direction);
        projectile.setGravity(def.hasGravity());
        projectile.setShooter(player);

        // Track it for on-hit callback
        if (!def.getOnHitActions().isEmpty()) {
            activeProjectiles.put(projectile.getUniqueId(), new ProjectileEntry(def, context));
        }

        // Homing: basic tick-based vector correction
        if (def.isHoming()) {
            scheduleHomingTask(projectile, player, def);
        }

        plugin.debugLog("Launched projectile: " + def.getEntityType()
                + " for player " + player.getName());
    }

    // -------------------------------------------------------------------------
    // Homing logic
    // -------------------------------------------------------------------------

    private void scheduleHomingTask(Projectile projectile, Player shooter, ProjectileDefinition def) {
        // Run every 2 ticks to correct trajectory toward nearest hostile
        plugin.getServer().getScheduler().runTaskTimer(plugin, task -> {
            if (!projectile.isValid() || projectile.isDead()) {
                task.cancel();
                return;
            }

            // Find nearest living entity within 20 blocks (excluding shooter)
            LivingEntity target = projectile.getLocation().getWorld()
                    .getNearbyEntities(projectile.getLocation(), 20, 20, 20)
                    .stream()
                    .filter(e -> e instanceof LivingEntity && !e.equals(shooter))
                    .map(e -> (LivingEntity) e)
                    .min((a, b) -> {
                        double da = a.getLocation().distanceSquared(projectile.getLocation());
                        double db = b.getLocation().distanceSquared(projectile.getLocation());
                        return Double.compare(da, db);
                    })
                    .orElse(null);

            if (target != null) {
                Vector toTarget = target.getLocation().add(0, 1, 0)
                        .toVector()
                        .subtract(projectile.getLocation().toVector())
                        .normalize()
                        .multiply(def.getSpeed());
                // Blend current + target velocity (soft correction)
                Vector blended = projectile.getVelocity().add(toTarget).multiply(0.5);
                projectile.setVelocity(blended.normalize().multiply(def.getSpeed()));
            }
        }, 0L, 2L);
    }

    // -------------------------------------------------------------------------
    // Hit tracking
    // -------------------------------------------------------------------------

    public ProjectileEntry getAndRemove(UUID projectileId) {
        return activeProjectiles.remove(projectileId);
    }

    public boolean isTracked(UUID projectileId) {
        return activeProjectiles.containsKey(projectileId);
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private Class<? extends Projectile> resolveProjectileClass(String name) {
        return switch (name) {
            case "ARROW"         -> Arrow.class;
            case "SNOWBALL"      -> Snowball.class;
            case "EGG"           -> Egg.class;
            case "FIREBALL",
                 "LARGE_FIREBALL" -> Fireball.class;
            case "SMALL_FIREBALL" -> SmallFireball.class;
            case "TRIDENT"       -> Trident.class;
            case "ENDER_PEARL"   -> EnderPearl.class;
            case "SPLASH_POTION" -> SplashPotion.class;
            case "SPECTRAL_ARROW" -> SpectralArrow.class;
            case "SHULKER_BULLET" -> ShulkerBullet.class;
            case "WITHER_SKULL"  -> WitherSkull.class;
            case "LLAMA_SPIT"    -> LlamaSpit.class;
            default              -> null;
        };
    }

    // -------------------------------------------------------------------------
    // Inner types
    // -------------------------------------------------------------------------

    public record ProjectileEntry(ProjectileDefinition definition, ExecutionContext context) {}
}
