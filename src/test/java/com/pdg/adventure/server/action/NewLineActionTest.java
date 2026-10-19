package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.ExecutionResult;

class NewLineActionTest {

    @Test
    void execute_yieldsALineBreak_andContinuesTheLine() {
        NewLineAction action = new NewLineAction();

        ExecutionResult result = action.execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEqualTo(System.lineSeparator());
        assertThat(action.continuesLine()).isTrue();
    }
}
