package com.pdg.adventure.server.action;

import lombok.EqualsAndHashCode;

import java.util.Comparator;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.support.Variable;
import com.pdg.adventure.server.support.VariableProvider;

/**
 * DIAGNOSE_VARS: prints every variable that exists right now, one per line as {@code NAME : value}, sorted by name
 * (ignoring case). With no variables at all it prints a single hyphen.
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class DiagnoseVariablesAction extends AbstractAction {

    private static final String NONE = "-";

    private final transient VariableProvider variableProvider;

    public DiagnoseVariablesAction(VariableProvider aVariableProvider) {
        variableProvider = aVariableProvider;
    }

    @Override
    public ExecutionResult execute() {
        String lines = String.join("\n", variableProvider.getAll().stream()
                .sorted(Comparator.comparing(Variable::name, String.CASE_INSENSITIVE_ORDER)
                                  .thenComparing(Variable::name))
                .map(variable -> variable.name() + " : " + variable.value())
                .toList());
        return new CommandExecutionResult(ExecutionResult.State.SUCCESS, lines.isEmpty() ? NONE : lines);
    }
}
