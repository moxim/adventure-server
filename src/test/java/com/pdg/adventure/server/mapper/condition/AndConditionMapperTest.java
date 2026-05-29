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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Mapper;
import com.pdg.adventure.api.PreCondition;
import com.pdg.adventure.model.condition.AndConditionData;
import com.pdg.adventure.model.condition.CarriedConditionData;
import com.pdg.adventure.model.condition.HereConditionData;
import com.pdg.adventure.model.condition.PreConditionData;
import com.pdg.adventure.server.condition.AndCondition;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AndConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private Mapper innerMapper;

    @Mock
    private PreCondition firstConditionBO;

    @Mock
    private PreCondition secondConditionBO;

    @Mock
    private PreConditionData firstConditionData;

    @Mock
    private PreConditionData secondConditionData;

    @Mock
    private GameContext gameContext;

    @Mock
    private Item mockItem;

    private AndConditionMapper mapper;

    @BeforeEach
    void setUp() {
        doNothing().when(mapperSupporter).registerMapper(any(), any(), any());
        mapper = new AndConditionMapper(mapperSupporter);
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts AndConditionData to AndCondition")
    @SuppressWarnings("unchecked")
    void mapToBO_shouldConvertAndConditionDataToAndCondition() {
        AndConditionData data = new AndConditionData();
        data.setId("and-001");
        data.setPreCondition(firstConditionData);
        data.setAnotherPreCondition(secondConditionData);

        when(mapperSupporter.getMapper(argThat(clazz -> clazz != null && PreConditionData.class.isAssignableFrom(clazz))))
                .thenReturn(innerMapper);
        when(innerMapper.mapToBO(firstConditionData)).thenReturn(firstConditionBO);
        when(innerMapper.mapToBO(secondConditionData)).thenReturn(secondConditionBO);

        AndCondition result = mapper.mapToBO(data);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("and-001");
        assertThat(result.getPreCondition()).isEqualTo(firstConditionBO);
        assertThat(result.getAnotherPreCondition()).isEqualTo(secondConditionBO);
    }

    @Test
    @DisplayName("Test 2: mapToBO - resolves nested condition mappers dynamically")
    @SuppressWarnings("unchecked")
    void mapToBO_shouldResolveNestedConditionMappersDynamically() {
        CarriedConditionData carriedData = new CarriedConditionData();
        HereConditionData hereData = new HereConditionData();

        AndConditionData data = new AndConditionData();
        data.setPreCondition(carriedData);
        data.setAnotherPreCondition(hereData);

        when(mapperSupporter.getMapper(argThat(clazz -> clazz != null && PreConditionData.class.isAssignableFrom(clazz))))
                .thenReturn(innerMapper);
        when(innerMapper.mapToBO(any())).thenReturn(firstConditionBO);

        mapper.mapToBO(data);

        verify(mapperSupporter).getMapper(CarriedConditionData.class);
        verify(mapperSupporter).getMapper(HereConditionData.class);
    }

    @Test
    @DisplayName("Test 3: mapToDO - converts AndCondition to AndConditionData")
    @SuppressWarnings("unchecked")
    void mapToDO_shouldConvertAndConditionToAndConditionData() {
        AndCondition condition = new AndCondition(firstConditionBO, secondConditionBO);
        condition.setId("and-002");

        when(mapperSupporter.getMapper(firstConditionBO.getClass())).thenReturn(innerMapper);
        when(mapperSupporter.getMapper(secondConditionBO.getClass())).thenReturn(innerMapper);
        when(innerMapper.mapToDO(firstConditionBO)).thenReturn(firstConditionData);
        when(innerMapper.mapToDO(secondConditionBO)).thenReturn(secondConditionData);

        AndConditionData result = mapper.mapToDO(condition);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("and-002");
        assertThat(result.getPreCondition()).isEqualTo(firstConditionData);
        assertThat(result.getAnotherPreCondition()).isEqualTo(secondConditionData);
    }

    @Test
    @DisplayName("Test 4: mapToDO - preserves ID during conversion")
    @SuppressWarnings("unchecked")
    void mapToDO_shouldPreserveIdDuringConversion() {
        AndCondition condition1 = new AndCondition(firstConditionBO, secondConditionBO);
        condition1.setId("and-id-001");

        AndCondition condition2 = new AndCondition(firstConditionBO, secondConditionBO);
        condition2.setId("and-id-002");

        when(mapperSupporter.getMapper(any())).thenReturn(innerMapper);
        when(innerMapper.mapToDO(any())).thenReturn(firstConditionData);

        AndConditionData result1 = mapper.mapToDO(condition1);
        AndConditionData result2 = mapper.mapToDO(condition2);

        assertThat(result1.getId()).isEqualTo("and-id-001");
        assertThat(result2.getId()).isEqualTo("and-id-002");
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Test 5: Round-trip mapping - data → BO → data preserves structure")
    @SuppressWarnings("unchecked")
    void roundTripMapping_shouldPreserveInformation() {
        CarriedConditionData carriedData = new CarriedConditionData();
        HereConditionData hereData = new HereConditionData();

        AndConditionData original = new AndConditionData();
        original.setId("round-trip-and");
        original.setPreCondition(carriedData);
        original.setAnotherPreCondition(hereData);

        // mapToBO path
        when(mapperSupporter.getMapper(argThat(clazz -> clazz != null && PreConditionData.class.isAssignableFrom(clazz))))
                .thenReturn(innerMapper);
        when(innerMapper.mapToBO(carriedData)).thenReturn(firstConditionBO);
        when(innerMapper.mapToBO(hereData)).thenReturn(secondConditionBO);

        // mapToDO path
        when(mapperSupporter.getMapper(firstConditionBO.getClass())).thenReturn(innerMapper);
        when(mapperSupporter.getMapper(secondConditionBO.getClass())).thenReturn(innerMapper);
        when(innerMapper.mapToDO(firstConditionBO)).thenReturn(carriedData);
        when(innerMapper.mapToDO(secondConditionBO)).thenReturn(hereData);

        AndCondition bo = mapper.mapToBO(original);
        AndConditionData roundTrip = mapper.mapToDO(bo);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getPreCondition()).isEqualTo(original.getPreCondition());
        assertThat(roundTrip.getAnotherPreCondition()).isEqualTo(original.getAnotherPreCondition());
    }
}
