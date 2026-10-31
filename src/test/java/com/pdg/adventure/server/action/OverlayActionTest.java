package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.PictureOverlayData;
import com.pdg.adventure.server.engine.GameContext;

class OverlayActionTest {

    private final GameContext gameContext = new GameContext();

    @Test
    void execute_withAPictureOnShow_laysTheOverlayOverTheCell() {
        gameContext.setCurrentPictureId("door-closed");

        ExecutionResult result = new OverlayAction(3, 4, "door-open", gameContext).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(gameContext.getOverlays()).containsExactly(new PictureOverlayData(3, 4, "door-open"));
    }

    @Test
    void execute_withNoPictureOnShow_doesNothingAndSucceeds() {
        ExecutionResult result = new OverlayAction(3, 4, "door-open", gameContext).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(gameContext.getOverlays()).isEmpty();
    }

    @Test
    void execute_withACellOutsideTheGridOrMissingData_doesNothingAndSucceeds() {
        gameContext.setCurrentPictureId("door-closed");

        for (OverlayAction action : new OverlayAction[] {
                new OverlayAction(-1, 4, "p", gameContext), new OverlayAction(10, 4, "p", gameContext),
                new OverlayAction(3, -1, "p", gameContext), new OverlayAction(3, 10, "p", gameContext),
                new OverlayAction(null, 4, "p", gameContext), new OverlayAction(3, null, "p", gameContext),
                new OverlayAction(3, 4, null, gameContext)}) {
            assertThat(action.execute().getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        }
        assertThat(gameContext.getOverlays()).isEmpty();
    }

    @Test
    void isInformationalOnly_isTrue() {
        assertThat(new OverlayAction(0, 0, "p", gameContext).isInformationalOnly()).isTrue();
    }
}
