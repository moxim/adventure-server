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
 * PAW's PRINT: shows the value of a variable, e.g. {@code 14}, without a line break after it, so the text of the
 * next action in the same command continues on the same line. Unlike {@link DiagnoseVariableAction} it prints the value
 * only, not the name. A variable that does not exist is a mistake in the adventure and is reported as a
 * {@link ConfigurationException}.
 */
@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PrintAction extends AbstractAction {
    private static final Logger LOG = LoggerFactory.getLogger(PrintAction.class);

    private final String name;
    private final transient VariableProvider variableProvider;

    public PrintAction(String aName, VariableProvider aVariableProvider) {
        name = aName;
        variableProvider = aVariableProvider;
    }

    @Override
    public ExecutionResult execute() {
        int value = variableProvider.get(name).orElseThrow(() -> {
            String errorText = "Variable " + name + " does not exist!";
            ConfigurationException ce = new ConfigurationException(errorText);
            LOG.warn(errorText, ce);
            return ce;
        }).value();
        ExecutionResult result = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        result.setResultMessage(String.valueOf(value));
        return result;
    }

    @Override
    public boolean isInformationalOnly() {
        return true;
    }

    @Override
    public boolean continuesLine() {
        return true;
    }
}
