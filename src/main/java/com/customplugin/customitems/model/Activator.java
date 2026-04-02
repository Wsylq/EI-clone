package com.customplugin.customitems.model;

import java.util.Collections;
import java.util.List;

/**
 * Represents a single activator configuration within a CustomItem.
 * An activator ties an event type to conditions, cooldown, and an action list.
 */
public final class Activator {

    private final String type;
    private final long cooldownTicks;
    private final String cooldownMessage;
    private final List<String> conditions;
    private final List<ActionDefinition> actions;

    public Activator(
            String type,
            long cooldownTicks,
            String cooldownMessage,
            List<String> conditions,
            List<ActionDefinition> actions) {
        this.type = type;
        this.cooldownTicks = cooldownTicks;
        this.cooldownMessage = cooldownMessage;
        this.conditions = Collections.unmodifiableList(conditions);
        this.actions = Collections.unmodifiableList(actions);
    }

    public String getType() {
        return type;
    }

    /** Cooldown duration expressed in server ticks (20 ticks = 1 second). */
    public long getCooldownTicks() {
        return cooldownTicks;
    }

    public String getCooldownMessage() {
        return cooldownMessage;
    }

    public List<String> getConditions() {
        return conditions;
    }

    public List<ActionDefinition> getActions() {
        return actions;
    }

    public boolean hasCooldown() {
        return cooldownTicks > 0;
    }

    @Override
    public String toString() {
        return "Activator{type='" + type + "', cooldownTicks=" + cooldownTicks
                + ", conditions=" + conditions.size() + ", actions=" + actions.size() + "}";
    }
}
