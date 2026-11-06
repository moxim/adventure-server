package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.pdg.adventure.model.action.AbilityActionData;
import com.pdg.adventure.server.action.AbilityAction;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;

class AbilityActionMapperTest {

    private final AbilityActionMapper mapper = new AbilityActionMapper(new GameContext(), mock(MapperSupporter.class));

    @Test
    void mapToBO_carriesOverBothLimits() {
        AbilityActionData data = new AbilityActionData();
        data.setMaxWeight(30);
        data.setMaxItems(5);

        AbilityAction result = mapper.mapToBO(data);

        assertThat(result.getMaxWeight()).isEqualTo(30);
        assertThat(result.getMaxItems()).isEqualTo(5);
    }

    @Test
    void mapToDO_roundTripsBothLimits() {
        AbilityActionData data = mapper.mapToDO(new AbilityAction(30, 0, new GameContext()));

        assertThat(data.getMaxWeight()).isEqualTo(30);
        assertThat(data.getMaxItems()).isZero();
    }
}
