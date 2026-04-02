package com.customplugin.customitems.actions.impl;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.actions.ActionExecutor;
import com.customplugin.customitems.model.ActionDefinition;
import com.customplugin.customitems.model.ExecutionContext;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

/**
 * Plays a sound to the player at their current location.
 *
 * YAML:
 *   type: SOUND
 *   sound: "ENTITY_FIREBALL_SHOOT"    # Bukkit Sound enum name
 *   volume: 1.0
 *   pitch: 1.0
 *   source: MASTER   # MASTER | MUSIC | RECORD | WEATHER | BLOCK | HOSTILE | NEUTRAL | PLAYER | AMBIENT | VOICE
 */
public final class SoundAction implements ActionExecutor {

    private final CustomItemsPlugin plugin;

    public SoundAction(CustomItemsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(ActionDefinition def, ExecutionContext context) {
        Player player = context.getPlayer();
        String soundName = def.getString("sound", "BLOCK_NOTE_BLOCK_PLING").toUpperCase();
        float volume = (float) def.getDouble("volume", 1.0);
        float pitch  = (float) def.getDouble("pitch", 1.0);
        String sourceStr = def.getString("source", "MASTER").toUpperCase();

        SoundCategory category;
        try {
            category = SoundCategory.valueOf(sourceStr);
        } catch (IllegalArgumentException e) {
            category = SoundCategory.MASTER;
        }

        Location location = player.getLocation();

        // Try Bukkit Sound enum first
        try {
            Sound sound = Sound.valueOf(soundName);
            player.playSound(location, sound, category, volume, pitch);
            return;
        } catch (IllegalArgumentException ignored) {
            // Not a valid enum name — try as a namespaced key string
        }

        // Handle namespaced keys like "minecraft:entity.fireball.shoot"
        // Convert to Bukkit enum format: replace dots and colons
        String normalized = soundName.toLowerCase()
                .replace("minecraft:", "")
                .replace(".", "_")
                .replace(":", "_")
                .toUpperCase();

        try {
            Sound sound = Sound.valueOf(normalized);
            player.playSound(location, sound, category, volume, pitch);
        } catch (IllegalArgumentException e) {
            // Last resort: play by string key (works on Paper/Spigot 1.9+)
            player.playSound(location, soundName.toLowerCase(), category, volume, pitch);
        }
    }
}
