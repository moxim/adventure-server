package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.ExecutionResult;

@ExtendWith(MockitoExtension.class)
class BreakActionTest {

    @Test
    void execute_returnsSuccess() {
        ExecutionResult result = new BreakAction().execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void isBreak_returnsTrue() {
        assertThat(new BreakAction().isBreak()).isTrue();
    }
}
