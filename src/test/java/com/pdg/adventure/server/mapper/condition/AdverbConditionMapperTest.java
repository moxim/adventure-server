package com.pdg.adventure.server.mapper.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.condition.AdverbConditionData;
import com.pdg.adventure.server.condition.AdverbCondition;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdverbConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private GameContext gameContext;

    private AdverbConditionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AdverbConditionMapper(mapperSupporter, gameContext);
    }

    @Test
    void mapToBO_carriesTheAdverbTextAndId() {
        AdverbConditionData data = new AdverbConditionData();
        data.setId("adv-1");
        data.setAdverbText("slowly");

        AdverbCondition result = mapper.mapToBO(data);

        assertThat(result.getId()).isEqualTo("adv-1");
        assertThat(result.getAdverb()).isEqualTo("slowly");
    }

    @Test
    void mapToDO_carriesTheAdverbTextAndId() {
        AdverbCondition condition = new AdverbCondition("quickly", gameContext);
        condition.setId("adv-2");

        AdverbConditionData result = mapper.mapToDO(condition);

        assertThat(result.getId()).isEqualTo("adv-2");
        assertThat(result.getAdverbText()).isEqualTo("quickly");
    }

    @Test
    void roundTripMapping_preservesInformation() {
        AdverbConditionData original = new AdverbConditionData();
        original.setId("adv-3");
        original.setAdverbText("slowly");

        AdverbCondition businessObject = mapper.mapToBO(original);
        AdverbConditionData roundTrip = mapper.mapToDO(businessObject);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getAdverbText()).isEqualTo(original.getAdverbText());
    }
}
