package com.customplugin.customitems.actions;

import com.customplugin.customitems.CustomItemsPlugin;
import com.customplugin.customitems.actions.impl.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry mapping action type strings → executor implementations.
 * Additional executors can be registered dynamically (API extension point).
 */
public final class ActionRegistry {

    private final Map<String, ActionExecutor> executors = new HashMap<>();

    public ActionRegistry(CustomItemsPlugin plugin) {
        // Core text/display actions
        register("MESSAGE",       new MessageAction(plugin));
        register("ACTIONBAR",     new ActionBarAction(plugin));
        register("TITLE",         new TitleAction(plugin));

        // Commands
        register("COMMAND",       new CommandAction(plugin));

        // Sound / Particle
        register("SOUND",         new SoundAction(plugin));
        register("PARTICLE",      new ParticleAction(plugin));

        // Combat
        register("DAMAGE",        new DamageAction(plugin));
        register("HEAL",          new HealAction(plugin));
        register("POTION_EFFECT", new PotionEffectAction(plugin));

        // Teleport
        register("TELEPORT",      new TeleportAction(plugin));

        // Inventory
        register("GIVE_ITEM",     new GiveItemAction(plugin));
        register("REMOVE_ITEM",   new RemoveItemAction(plugin));

        // Entity
        register("SPAWN_ENTITY",  new SpawnEntityAction(plugin));
        register("PROJECTILE",    new ProjectileAction(plugin));

        // Variables
        register("SET_VARIABLE",  new SetVariableAction(plugin));
    }

    public void register(String type, ActionExecutor executor) {
        executors.put(type.toUpperCase(), executor);
    }

    public ActionExecutor getExecutor(String type) {
        return executors.get(type.toUpperCase());
    }

    public boolean hasExecutor(String type) {
        return executors.containsKey(type.toUpperCase());
    }

    public Map<String, ActionExecutor> getAllExecutors() {
        return Map.copyOf(executors);
    }
}
