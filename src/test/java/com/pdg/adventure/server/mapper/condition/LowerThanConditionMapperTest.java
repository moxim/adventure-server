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

import com.pdg.adventure.model.condition.LowerThanConditionData;
import com.pdg.adventure.server.condition.LowerThanCondition;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LowerThanConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private VariableProvider variableProvider;

    private LowerThanConditionMapper mapper;

    @BeforeEach
    void setUp() {
        doNothing().when(mapperSupporter).registerMapper(any(), any(), any());
        when(mapperSupporter.getVariableProvider()).thenReturn(variableProvider);
        mapper = new LowerThanConditionMapper(mapperSupporter);
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts LowerThanConditionData to LowerThanCondition")
    void mapToBO_shouldConvertLowerThanConditionDataToLowerThanCondition() {
        LowerThanConditionData data = new LowerThanConditionData();
        data.setId("lt-001");
        data.setVariableName("health");
        data.setValue(10);

        LowerThanCondition result = mapper.mapToBO(data);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("lt-001");
        assertThat(result.getVariableName()).isEqualTo("health");
        assertThat(result.getValue()).isEqualTo(10);
    }

    @Test
    @DisplayName("Test 2: mapToBO - passes VariableProvider from MapperSupporter to condition")
    void mapToBO_shouldPassVariableProviderToCondition() {
        LowerThanConditionData data = new LowerThanConditionData();
        data.setVariableName("lives");
        data.setValue(3);

        LowerThanCondition result = mapper.mapToBO(data);

        assertThat(result.getVariableProvider()).isEqualTo(variableProvider);
    }

    @Test
    @DisplayName("Test 3: mapToDO - converts LowerThanCondition to LowerThanConditionData")
    void mapToDO_shouldConvertLowerThanConditionToLowerThanConditionData() {
        LowerThanCondition condition = new LowerThanCondition("energy", 5, variableProvider);
        condition.setId("lt-002");

        LowerThanConditionData result = mapper.mapToDO(condition);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("lt-002");
        assertThat(result.getVariableName()).isEqualTo("energy");
        assertThat(result.getValue()).isEqualTo(5);
    }

    @Test
    @DisplayName("Test 4: mapToDO - preserves ID during conversion")
    void mapToDO_shouldPreserveIdDuringConversion() {
        LowerThanCondition condition1 = new LowerThanCondition("x", 5, variableProvider);
        condition1.setId("lt-id-001");

        LowerThanCondition condition2 = new LowerThanCondition("x", 5, variableProvider);
        condition2.setId("lt-id-002");

        LowerThanConditionData result1 = mapper.mapToDO(condition1);
        LowerThanConditionData result2 = mapper.mapToDO(condition2);

        assertThat(result1.getId()).isEqualTo("lt-id-001");
        assertThat(result2.getId()).isEqualTo("lt-id-002");
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Test 5: Round-trip mapping - data → BO → data preserves information")
    void roundTripMapping_shouldPreserveInformation() {
        LowerThanConditionData original = new LowerThanConditionData();
        original.setId("round-trip-lt");
        original.setVariableName("health");
        original.setValue(10);

        LowerThanCondition bo = mapper.mapToBO(original);
        LowerThanConditionData roundTrip = mapper.mapToDO(bo);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getVariableName()).isEqualTo(original.getVariableName());
        assertThat(roundTrip.getValue()).isEqualTo(original.getValue());
    }
}
