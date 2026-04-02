package com.customplugin.customitems.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A raw action definition parsed from YAML.
 * Stores the action type and all raw key-value parameters.
 * Nested action lists (IF/ELSE, DELAY) are stored as child ActionDefinition lists.
 */
public final class ActionDefinition {

    private final String type;
    private final Map<String, Object> params;
    private final List<ActionDefinition> thenActions;
    private final List<ActionDefinition> elseActions;

    public ActionDefinition(
            String type,
            Map<String, Object> params,
            List<ActionDefinition> thenActions,
            List<ActionDefinition> elseActions) {
        this.type = type.toUpperCase();
        this.params = Collections.unmodifiableMap(new HashMap<>(params));
        this.thenActions = thenActions != null
                ? Collections.unmodifiableList(thenActions)
                : Collections.emptyList();
        this.elseActions = elseActions != null
                ? Collections.unmodifiableList(elseActions)
                : Collections.emptyList();
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public String getString(String key, String def) {
        Object v = params.get(key);
        return v != null ? v.toString() : def;
    }

    public int getInt(String key, int def) {
        Object v = params.get(key);
        if (v == null) return def;
        try { return Integer.parseInt(v.toString()); } catch (NumberFormatException e) { return def; }
    }

    public double getDouble(String key, double def) {
        Object v = params.get(key);
        if (v == null) return def;
        try { return Double.parseDouble(v.toString()); } catch (NumberFormatException e) { return def; }
    }

    public boolean getBoolean(String key, boolean def) {
        Object v = params.get(key);
        if (v == null) return def;
        if (v instanceof Boolean b) return b;
        return Boolean.parseBoolean(v.toString());
    }

    public List<ActionDefinition> getThenActions() {
        return thenActions;
    }

    public List<ActionDefinition> getElseActions() {
        return elseActions;
    }

    @Override
    public String toString() {
        return "ActionDefinition{type='" + type + "', params=" + params + "}";
    }
}
