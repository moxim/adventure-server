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

import com.pdg.adventure.model.condition.LessThanConditionData;
import com.pdg.adventure.server.condition.LessThanCondition;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LessThanConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private VariableProvider variableProvider;

    private LessThanConditionMapper mapper;

    @BeforeEach
    void setUp() {
        doNothing().when(mapperSupporter).registerMapper(any(), any(), any());
        when(mapperSupporter.getVariableProvider()).thenReturn(variableProvider);
        mapper = new LessThanConditionMapper(mapperSupporter);
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts LessThanConditionData to LessThanCondition")
    void mapToBO_shouldConvertLessThanConditionDataToLessThanCondition() {
        LessThanConditionData data = new LessThanConditionData();
        data.setId("lt-001");
        data.setVariableName("health");
        data.setValue(10);

        LessThanCondition result = mapper.mapToBO(data);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("lt-001");
        assertThat(result.getVariableName()).isEqualTo("health");
        assertThat(result.getValue()).isEqualTo(10);
    }

    @Test
    @DisplayName("Test 2: mapToBO - passes VariableProvider from MapperSupporter to condition")
    void mapToBO_shouldPassVariableProviderToCondition() {
        LessThanConditionData data = new LessThanConditionData();
        data.setVariableName("lives");
        data.setValue(3);

        LessThanCondition result = mapper.mapToBO(data);

        assertThat(result.getVariableProvider()).isEqualTo(variableProvider);
    }

    @Test
    @DisplayName("Test 3: mapToDO - converts LessThanCondition to LessThanConditionData")
    void mapToDO_shouldConvertLessThanConditionToLessThanConditionData() {
        LessThanCondition condition = new LessThanCondition("energy", 5, variableProvider);
        condition.setId("lt-002");

        LessThanConditionData result = mapper.mapToDO(condition);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("lt-002");
        assertThat(result.getVariableName()).isEqualTo("energy");
        assertThat(result.getValue()).isEqualTo(5);
    }

    @Test
    @DisplayName("Test 4: mapToDO - preserves ID during conversion")
    void mapToDO_shouldPreserveIdDuringConversion() {
        LessThanCondition condition1 = new LessThanCondition("x", 5, variableProvider);
        condition1.setId("lt-id-001");

        LessThanCondition condition2 = new LessThanCondition("x", 5, variableProvider);
        condition2.setId("lt-id-002");

        LessThanConditionData result1 = mapper.mapToDO(condition1);
        LessThanConditionData result2 = mapper.mapToDO(condition2);

        assertThat(result1.getId()).isEqualTo("lt-id-001");
        assertThat(result2.getId()).isEqualTo("lt-id-002");
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Test 5: Round-trip mapping - data → BO → data preserves information")
    void roundTripMapping_shouldPreserveInformation() {
        LessThanConditionData original = new LessThanConditionData();
        original.setId("round-trip-lt");
        original.setVariableName("health");
        original.setValue(10);

        LessThanCondition bo = mapper.mapToBO(original);
        LessThanConditionData roundTrip = mapper.mapToDO(bo);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getVariableName()).isEqualTo(original.getVariableName());
        assertThat(roundTrip.getValue()).isEqualTo(original.getValue());
    }
}
