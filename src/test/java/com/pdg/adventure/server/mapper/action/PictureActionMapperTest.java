package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.PictureActionData;
import com.pdg.adventure.server.action.PictureAction;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@ExtendWith(MockitoExtension.class)
class PictureActionMapperTest {

    @Mock
    private GameContext gameContext;

    @Mock
    private MapperSupporter mapperSupporter;

    private PictureActionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PictureActionMapper(gameContext, mapperSupporter);
    }

    @Test
    void mapToBO_createsPictureActionWithGivenPictureId() {
        PictureActionData data = new PictureActionData();
        data.setPictureId("treasure-chest");

        PictureAction result = mapper.mapToBO(data);

        assertThat(result.getPictureId()).isEqualTo("treasure-chest");
    }

    @Test
    void mapToDO_convertsPictureActionToData() {
        PictureAction action = new PictureAction("treasure-chest", gameContext);

        PictureActionData result = mapper.mapToDO(action);

        assertThat(result.getPictureId()).isEqualTo("treasure-chest");
    }
}
