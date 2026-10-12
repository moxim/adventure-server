package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.support.VariableProvider;

class PrintVariableActionTest {

    private final List<String> told = new ArrayList<>();
    private final VariableProvider variables = new VariableProvider();

    private PrintVariableAction printing(String aName) {
        return new PrintVariableAction(aName, told::add, variables);
    }

    @Test
    void execute_printsTheNameAndTheValue() {
        variables.set(VariableProvider.VISITED_VARIABLE_NAME, 14);

        printing(VariableProvider.VISITED_VARIABLE_NAME).execute();

        assertThat(told).containsExactly("VISITED : 14");
    }

    @Test
    void execute_printsWhateverTheValueIs_negativeAndZeroIncluded() {
        variables.set("score", -3);
        variables.set("flag", 0);

        printing("score").execute();
        printing("flag").execute();

        assertThat(told).containsExactly("score : -3", "flag : 0");
    }

    @Test
    void execute_readsTheValueAtTheMomentItRuns() {
        PrintVariableAction action = printing("score");
        variables.set("score", 1);
        action.execute();
        variables.set("score", 2);
        action.execute();

        assertThat(told).containsExactly("score : 1", "score : 2");
    }

    @Test
    void execute_forTheStandardTurnsVariable_printsTheTurnCount() {
        variables.set(VariableProvider.TURNS_VARIABLE_NAME, 7);

        printing(VariableProvider.TURNS_VARIABLE_NAME).execute();

        assertThat(told).containsExactly("TURNS : 7");
    }

    @Test
    void execute_forAVariableThatDoesNotExist_printsAHyphenWithoutDefiningIt() {
        printing("ghost").execute();

        assertThat(told).containsExactly("ghost : -");
        assertThat(variables.isDefined("ghost")).isFalse();
    }

    @Test
    void execute_returnsSuccess() {
        ExecutionResult result = printing("score").execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }
}
