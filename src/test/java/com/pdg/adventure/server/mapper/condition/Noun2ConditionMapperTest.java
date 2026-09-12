package com.pdg.adventure.server.mapper.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.condition.Noun2ConditionData;
import com.pdg.adventure.server.condition.Noun2Condition;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Noun2ConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private GameContext gameContext;

    private Noun2ConditionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new Noun2ConditionMapper(mapperSupporter, gameContext);
    }

    @Test
    void mapToBO_carriesTheNoun2TextAndId() {
        Noun2ConditionData data = new Noun2ConditionData();
        data.setId("noun2-1");
        data.setNoun2Text("machine");

        Noun2Condition result = mapper.mapToBO(data);

        assertThat(result.getId()).isEqualTo("noun2-1");
        assertThat(result.getNoun2()).isEqualTo("machine");
    }

    @Test
    void mapToDO_carriesTheNoun2TextAndId() {
        Noun2Condition condition = new Noun2Condition("engine", gameContext);
        condition.setId("noun2-2");

        Noun2ConditionData result = mapper.mapToDO(condition);

        assertThat(result.getId()).isEqualTo("noun2-2");
        assertThat(result.getNoun2Text()).isEqualTo("engine");
    }

    @Test
    void roundTripMapping_preservesInformation() {
        Noun2ConditionData original = new Noun2ConditionData();
        original.setId("noun2-3");
        original.setNoun2Text("machine");

        Noun2Condition businessObject = mapper.mapToBO(original);
        Noun2ConditionData roundTrip = mapper.mapToDO(businessObject);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getNoun2Text()).isEqualTo(original.getNoun2Text());
    }
}
