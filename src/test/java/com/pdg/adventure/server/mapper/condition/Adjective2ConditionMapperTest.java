package com.pdg.adventure.server.mapper.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.condition.Adjective2ConditionData;
import com.pdg.adventure.server.condition.Adjective2Condition;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class Adjective2ConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private GameContext gameContext;

    private Adjective2ConditionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new Adjective2ConditionMapper(mapperSupporter, gameContext);
    }

    @Test
    void mapToBO_carriesTheAdjective2TextAndId() {
        Adjective2ConditionData data = new Adjective2ConditionData();
        data.setId("adj2-1");
        data.setAdjective2Text("ancient");

        Adjective2Condition result = mapper.mapToBO(data);

        assertThat(result.getId()).isEqualTo("adj2-1");
        assertThat(result.getAdjective2()).isEqualTo("ancient");
    }

    @Test
    void mapToDO_carriesTheAdjective2TextAndId() {
        Adjective2Condition condition = new Adjective2Condition("rusty", gameContext);
        condition.setId("adj2-2");

        Adjective2ConditionData result = mapper.mapToDO(condition);

        assertThat(result.getId()).isEqualTo("adj2-2");
        assertThat(result.getAdjective2Text()).isEqualTo("rusty");
    }

    @Test
    void roundTripMapping_preservesInformation() {
        Adjective2ConditionData original = new Adjective2ConditionData();
        original.setId("adj2-3");
        original.setAdjective2Text("ancient");

        Adjective2Condition businessObject = mapper.mapToBO(original);
        Adjective2ConditionData roundTrip = mapper.mapToDO(businessObject);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getAdjective2Text()).isEqualTo(original.getAdjective2Text());
    }
}
