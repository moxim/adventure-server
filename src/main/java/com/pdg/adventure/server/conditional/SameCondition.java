package com.pdg.adventure.server.conditional;

import com.pdg.adventure.server.support.VariableProvider;

public class SameCondition extends AbstractVariableCondition {

    private final String variableNameOne;
    private final String variableNameTwo;

    public SameCondition(String aVariableNameOne, String aVariableNameTwo, VariableProvider aVariableProvider) {
        super(aVariableProvider);
        variableNameOne = aVariableNameOne;
        variableNameTwo = aVariableNameTwo;
    }

    @Override
    public boolean isValid() {
        return variableProvider.get(variableNameOne).equals(variableProvider.get(variableNameTwo));
    }
}
