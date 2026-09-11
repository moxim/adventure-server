package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import java.util.Objects;

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

    @Override
    public boolean equals(final Object aO) {
        if (aO == null || getClass() != aO.getClass()) return false;
        if (!super.equals(aO)) return false;

        AbstractAction that = (AbstractAction) aO;
        return Objects.equals(getActionName(), that.getActionName());
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(getActionName());
        return result;
    }
}
