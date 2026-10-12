package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.support.VariableProvider;

class PrintVariablesActionTest {

    private final List<String> told = new ArrayList<>();
    private final VariableProvider variables = new VariableProvider();
    private final PrintVariablesAction action = new PrintVariablesAction(told::add, variables);

    @Test
    void execute_listsEveryVariableOnItsOwnLine_sortedByNameIgnoringCase() {
        variables.set("zeta", 1);
        variables.set("VISITED", 14);
        variables.set("alpha", -2);
        variables.set("TURNS", 0);

        action.execute();

        assertThat(told).containsExactly("alpha : -2\nTURNS : 0\nVISITED : 14\nzeta : 1");
    }

    @Test
    void execute_withASingleVariable_printsOneLineWithoutATrailingNewline() {
        variables.set("score", 7);

        action.execute();

        assertThat(told).containsExactly("score : 7");
    }

    @Test
    void execute_withoutAnyVariable_printsAHyphen() {
        action.execute();

        assertThat(told).containsExactly("-");
    }

    @Test
    void execute_readsTheValuesAtTheMomentItRuns() {
        variables.set("score", 1);
        action.execute();
        variables.set("score", 2);
        action.execute();

        assertThat(told).containsExactly("score : 1", "score : 2");
    }

    @Test
    void execute_returnsSuccess() {
        ExecutionResult result = action.execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }
}
