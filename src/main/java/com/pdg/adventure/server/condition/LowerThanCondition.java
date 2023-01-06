package com.pdg.adventure.server.condition;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.exception.ConfigurationException;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

public class LowerThanCondition extends AbstractVariableCondition {

    private final String variableName;
    private final Object value;

    public LowerThanCondition(String aVariableName, Number aValue, VariableProvider aVariableProvider) {
        super(aVariableProvider);
        variableName = aVariableName;
        value = aValue;
    }

    @Override
    public ExecutionResult check() {
        ExecutionResult result = new CommandExecutionResult();
        final Variable envVariable = getVariable(variableName);
        int envVal;
        try {
            envVal = Integer.valueOf(envVariable.aValue());
        } catch (NumberFormatException e) {
            throw new ConfigurationException("This variable does not contain a number: " + variableName);
        }
        int iVal;
        try {
            iVal = Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            throw new ConfigurationException("This value is not a number: " + value);
        }
        if (envVal < iVal) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        return result;
    }
}
