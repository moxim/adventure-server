package com.pdg.adventure.server.mapper.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.condition.EqualsConditionData;
import com.pdg.adventure.server.condition.EqualsCondition;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EqualsConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private VariableProvider variableProvider;

    private EqualsConditionMapper mapper;

    @BeforeEach
    void setUp() {
        doNothing().when(mapperSupporter).registerMapper(any(), any(), any());
        when(mapperSupporter.getVariableProvider()).thenReturn(variableProvider);
        mapper = new EqualsConditionMapper(mapperSupporter);
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts EqualsConditionData to EqualsCondition")
    void mapToBO_shouldConvertEqualsConditionDataToEqualsCondition() {
        EqualsConditionData data = new EqualsConditionData("score", "100");
        data.setId("equals-001");

        EqualsCondition result = mapper.mapToBO(data);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("equals-001");
        assertThat(result.getVariableName()).isEqualTo("score");
        assertThat(result.getValue()).isEqualTo("100");
    }

    @Test
    @DisplayName("Test 2: mapToBO - passes VariableProvider from MapperSupporter to condition")
    void mapToBO_shouldPassVariableProviderToCondition() {
        EqualsConditionData data = new EqualsConditionData("lives", "3");

        EqualsCondition result = mapper.mapToBO(data);

        assertThat(result.getVariableProvider()).isEqualTo(variableProvider);
    }

    @Test
    @DisplayName("Test 3: mapToDO - converts EqualsCondition to EqualsConditionData")
    void mapToDO_shouldConvertEqualsConditionToEqualsConditionData() {
        EqualsCondition condition = new EqualsCondition("level", "5", variableProvider);
        condition.setId("equals-002");

        EqualsConditionData result = mapper.mapToDO(condition);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("equals-002");
        assertThat(result.getVariableName()).isEqualTo("level");
        assertThat(result.getValue()).isEqualTo("5");
    }

    @Test
    @DisplayName("Test 4: mapToDO - preserves ID during conversion")
    void mapToDO_shouldPreserveIdDuringConversion() {
        EqualsCondition condition1 = new EqualsCondition("x", "1", variableProvider);
        condition1.setId("equals-id-001");

        EqualsCondition condition2 = new EqualsCondition("x", "1", variableProvider);
        condition2.setId("equals-id-002");

        EqualsConditionData result1 = mapper.mapToDO(condition1);
        EqualsConditionData result2 = mapper.mapToDO(condition2);

        assertThat(result1.getId()).isEqualTo("equals-id-001");
        assertThat(result2.getId()).isEqualTo("equals-id-002");
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Test 5: Round-trip mapping - data → BO → data preserves information")
    void roundTripMapping_shouldPreserveInformation() {
        EqualsConditionData original = new EqualsConditionData("score", "100");
        original.setId("round-trip-equals");

        EqualsCondition bo = mapper.mapToBO(original);
        EqualsConditionData roundTrip = mapper.mapToDO(bo);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getVariableName()).isEqualTo(original.getVariableName());
        assertThat(roundTrip.getValue()).isEqualTo(original.getValue());
    }
}
