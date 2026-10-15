package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.support.VariableProvider;

class DiagnoseVariablesActionTest {

    private final VariableProvider variables = new VariableProvider();
    private final DiagnoseVariablesAction action = new DiagnoseVariablesAction(variables);

    @Test
    void execute_listsEveryVariableOnItsOwnLine_sortedByNameIgnoringCase() {
        variables.set("zeta", 1);
        variables.set("VISITED", 14);
        variables.set("alpha", -2);
        variables.set("TURNS", 0);

        ExecutionResult result = action.execute();

        assertThat(result.getResultMessage()).isEqualTo("alpha : -2\nTURNS : 0\nVISITED : 14\nzeta : 1");
    }

    @Test
    void execute_withASingleVariable_printsOneLineWithoutATrailingNewline() {
        variables.set("score", 7);

        ExecutionResult result = action.execute();

        assertThat(result.getResultMessage()).isEqualTo("score : 7");
    }

    @Test
    void execute_withoutAnyVariable_printsAHyphen() {
        ExecutionResult result = action.execute();

        assertThat(result.getResultMessage()).isEqualTo("-");
    }

    @Test
    void execute_readsTheValuesAtTheMomentItRuns() {
        variables.set("score", 1);
        String first = action.execute().getResultMessage();
        variables.set("score", 2);
        String second = action.execute().getResultMessage();

        assertThat(first).isEqualTo("score : 1");
        assertThat(second).isEqualTo("score : 2");
    }

    @Test
    void execute_returnsSuccess() {
        ExecutionResult result = action.execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }
}
