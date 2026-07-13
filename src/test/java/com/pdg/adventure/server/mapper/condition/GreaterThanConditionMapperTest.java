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

import com.pdg.adventure.model.condition.GreaterThanConditionData;
import com.pdg.adventure.server.condition.GreaterThanCondition;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GreaterThanConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private VariableProvider variableProvider;

    private GreaterThanConditionMapper mapper;

    @BeforeEach
    void setUp() {
        doNothing().when(mapperSupporter).registerMapper(any(), any(), any());
        when(mapperSupporter.getVariableProvider()).thenReturn(variableProvider);
        mapper = new GreaterThanConditionMapper(mapperSupporter);
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts GreaterThanConditionData to GreaterThanCondition")
    void mapToBO_shouldConvertGreaterThanConditionDataToGreaterThanCondition() {
        GreaterThanConditionData data = new GreaterThanConditionData();
        data.setId("gt-001");
        data.setVariableName("score");
        data.setValue(50);

        GreaterThanCondition result = mapper.mapToBO(data);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("gt-001");
        assertThat(result.getVariableName()).isEqualTo("score");
        assertThat(result.getValue()).isEqualTo(50);
    }

    @Test
    @DisplayName("Test 2: mapToBO - passes VariableProvider from MapperSupporter to condition")
    void mapToBO_shouldPassVariableProviderToCondition() {
        GreaterThanConditionData data = new GreaterThanConditionData();
        data.setVariableName("lives");
        data.setValue(0);

        GreaterThanCondition result = mapper.mapToBO(data);

        assertThat(result.getVariableProvider()).isEqualTo(variableProvider);
    }

    @Test
    @DisplayName("Test 3: mapToDO - converts GreaterThanCondition to GreaterThanConditionData")
    void mapToDO_shouldConvertGreaterThanConditionToGreaterThanConditionData() {
        GreaterThanCondition condition = new GreaterThanCondition("level", 10, variableProvider);
        condition.setId("gt-002");

        GreaterThanConditionData result = mapper.mapToDO(condition);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("gt-002");
        assertThat(result.getVariableName()).isEqualTo("level");
        assertThat(result.getValue()).isEqualTo(10);
    }

    @Test
    @DisplayName("Test 4: mapToDO - preserves ID during conversion")
    void mapToDO_shouldPreserveIdDuringConversion() {
        GreaterThanCondition condition1 = new GreaterThanCondition("x", 5, variableProvider);
        condition1.setId("gt-id-001");

        GreaterThanCondition condition2 = new GreaterThanCondition("x", 5, variableProvider);
        condition2.setId("gt-id-002");

        GreaterThanConditionData result1 = mapper.mapToDO(condition1);
        GreaterThanConditionData result2 = mapper.mapToDO(condition2);

        assertThat(result1.getId()).isEqualTo("gt-id-001");
        assertThat(result2.getId()).isEqualTo("gt-id-002");
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Test 5: Round-trip mapping - data → BO → data preserves information")
    void roundTripMapping_shouldPreserveInformation() {
        GreaterThanConditionData original = new GreaterThanConditionData();
        original.setId("round-trip-gt");
        original.setVariableName("score");
        original.setValue(50);

        GreaterThanCondition bo = mapper.mapToBO(original);
        GreaterThanConditionData roundTrip = mapper.mapToDO(bo);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getVariableName()).isEqualTo(original.getVariableName());
        assertThat(roundTrip.getValue()).isEqualTo(original.getValue());
    }
}
