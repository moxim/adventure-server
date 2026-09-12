package com.pdg.adventure.server.mapper.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.condition.PrepositionConditionData;
import com.pdg.adventure.server.condition.PrepositionCondition;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PrepositionConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private GameContext gameContext;

    private PrepositionConditionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PrepositionConditionMapper(mapperSupporter, gameContext);
    }

    @Test
    void mapToBO_carriesThePrepositionTextAndId() {
        PrepositionConditionData data = new PrepositionConditionData();
        data.setId("prep-1");
        data.setPrepositionText("on");

        PrepositionCondition result = mapper.mapToBO(data);

        assertThat(result.getId()).isEqualTo("prep-1");
        assertThat(result.getPreposition()).isEqualTo("on");
    }

    @Test
    void mapToDO_carriesThePrepositionTextAndId() {
        PrepositionCondition condition = new PrepositionCondition("off", gameContext);
        condition.setId("prep-2");

        PrepositionConditionData result = mapper.mapToDO(condition);

        assertThat(result.getId()).isEqualTo("prep-2");
        assertThat(result.getPrepositionText()).isEqualTo("off");
    }

    @Test
    void roundTripMapping_preservesInformation() {
        PrepositionConditionData original = new PrepositionConditionData();
        original.setId("prep-3");
        original.setPrepositionText("on");

        PrepositionCondition businessObject = mapper.mapToBO(original);
        PrepositionConditionData roundTrip = mapper.mapToDO(businessObject);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getPrepositionText()).isEqualTo(original.getPrepositionText());
    }
}
