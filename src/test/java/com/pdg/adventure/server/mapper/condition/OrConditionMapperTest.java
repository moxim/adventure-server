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
import com.pdg.adventure.model.condition.CarriedConditionData;
import com.pdg.adventure.model.condition.HereConditionData;
import com.pdg.adventure.model.condition.OrConditionData;
import com.pdg.adventure.model.condition.PreConditionData;
import com.pdg.adventure.server.condition.OrCondition;
import com.pdg.adventure.server.support.MapperSupporter;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrConditionMapperTest {

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

    private OrConditionMapper mapper;

    @BeforeEach
    void setUp() {
        doNothing().when(mapperSupporter).registerMapper(any(), any(), any());
        mapper = new OrConditionMapper(mapperSupporter);
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts OrConditionData to OrCondition")
    @SuppressWarnings("unchecked")
    void mapToBO_shouldConvertOrConditionDataToOrCondition() {
        OrConditionData data = new OrConditionData();
        data.setId("or-001");
        data.setPreCondition(firstConditionData);
        data.setAnotherPreCondition(secondConditionData);

        when(mapperSupporter.getMapper(argThat(clazz -> clazz != null && PreConditionData.class.isAssignableFrom(clazz))))
                .thenReturn(innerMapper);
        when(innerMapper.mapToBO(firstConditionData)).thenReturn(firstConditionBO);
        when(innerMapper.mapToBO(secondConditionData)).thenReturn(secondConditionBO);

        OrCondition result = mapper.mapToBO(data);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("or-001");
        assertThat(result.getPreCondition()).isEqualTo(firstConditionBO);
        assertThat(result.getAnotherPreCondition()).isEqualTo(secondConditionBO);
    }

    @Test
    @DisplayName("Test 2: mapToBO - resolves nested condition mappers dynamically")
    @SuppressWarnings("unchecked")
    void mapToBO_shouldResolveNestedConditionMappersDynamically() {
        CarriedConditionData carriedData = new CarriedConditionData();
        HereConditionData hereData = new HereConditionData();

        OrConditionData data = new OrConditionData();
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
    @DisplayName("Test 3: mapToDO - converts OrCondition to OrConditionData")
    @SuppressWarnings("unchecked")
    void mapToDO_shouldConvertOrConditionToOrConditionData() {
        OrCondition condition = new OrCondition(firstConditionBO, secondConditionBO);
        condition.setId("or-002");

        when(mapperSupporter.getMapper(firstConditionBO.getClass())).thenReturn(innerMapper);
        when(mapperSupporter.getMapper(secondConditionBO.getClass())).thenReturn(innerMapper);
        when(innerMapper.mapToDO(firstConditionBO)).thenReturn(firstConditionData);
        when(innerMapper.mapToDO(secondConditionBO)).thenReturn(secondConditionData);

        OrConditionData result = mapper.mapToDO(condition);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("or-002");
        assertThat(result.getPreCondition()).isEqualTo(firstConditionData);
        assertThat(result.getAnotherPreCondition()).isEqualTo(secondConditionData);
    }

    @Test
    @DisplayName("Test 4: mapToDO - preserves ID during conversion")
    @SuppressWarnings("unchecked")
    void mapToDO_shouldPreserveIdDuringConversion() {
        OrCondition condition1 = new OrCondition(firstConditionBO, secondConditionBO);
        condition1.setId("or-id-001");

        OrCondition condition2 = new OrCondition(firstConditionBO, secondConditionBO);
        condition2.setId("or-id-002");

        when(mapperSupporter.getMapper(any())).thenReturn(innerMapper);
        when(innerMapper.mapToDO(any())).thenReturn(firstConditionData);

        OrConditionData result1 = mapper.mapToDO(condition1);
        OrConditionData result2 = mapper.mapToDO(condition2);

        assertThat(result1.getId()).isEqualTo("or-id-001");
        assertThat(result2.getId()).isEqualTo("or-id-002");
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Test 5: Round-trip mapping - data → BO → data preserves structure")
    @SuppressWarnings("unchecked")
    void roundTripMapping_shouldPreserveInformation() {
        CarriedConditionData carriedData = new CarriedConditionData();
        HereConditionData hereData = new HereConditionData();

        OrConditionData original = new OrConditionData();
        original.setId("round-trip-or");
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

        OrCondition bo = mapper.mapToBO(original);
        OrConditionData roundTrip = mapper.mapToDO(bo);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getPreCondition()).isEqualTo(original.getPreCondition());
        assertThat(roundTrip.getAnotherPreCondition()).isEqualTo(original.getAnotherPreCondition());
    }
}
