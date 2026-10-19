package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.PictureMode;
import com.pdg.adventure.server.engine.GameContext;

class PictureModeActionTest {

    @Test
    void execute_setsTheModeOnTheGameContext_withoutAnyText() {
        GameContext gameContext = new GameContext();

        ExecutionResult result = new PictureModeAction(PictureMode.OFF, gameContext).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEmpty();
        assertThat(gameContext.getPictureMode()).isEqualTo(PictureMode.OFF);
    }

    @Test
    void execute_canSwitchBackAndForth() {
        GameContext gameContext = new GameContext();

        new PictureModeAction(PictureMode.ON, gameContext).execute();
        assertThat(gameContext.getPictureMode()).isEqualTo(PictureMode.ON);

        new PictureModeAction(PictureMode.DEFAULT, gameContext).execute();
        assertThat(gameContext.getPictureMode()).isEqualTo(PictureMode.DEFAULT);
    }

    @Test
    void aGameContext_startsInTheDefaultMode() {
        assertThat(new GameContext().getPictureMode()).isEqualTo(PictureMode.DEFAULT);
    }

    @Test
    void isInformationalOnly_becauseItNeverTellsAnything() {
        assertThat(new PictureModeAction(PictureMode.ON, new GameContext()).isInformationalOnly()).isTrue();
    }
}
