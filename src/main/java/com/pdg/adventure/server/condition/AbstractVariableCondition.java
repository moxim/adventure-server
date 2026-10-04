package com.pdg.adventure.server.condition;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Optional;

import com.pdg.adventure.server.exception.ConfigurationException;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public abstract class AbstractVariableCondition extends AbstractCondition {
    @Getter
    protected final transient VariableProvider variableProvider;

    AbstractVariableCondition(VariableProvider aVariableProvider) {
        variableProvider = aVariableProvider;
    }

    protected Variable getVariable(String aVariableName) {
        Optional<Variable> variableOptional = variableProvider.get(aVariableName);
        if (variableOptional.isEmpty()) {
            throw new ConfigurationException("Variable " + aVariableName + " does not exist!");
        }
        return variableOptional.get();
    }

    protected int extractVariableValue(String aVariableName) {
        final Variable envVariable = getVariable(aVariableName);
        int envVal;
        try {
            envVal = envVariable.value();
        } catch (NumberFormatException _) {
            throw new ConfigurationException("This variable does not contain a number: " + aVariableName);
        }
        return envVal;
    }

}
