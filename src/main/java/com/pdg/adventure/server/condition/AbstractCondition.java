package com.pdg.adventure.server.condition;

import com.pdg.adventure.api.PreCondition;
import com.pdg.adventure.server.action.IdedAction;

public abstract class AbstractCondition extends IdedAction implements PreCondition {

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return getName();
    }
}
