package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.pdg.adventure.model.action.PlayActionData;
import com.pdg.adventure.server.action.PlayAction;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

class PlayActionMapperTest {

    private PlayActionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PlayActionMapper(mock(GameContext.class), mock(MapperSupporter.class));
    }

    @Test
    void roundTrip_keepsSoundIdAndTimes() {
        PlayActionData data = new PlayActionData();
        data.setSoundId("sound-1");
        data.setTimes(3);

        PlayActionData result = mapper.mapToDO(mapper.mapToBO(data));

        assertThat(result.getSoundId()).isEqualTo("sound-1");
        assertThat(result.getTimes()).isEqualTo(3);
    }

    @Test
    void mapToBO_withNullSoundId_stillBuildsAnAction() {
        PlayAction action = mapper.mapToBO(new PlayActionData());

        assertThat(action.getSoundId()).isNull();
        assertThat(action.getTimes()).isNull();
    }
}
