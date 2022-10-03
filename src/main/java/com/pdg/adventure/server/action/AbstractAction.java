package com.pdg.adventure.server.action;

import com.pdg.adventure.server.api.Action;

public abstract class AbstractAction implements Action {

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
