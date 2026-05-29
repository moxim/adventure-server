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

import com.pdg.adventure.model.condition.SameConditionData;
import com.pdg.adventure.server.condition.SameCondition;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.support.VariableProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SameConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private VariableProvider variableProvider;

    private SameConditionMapper mapper;

    @BeforeEach
    void setUp() {
        doNothing().when(mapperSupporter).registerMapper(any(), any(), any());
        when(mapperSupporter.getVariableProvider()).thenReturn(variableProvider);
        mapper = new SameConditionMapper(mapperSupporter);
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts SameConditionData to SameCondition")
    void mapToBO_shouldConvertSameConditionDataToSameCondition() {
        SameConditionData data = new SameConditionData();
        data.setId("same-001");
        data.setVariableNameOne("colour");
        data.setVariableNameTwo("targetColour");

        SameCondition result = mapper.mapToBO(data);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("same-001");
        assertThat(result.getVariableNameOne()).isEqualTo("colour");
        assertThat(result.getVariableNameTwo()).isEqualTo("targetColour");
    }

    @Test
    @DisplayName("Test 2: mapToBO - passes VariableProvider from MapperSupporter to condition")
    void mapToBO_shouldPassVariableProviderToCondition() {
        SameConditionData data = new SameConditionData();
        data.setVariableNameOne("a");
        data.setVariableNameTwo("b");

        SameCondition result = mapper.mapToBO(data);

        assertThat(result.getVariableProvider()).isEqualTo(variableProvider);
    }

    @Test
    @DisplayName("Test 3: mapToDO - converts SameCondition to SameConditionData")
    void mapToDO_shouldConvertSameConditionToSameConditionData() {
        SameCondition condition = new SameCondition("colourA", "colourB", variableProvider);
        condition.setId("same-002");

        SameConditionData result = mapper.mapToDO(condition);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("same-002");
        assertThat(result.getVariableNameOne()).isEqualTo("colourA");
        assertThat(result.getVariableNameTwo()).isEqualTo("colourB");
    }

    @Test
    @DisplayName("Test 4: mapToDO - preserves ID during conversion")
    void mapToDO_shouldPreserveIdDuringConversion() {
        SameCondition condition1 = new SameCondition("x", "y", variableProvider);
        condition1.setId("same-id-001");

        SameCondition condition2 = new SameCondition("x", "y", variableProvider);
        condition2.setId("same-id-002");

        SameConditionData result1 = mapper.mapToDO(condition1);
        SameConditionData result2 = mapper.mapToDO(condition2);

        assertThat(result1.getId()).isEqualTo("same-id-001");
        assertThat(result2.getId()).isEqualTo("same-id-002");
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Test 5: Round-trip mapping - data → BO → data preserves information")
    void roundTripMapping_shouldPreserveInformation() {
        SameConditionData original = new SameConditionData();
        original.setId("round-trip-same");
        original.setVariableNameOne("colour");
        original.setVariableNameTwo("targetColour");

        SameCondition bo = mapper.mapToBO(original);
        SameConditionData roundTrip = mapper.mapToDO(bo);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getVariableNameOne()).isEqualTo(original.getVariableNameOne());
        assertThat(roundTrip.getVariableNameTwo()).isEqualTo(original.getVariableNameTwo());
    }
}
