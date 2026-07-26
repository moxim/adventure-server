package com.pdg.adventure.server.mapper.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.condition.ChanceConditionData;
import com.pdg.adventure.server.condition.ChanceCondition;
import com.pdg.adventure.server.support.MapperSupporter;

@ExtendWith(MockitoExtension.class)
class ChanceConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    private ChanceConditionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ChanceConditionMapper(mapperSupporter);
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts ChanceConditionData to ChanceCondition")
    void mapToBO_shouldConvertChanceConditionDataToChanceCondition() {
        ChanceConditionData data = new ChanceConditionData();
        data.setId("chance-001");
        data.setValue(75);

        ChanceCondition result = mapper.mapToBO(data);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("chance-001");
        assertThat(result.getChance()).isEqualTo(75);
    }

    @Test
    @DisplayName("Test 2: mapToDO - converts ChanceCondition to ChanceConditionData")
    void mapToDO_shouldConvertChanceConditionToChanceConditionData() {
        ChanceCondition condition = new ChanceCondition(40);
        condition.setId("chance-002");

        ChanceConditionData result = mapper.mapToDO(condition);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("chance-002");
        assertThat(result.getValue()).isEqualTo(40);
    }

    @Test
    @DisplayName("Test 3: Round-trip mapping - data -> BO -> data preserves information")
    void roundTripMapping_shouldPreserveInformation() {
        ChanceConditionData original = new ChanceConditionData();
        original.setId("round-trip-chance");
        original.setValue(15);

        ChanceCondition bo = mapper.mapToBO(original);
        ChanceConditionData roundTrip = mapper.mapToDO(bo);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getValue()).isEqualTo(original.getValue());
    }
}
