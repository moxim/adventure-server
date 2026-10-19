package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.PictureMode;
import com.pdg.adventure.model.action.PictureModeActionData;
import com.pdg.adventure.server.action.PictureModeAction;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@ExtendWith(MockitoExtension.class)
class PictureModeActionMapperTest {

    @Mock private MapperSupporter mapperSupporter;

    @Test
    void mapToBO_keepsTheModeAndTheId() {
        PictureModeActionData data = new PictureModeActionData();
        data.setMode(PictureMode.ON);
        data.setId("id");
        GameContext gameContext = new GameContext();

        PictureModeAction result = new PictureModeActionMapper(gameContext, mapperSupporter).mapToBO(data);
        result.execute();

        assertThat(gameContext.getPictureMode()).isEqualTo(PictureMode.ON);
        assertThat(result.getMode()).isEqualTo(PictureMode.ON);
        assertThat(result.getId()).isEqualTo("id");
    }

    @Test
    void mapToDO_keepsTheModeAndTheId() {
        PictureModeAction action = new PictureModeAction(PictureMode.OFF, new GameContext());

        PictureModeActionData data = new PictureModeActionMapper(new GameContext(), mapperSupporter).mapToDO(action);

        assertThat(data.getMode()).isEqualTo(PictureMode.OFF);
        assertThat(data.getId()).isEqualTo(action.getId());
    }

    @Test
    void mapToBO_withoutAMode_fallsBackToTheDefaultMode() {
        PictureModeAction result = new PictureModeActionMapper(new GameContext(), mapperSupporter)
                .mapToBO(new PictureModeActionData());

        assertThat(result.getMode()).isEqualTo(PictureMode.DEFAULT);
    }
}
