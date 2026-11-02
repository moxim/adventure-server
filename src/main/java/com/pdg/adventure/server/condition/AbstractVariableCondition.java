package com.pdg.adventure.server.condition;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pdg.adventure.server.exception.ConfigurationException;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public abstract class AbstractVariableCondition extends AbstractCondition {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractVariableCondition.class);

    @Getter
    protected final transient VariableProvider variableProvider;

    AbstractVariableCondition(VariableProvider aVariableProvider) {
        variableProvider = aVariableProvider;
    }

    private Variable getVariable(String aVariableName) {
        return variableProvider.findVariableOrThrow(aVariableName);
    }

    protected int extractVariableValue(String aVariableName) {
        final Variable envVariable = getVariable(aVariableName);
        int envVal;
        try {
            envVal = envVariable.value();
        } catch (NumberFormatException _) {
            ConfigurationException ce = new ConfigurationException("Variable " + aVariableName + " does not contain a number!");
            LOG.warn("Variable {} does not contain a number!", aVariableName, ce);
            throw ce;
        }
        return envVal;
    }

}
