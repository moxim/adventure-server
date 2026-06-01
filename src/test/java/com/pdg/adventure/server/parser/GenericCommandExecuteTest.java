package com.pdg.adventure.server.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Action;
import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.PreCondition;

class GenericCommandExecuteTest {

    private static Action action(ExecutionResult.State state, String message) {
        Action a = mock(Action.class);
        ExecutionResult r = new CommandExecutionResult(state);
        r.setResultMessage(message);
        when(a.execute()).thenReturn(r);
        return a;
    }

    private static PreCondition precondition(ExecutionResult.State state, String message) {
        PreCondition p = mock(PreCondition.class);
        ExecutionResult r = new CommandExecutionResult(state);
        r.setResultMessage(message);
        when(p.check()).thenReturn(r);
        return p;
    }

    @Test
    void emptyActions_doesNotThrow_andSucceeds() {
        GenericCommand cmd = new GenericCommand(mock(CommandDescription.class));
        ExecutionResult result = cmd.execute();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void firstActionSuccessMessageSurfaces_laterSuccessesDiscarded() {
        GenericCommand cmd = new GenericCommand(mock(CommandDescription.class));
        cmd.addAction(action(ExecutionResult.State.SUCCESS, "first"));
        cmd.addAction(action(ExecutionResult.State.SUCCESS, "second"));
        ExecutionResult result = cmd.execute();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEqualTo("first");
    }

    @Test
    void firstActionFailure_shortCircuits() {
        GenericCommand cmd = new GenericCommand(mock(CommandDescription.class));
        Action second = mock(Action.class);
        cmd.addAction(action(ExecutionResult.State.FAILURE, "boom"));
        cmd.addAction(second);
        ExecutionResult result = cmd.execute();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEqualTo("boom");
    }

    @Test
    void laterActionFailure_surfacesThatFailure() {
        GenericCommand cmd = new GenericCommand(mock(CommandDescription.class));
        cmd.addAction(action(ExecutionResult.State.SUCCESS, "ok"));
        cmd.addAction(action(ExecutionResult.State.FAILURE, "later-fail"));
        ExecutionResult result = cmd.execute();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEqualTo("later-fail");
    }

    @Test
    void preconditionFailure_skipsActions() {
        GenericCommand cmd = new GenericCommand(mock(CommandDescription.class));
        cmd.addPreCondition(precondition(ExecutionResult.State.FAILURE, "blocked"));
        Action action = mock(Action.class);
        cmd.addAction(action);
        ExecutionResult result = cmd.execute();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).isEqualTo("blocked");
    }
}
