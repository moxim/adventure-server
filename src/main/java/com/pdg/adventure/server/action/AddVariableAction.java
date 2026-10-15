package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import com.pdg.adventure.server.support.VariableProvider;

/**
 * Adds the value of the first variable to the second (PAW's ADD).
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AddVariableAction extends AbstractTwoVariablesAction {

    public AddVariableAction(String aFirstName, String aSecondName, VariableProvider aVariableProvider) {
        super(aFirstName, aSecondName, aVariableProvider);
    }

    @Override
    protected int combine(int aFirst, int aSecond) {
        return aFirst + aSecond;
    }
}
