package com.pdg.adventure.server.action;

import com.pdg.adventure.api.Action;

public abstract class AbstractAction implements Action {

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return getName();
    }
}
