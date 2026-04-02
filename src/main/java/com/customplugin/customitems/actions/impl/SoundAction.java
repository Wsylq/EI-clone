package com.customplugin.customitems.actions.impl;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.actions.ActionExecutor;
import com.customplugin.customitems.model.ActionDefinition;
import com.customplugin.customitems.model.ExecutionContext;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;

/**
 * Plays a sound to the player at their current location.
 *
 * YAML:
 *   type: SOUND
 *   sound: "minecraft:entity.fireball.shoot"
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
        String soundName = def.getString("sound", "minecraft:block.note_block.pling");
        float volume = (float) def.getDouble("volume", 1.0);
        float pitch  = (float) def.getDouble("pitch", 1.0);
        String sourceStr = def.getString("source", "MASTER").toUpperCase();

        Sound.Source source;
        try {
            source = Sound.Source.valueOf(sourceStr);
        } catch (IllegalArgumentException e) {
            source = Sound.Source.MASTER;
        }

        // Normalize sound name to namespaced key
        if (!soundName.contains(":")) {
            soundName = "minecraft:" + soundName.toLowerCase();
        }

        try {
            Sound sound = Sound.sound(Key.key(soundName), source, volume, pitch);
            context.getPlayer().playSound(sound);
        } catch (Exception e) {
            plugin.getLogger().warning("[CustomItems] Invalid sound key: " + soundName);
        }
    }
}
