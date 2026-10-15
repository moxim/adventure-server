package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.exception.ConfigurationException;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.VariableProvider;

/**
 * Base of the actions that combine two variables, as PAW's ADD and MINUS do: the value of the first variable is
 * applied to the second, which is the only one that changes. Both variables must exist; an unknown one is a
 * mistake in the adventure and is reported as a {@link ConfigurationException}.
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public abstract class AbstractTwoVariablesAction extends AbstractAction {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTwoVariablesAction.class);

    @Getter
    private final String firstName;
    @Getter
    private final String secondName;
    @Getter
    protected final transient VariableProvider variableProvider;

    AbstractTwoVariablesAction(String aFirstName, String aSecondName, VariableProvider aVariableProvider) {
        firstName = aFirstName;
        secondName = aSecondName;
        variableProvider = aVariableProvider;
    }

    @Override
    public ExecutionResult execute() {
        int first = valueOf(firstName);
        int second = valueOf(secondName);
        variableProvider.set(secondName, combine(first, second));
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }

    /**
     * @param aFirst  the value of the first variable
     * @param aSecond the current value of the second variable
     * @return the new value of the second variable
     */
    protected abstract int combine(int aFirst, int aSecond);

    private int valueOf(String aName) {
        return variableProvider.get(aName).orElseThrow(() -> {
            String errorText = "Variable " + aName + " does not exist!";
            ConfigurationException ce = new ConfigurationException(errorText);
            LOG.warn(errorText, ce);
            return ce;
        }).value();
    }
}
