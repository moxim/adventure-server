package com.pdg.adventure.server.condition;

import com.pdg.adventure.server.api.PreCondition;

public class AndCondition implements PreCondition {

    private final PreCondition preCondition;
    private final PreCondition anotherPreCondition;

    public AndCondition(PreCondition aPreCondition, PreCondition andAnotherPreCondition) {
        preCondition = aPreCondition;
        anotherPreCondition = andAnotherPreCondition;
    }

    @Override
    public boolean isValid() {
        return preCondition.isValid() && anotherPreCondition.isValid();
    }
}
