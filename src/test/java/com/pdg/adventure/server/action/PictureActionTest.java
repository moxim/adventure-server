package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.GameContext;

@ExtendWith(MockitoExtension.class)
class PictureActionTest {

    @Mock
    private GameContext gameContext;

    @Test
    void execute_setsCurrentPictureIdOnGameContext() {
        PictureAction action = new PictureAction("treasure-chest", gameContext);

        action.execute();

        Mockito.verify(gameContext).setCurrentPictureId("treasure-chest");
    }

    @Test
    void execute_returnsSuccess() {
        PictureAction action = new PictureAction("treasure-chest", gameContext);

        ExecutionResult result = action.execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void isInformationalOnly_isTrue() {
        PictureAction action = new PictureAction("treasure-chest", gameContext);

        assertThat(action.isInformationalOnly()).isTrue();
    }

    @Test
    void constructor_setsPictureIdCorrectly() {
        PictureAction action = new PictureAction("treasure-chest", gameContext);

        assertThat(action.getPictureId()).isEqualTo("treasure-chest");
        assertThat(action.getActionName()).isEqualTo("PictureAction");
    }
}
