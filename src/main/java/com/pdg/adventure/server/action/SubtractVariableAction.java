package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import com.pdg.adventure.server.support.VariableProvider;

/**
 * Subtracts the value of the first variable from the second (PAW's MINUS).
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SubtractVariableAction extends AbstractTwoVariablesAction {

    public SubtractVariableAction(String aFirstName, String aSecondName, VariableProvider aVariableProvider) {
        super(aFirstName, aSecondName, aVariableProvider);
    }

    @Override
    protected int combine(int aFirst, int aSecond) {
        return aSecond - aFirst;
    }
}
