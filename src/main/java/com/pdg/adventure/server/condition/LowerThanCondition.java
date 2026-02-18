package com.pdg.adventure.server.condition;

import lombok.EqualsAndHashCode;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.exception.ConfigurationException;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class LowerThanCondition extends AbstractVariableCondition {

    private final String variableName;
    private final Number value;

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
            envVal = Integer.parseInt(envVariable.aValue());
        } catch (NumberFormatException _) {
            throw new ConfigurationException("This variable does not contain a number: " + variableName);
        }
        int iVal;
        try {
            iVal = Integer.parseInt(value.toString());
        } catch (NumberFormatException _) {
            throw new ConfigurationException("This value is not a number: " + value);
        }
        if (envVal < iVal) {
            result.setExecutionState(ExecutionResult.State.SUCCESS);
        }
        return result;
    }
}
