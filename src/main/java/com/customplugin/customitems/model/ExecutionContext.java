package com.customplugin.customitems.model;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Immutable carrier for all runtime data available during action execution.
 * Variables map is mutable to allow actions to set/get context-local state.
 */
public final class ExecutionContext {

    private final Player player;
    private final Entity targetEntity;
    private final Block targetBlock;
    private final Location location;
    private final ItemStack item;
    private final String activatorType;
    private final Map<String, Object> variables;

    private ExecutionContext(Builder builder) {
        this.player = builder.player;
        this.targetEntity = builder.targetEntity;
        this.targetBlock = builder.targetBlock;
        this.location = builder.location;
        this.item = builder.item;
        this.activatorType = builder.activatorType;
        this.variables = builder.variables;
    }

    public Player getPlayer() {
        return player;
    }

    public Entity getTargetEntity() {
        return targetEntity;
    }

    public Block getTargetBlock() {
        return targetBlock;
    }

    public Location getLocation() {
        return location;
    }

    public ItemStack getItem() {
        return item;
    }

    public String getActivatorType() {
        return activatorType;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public Object getVariable(String key) {
        return variables.get(key);
    }

    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    public static Builder builder(Player player) {
        return new Builder(player);
    }

    public static final class Builder {
        private final Player player;
        private Entity targetEntity;
        private Block targetBlock;
        private Location location;
        private ItemStack item;
        private String activatorType;
        private Map<String, Object> variables = new HashMap<>();

        private Builder(Player player) {
            this.player = player;
            this.location = player.getLocation();
        }

        public Builder targetEntity(Entity e) { this.targetEntity = e; return this; }
        public Builder targetBlock(Block b) { this.targetBlock = b; return this; }
        public Builder location(Location l) { this.location = l; return this; }
        public Builder item(ItemStack i) { this.item = i; return this; }
        public Builder activatorType(String t) { this.activatorType = t; return this; }
        public Builder variables(Map<String, Object> v) { this.variables = new HashMap<>(v); return this; }

        public ExecutionContext build() {
            return new ExecutionContext(this);
        }
    }
}
