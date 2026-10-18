package com.pdg.adventure.server.mapper.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.condition.ClickedConditionData;
import com.pdg.adventure.server.condition.ClickedCondition;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

@ExtendWith(MockitoExtension.class)
class ClickedConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;
    @Mock
    private GameContext gameContext;

    private ClickedConditionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ClickedConditionMapper(mapperSupporter, gameContext);
    }

    @Test
    void mapToBO_convertsDataToCondition() {
        ClickedConditionData data = new ClickedConditionData();
        data.setId("clicked-001");
        data.setX(3);
        data.setY(4);

        ClickedCondition result = mapper.mapToBO(data);

        assertThat(result.getId()).isEqualTo("clicked-001");
        assertThat(result.getX()).isEqualTo(3);
        assertThat(result.getY()).isEqualTo(4);
    }

    @Test
    void mapToDO_convertsConditionToData() {
        ClickedCondition condition = new ClickedCondition(7, 9, gameContext);
        condition.setId("clicked-002");

        ClickedConditionData result = mapper.mapToDO(condition);

        assertThat(result.getId()).isEqualTo("clicked-002");
        assertThat(result.getX()).isEqualTo(7);
        assertThat(result.getY()).isEqualTo(9);
    }

    @Test
    void roundTrip_preservesIdAndCoordinates() {
        ClickedConditionData original = new ClickedConditionData();
        original.setId("round-trip-clicked");
        original.setX(0);
        original.setY(9);

        ClickedConditionData roundTrip = mapper.mapToDO(mapper.mapToBO(original));

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getX()).isEqualTo(0);
        assertThat(roundTrip.getY()).isEqualTo(9);
    }
}
