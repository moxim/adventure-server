package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import java.util.Comparator;
import java.util.function.Consumer;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

/**
 * PRINT_VARS: prints every variable that exists right now, one per line as {@code NAME : value}, sorted by name
 * (ignoring case). With no variables at all it prints a single hyphen.
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PrintVariablesAction extends AbstractAction {

    private static final String NONE = "-";

    private final transient Consumer<String> messageConsumer;
    private final transient VariableProvider variableProvider;

    public PrintVariablesAction(Consumer<String> aMessageConsumer, VariableProvider aVariableProvider) {
        messageConsumer = aMessageConsumer;
        variableProvider = aVariableProvider;
    }

    @Override
    public ExecutionResult execute() {
        String lines = String.join("\n", variableProvider.getAll().stream()
                .sorted(Comparator.comparing(Variable::name, String.CASE_INSENSITIVE_ORDER)
                                  .thenComparing(Variable::name))
                .map(variable -> variable.name() + " : " + variable.value())
                .toList());
        messageConsumer.accept(lines.isEmpty() ? NONE : lines);
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS);
    }
}
