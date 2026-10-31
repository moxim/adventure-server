package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.OverlayActionData;
import com.pdg.adventure.server.action.OverlayAction;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@ExtendWith(MockitoExtension.class)
class OverlayActionMapperTest {

    @Mock private MapperSupporter mapperSupporter;

    @Test
    void mapToBO_keepsTheCellThePictureAndTheId() {
        OverlayActionData data = new OverlayActionData();
        data.setX(3);
        data.setY(4);
        data.setPictureId("door-open");
        data.setId("id");

        OverlayAction result = new OverlayActionMapper(new GameContext(), mapperSupporter).mapToBO(data);

        assertThat(result.getX()).isEqualTo(3);
        assertThat(result.getY()).isEqualTo(4);
        assertThat(result.getPictureId()).isEqualTo("door-open");
        assertThat(result.getId()).isEqualTo("id");
    }

    @Test
    void mapToDO_keepsTheCellThePictureAndTheId() {
        OverlayAction action = new OverlayAction(3, 4, "door-open", new GameContext());

        OverlayActionData data = new OverlayActionMapper(new GameContext(), mapperSupporter).mapToDO(action);

        assertThat(data.getX()).isEqualTo(3);
        assertThat(data.getY()).isEqualTo(4);
        assertThat(data.getPictureId()).isEqualTo("door-open");
        assertThat(data.getId()).isEqualTo(action.getId());
    }
}
