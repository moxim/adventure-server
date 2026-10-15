package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.api.CommandDescription;
import com.pdg.adventure.server.exception.ConfigurationException;
import com.pdg.adventure.server.parser.GenericCommand;
import com.pdg.adventure.server.support.VariableProvider;

import static org.mockito.Mockito.mock;

class PrintActionTest {
    private final VariableProvider variableProvider = new VariableProvider();

    @Test
    void execute_returnsTheValueOnly() {
        variableProvider.set("score", 14);

        ExecutionResult result = new PrintAction("score", variableProvider).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEqualTo("14");
    }

    @Test
    void execute_printsNegativeValues() {
        variableProvider.set("score", -3);

        assertThat(new PrintAction("score", variableProvider).execute().getResultMessage()).isEqualTo("-3");
    }

    @Test
    void execute_withUnknownVariable_throwsConfigurationException() {
        PrintAction sut = new PrintAction("ghost", variableProvider);

        assertThatThrownBy(sut::execute).isInstanceOf(ConfigurationException.class).hasMessageContaining("ghost");
        assertThat(variableProvider.isDefined("ghost")).isFalse();
    }

    @Test
    void continuesTheLine_andIsInformationalOnly() {
        PrintAction sut = new PrintAction("score", variableProvider);

        assertThat(sut.continuesLine()).isTrue();
        assertThat(sut.isInformationalOnly()).isTrue();
    }

    @Test
    void inACommand_aMessageClosesTheLineTheMesAndPrintStarted() {
        variableProvider.set("score", 14);
        GenericCommand command = new GenericCommand(mock(CommandDescription.class));
        command.addAction(new MesAction("You have "));
        command.addAction(new PrintAction("score", variableProvider));
        command.addAction(new MessageAction(" points."));    // MESSAGE closes the line
        command.addAction(new MessageAction("Well done."));

        assertThat(command.execute().getResultMessage())
                .isEqualTo("You have 14 points." + System.lineSeparator() + "Well done.");
    }

    @Test
    void inACommand_turnsTextKeepsItsPlaceAmongTheMessages() {
        variableProvider.set(VariableProvider.TURNS_VARIABLE_NAME, 1);
        GenericCommand command = new GenericCommand(mock(CommandDescription.class));
        command.addAction(new MesAction("Can you "));
        command.addAction(new MessageAction("please hold"));    // MESSAGE closes the line
        command.addAction(new TurnsAction(variableProvider));

        assertThat(command.execute().getResultMessage())
                .isEqualTo("Can you please hold" + System.lineSeparator() + "You have taken 1 turn.");
    }
}
