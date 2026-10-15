package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.AdventureFont;
import com.pdg.adventure.server.engine.FontMarkup;

class MesActionTest {

    @Test
    void execute_returnsSuccessWithMessage() {
        ExecutionResult result = new MesAction("You see a door.").execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEqualTo("You see a door.");
    }

    @Test
    void execute_wrapsTheTextInItsFont() {
        AdventureFont font = AdventureFont.values()[AdventureFont.values().length - 1];

        ExecutionResult result = new MesAction("Hi", font).execute();

        assertThat(result.getResultMessage()).isEqualTo(FontMarkup.wrap("Hi", font));
    }

    @Test
    void continuesTheLine_andIsInformationalOnly() {
        MesAction mes = new MesAction("x");

        assertThat(mes.continuesLine()).isTrue();
        assertThat(mes.isInformationalOnly()).isTrue();
        assertThat(new MessageAction("x").continuesLine()).isFalse();
    }
}
