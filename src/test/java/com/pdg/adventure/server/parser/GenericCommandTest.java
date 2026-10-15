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

    private static Action continuingAction(String message) {
        Action a = action(message);
        when(a.continuesLine()).thenReturn(true);
        return a;
    }

    @Test
    void continuesLineAction_isNotFollowedByALineBreak() {
        GenericCommand command = new GenericCommand(mock(CommandDescription.class));
        command.addAction(continuingAction("You see "));
        command.addAction(continuingAction("a door"));
        command.addAction(action("."));
        command.addAction(action("Next line."));

        ExecutionResult result = command.execute();

        assertThat(result.getResultMessage())
                .isEqualTo("You see a door." + System.lineSeparator() + "Next line.");
    }

    @Test
    void continuesLineAction_startsOnANewLineAfterAnOrdinaryAction() {
        GenericCommand command = new GenericCommand(mock(CommandDescription.class));
        command.addAction(action("First."));
        command.addAction(continuingAction("Second "));
        command.addAction(action("third."));

        ExecutionResult result = command.execute();

        assertThat(result.getResultMessage())
                .isEqualTo("First." + System.lineSeparator() + "Second third.");
    }

    @Test
    void continuesLineAction_keepsATextThatIsJustASpace() {
        GenericCommand command = new GenericCommand(mock(CommandDescription.class));
        command.addAction(continuingAction("one"));
        command.addAction(continuingAction(" "));
        command.addAction(continuingAction("two"));

        assertThat(command.execute().getResultMessage()).isEqualTo("one two");
    }

    @Test
    void ordinaryAction_stillDropsABlankText() {
        GenericCommand command = new GenericCommand(mock(CommandDescription.class));
        command.addAction(action("one"));
        command.addAction(action(" "));
        command.addAction(action("two"));

        assertThat(command.execute().getResultMessage())
                .isEqualTo("one" + System.lineSeparator() + "two");
    }
}
