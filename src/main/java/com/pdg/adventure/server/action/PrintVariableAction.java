package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.function.Consumer;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

/**
 * PRINT_VAR: prints a variable's name and its current value, e.g. {@code VISITED : 14}. A variable that does not
 * exist yet prints a hyphen instead of a value ({@code ghost : -}) and is not created by being printed.
 */
@Getter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PrintVariableAction extends AbstractAction {

    private static final String UNDEFINED = "-";

    private final String name;
    private final transient Consumer<String> messageConsumer;
    private final transient VariableProvider variableProvider;

    public PrintVariableAction(String aName, Consumer<String> aMessageConsumer, VariableProvider aVariableProvider) {
        name = aName;
        messageConsumer = aMessageConsumer;
        variableProvider = aVariableProvider;
    }

    @Override
    public ExecutionResult execute() {
        String value = variableProvider.get(name).map(variable -> String.valueOf(variable.value())).orElse(UNDEFINED);
        messageConsumer.accept(name + " : " + value);
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }
}
