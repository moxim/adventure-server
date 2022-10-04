package com.pdg.adventure.server.conditional;

import com.pdg.adventure.server.api.PreCondition;

public class NotCondition implements PreCondition {

    private final PreCondition wrappedCondition;

    public NotCondition(PreCondition aWrappedCondition) {

        wrappedCondition = aWrappedCondition;
    }

    @Override
    public boolean isValid() {
        return !wrappedCondition.isValid();
    }
}
