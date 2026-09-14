package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.Action;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public abstract class AbstractAction extends IdedAction implements Action {

    @Override
    public String getActionName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return getActionName();
    }

}
