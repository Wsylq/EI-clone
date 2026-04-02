package com.customplugin.customitems.actions;

import com.customplugin.customitems.model.ActionDefinition;
import com.customplugin.customitems.model.ExecutionContext;

/**
 * Contract for all action executors.
 * Each implementation handles a single action type.
 */
public interface ActionExecutor {

    /**
     * Execute the action described by {@code def} within the given {@code context}.
     *
     * @param def     the parsed action definition containing all parameters
     * @param context the current execution context
     */
    void execute(ActionDefinition def, ExecutionContext context);
}
