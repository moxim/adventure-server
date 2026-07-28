package com.pdg.adventure.server.parser;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Action;
import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.api.ExecutionResult;

class GenericCommandTest {

    private static Action action(String message) {
        Action a = mock(Action.class);
        ExecutionResult r = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        r.setResultMessage(message);
        when(a.execute()).thenReturn(r);
        return a;
    }

    private static Action breakAction(String message) {
        Action a = action(message);
        when(a.isBreak()).thenReturn(true);
        return a;
    }

    @Test
    void breakAction_stopsFurtherActionsWithinTheSameCommand() {
        GenericCommand command = new GenericCommand(mock(CommandDescription.class));
        command.addAction(action("before"));
        command.addAction(breakAction("at-break"));
        command.addAction(action("after"));    // must not run

        ExecutionResult result = command.execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage())
                .isEqualTo("before" + System.lineSeparator() + "at-break");
    }
}
