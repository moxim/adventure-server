package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public abstract class AbstractVariableAction extends AbstractAction {
    @Getter
    private final String variableName;
    @Getter
    protected final transient VariableProvider variableProvider;
    private static final Logger LOG = LoggerFactory.getLogger(AbstractVariableAction.class);

    AbstractVariableAction(final String aName, VariableProvider aVariableProvider) {
        variableName = aName;
        variableProvider = aVariableProvider;
    }

    @Override
    public ExecutionResult execute() {
        CommandExecutionResult result = new CommandExecutionResult();
        Variable variable = variableProvider.findVariableOrThrow(getVariableName());
        Integer baseValue = variable.value();
        changeValue(baseValue);
        result.setExecutionState(ExecutionResult.State.SUCCESS);
        return result;
    }


    protected abstract void changeValue(Integer aBaseValue);
}
