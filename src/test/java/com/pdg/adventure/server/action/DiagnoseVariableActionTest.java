package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.support.VariableProvider;

class DiagnoseVariableActionTest {

    private final VariableProvider variables = new VariableProvider();

    private DiagnoseVariableAction printing(String aName) {
        return new DiagnoseVariableAction(aName, variables);
    }

    @Test
    void execute_printsTheNameAndTheValue() {
        variables.set(VariableProvider.VISITED_VARIABLE_NAME, 14);

        ExecutionResult result = printing(VariableProvider.VISITED_VARIABLE_NAME).execute();

        assertThat(result.getResultMessage()).isEqualTo("VISITED : 14");
    }

    @Test
    void execute_printsWhateverTheValueIs_negativeAndZeroIncluded() {
        variables.set("score", -3);
        variables.set("flag", 0);

        assertThat(printing("score").execute().getResultMessage()).isEqualTo("score : -3");
        assertThat(printing("flag").execute().getResultMessage()).isEqualTo("flag : 0");
    }

    @Test
    void execute_readsTheValueAtTheMomentItRuns() {
        DiagnoseVariableAction action = printing("score");
        variables.set("score", 1);
        String first = action.execute().getResultMessage();
        variables.set("score", 2);
        String second = action.execute().getResultMessage();

        assertThat(first).isEqualTo("score : 1");
        assertThat(second).isEqualTo("score : 2");
    }

    @Test
    void execute_forTheStandardTurnsVariable_printsTheTurnCount() {
        variables.set(VariableProvider.TURNS_VARIABLE_NAME, 7);

        ExecutionResult result = printing(VariableProvider.TURNS_VARIABLE_NAME).execute();

        assertThat(result.getResultMessage()).isEqualTo("TURNS : 7");
    }

    @Test
    void execute_forAVariableThatDoesNotExist_printsAHyphenWithoutDefiningIt() {
        ExecutionResult result = printing("ghost").execute();

        assertThat(result.getResultMessage()).isEqualTo("ghost : -");
        assertThat(variables.isDefined("ghost")).isFalse();
    }

    @Test
    void execute_returnsSuccess() {
        ExecutionResult result = printing("score").execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }
}
