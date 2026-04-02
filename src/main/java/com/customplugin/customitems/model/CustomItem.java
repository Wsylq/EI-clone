package com.customplugin.customitems.model;

import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Map;

/**
 * Represents a fully parsed custom item definition loaded from YAML.
 * Immutable after construction — all mutation occurs via builder.
 */
public final class CustomItem {

    private final String id;
    private final ItemStack baseItem;
    private final Map<String, Activator> activators;

    public CustomItem(String id, ItemStack baseItem, Map<String, Activator> activators) {
        this.id = id;
        this.baseItem = baseItem;
        this.activators = Collections.unmodifiableMap(activators);
    }

    public String getId() {
        return id;
    }

    /** Returns a clone of the base ItemStack to prevent mutation. */
    public ItemStack getBaseItem() {
        return baseItem.clone();
    }

    /**
     * Alias for {@link #getBaseItem()} — returns a clone of the base ItemStack.
     * Provided for compatibility with code that calls getItemStack().
     */
    public ItemStack getItemStack() {
        return baseItem.clone();
    }

    public Map<String, Activator> getActivators() {
        return activators;
    }

    public Activator getActivator(String type) {
        return activators.get(type.toUpperCase());
    }

    public boolean hasActivator(String type) {
        return activators.containsKey(type.toUpperCase());
    }

    @Override
    public String toString() {
        return "CustomItem{id='" + id + "', activators=" + activators.keySet() + "}";
    }
}
