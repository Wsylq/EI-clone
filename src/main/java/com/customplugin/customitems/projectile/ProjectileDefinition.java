package com.customplugin.customitems.projectile;

import com.customplugin.customitems.model.ActionDefinition;

import java.util.Collections;
import java.util.List;

/**
 * Immutable definition of a custom projectile.
 */
public final class ProjectileDefinition {

    private final String id;
    private final String entityType;
    private final double speed;
    private final boolean gravity;
    private final boolean homing;
    private final List<ActionDefinition> onHitActions;

    public ProjectileDefinition(
            String id,
            String entityType,
            double speed,
            boolean gravity,
            boolean homing,
            List<ActionDefinition> onHitActions) {
        this.id = id;
        this.entityType = entityType.toUpperCase();
        this.speed = speed;
        this.gravity = gravity;
        this.homing = homing;
        this.onHitActions = onHitActions != null
                ? Collections.unmodifiableList(onHitActions)
                : Collections.emptyList();
    }

    public String getId()           { return id; }
    public String getEntityType()   { return entityType; }
    public double getSpeed()        { return speed; }
    public boolean hasGravity()     { return gravity; }
    public boolean isHoming()       { return homing; }
    public List<ActionDefinition> getOnHitActions() { return onHitActions; }
}
