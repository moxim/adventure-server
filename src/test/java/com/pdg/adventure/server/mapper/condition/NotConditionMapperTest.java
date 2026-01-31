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
import com.pdg.adventure.model.condition.HereConditionData;
import com.pdg.adventure.model.condition.NotConditionData;
import com.pdg.adventure.model.condition.PreConditionData;
import com.pdg.adventure.server.condition.HereCondition;
import com.pdg.adventure.server.condition.NotCondition;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

/**
 * Comprehensive unit tests for NotConditionMapper covering:
 * 1. Basic mapToBO conversion (NotConditionData → NotCondition)
 * 2. Basic mapToDO conversion (NotCondition → NotConditionData)
 * 3. ID preservation during mapping
 * 4. Nested condition mapper resolution via MapperSupporter
 * 5. Wrapping and unwrapping of nested conditions
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private Mapper hereConditionMapper;

    @Mock
    private HereConditionData wrappedConditionData;

    @Mock
    private PreCondition wrappedCondition;

    @Mock
    private Item mockItem;

    private NotConditionMapper mapper;

    @BeforeEach
    void setUp() {
        doNothing().when(mapperSupporter).registerMapper(any(), any(), any());
        mapper = new NotConditionMapper(mapperSupporter);
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts NotConditionData to NotCondition")
    @SuppressWarnings("unchecked")
    void mapToBO_shouldConvertNotConditionDataToNotCondition() {
        // Given: NotConditionData wrapping another condition
        NotConditionData conditionData = new NotConditionData();
        conditionData.setId("not-condition-001");
        conditionData.setPreCondition(wrappedConditionData);

        when(mapperSupporter.getMapper(argThat(clazz -> clazz != null && PreConditionData.class.isAssignableFrom(clazz))))
                .thenReturn(hereConditionMapper);
        when(hereConditionMapper.mapToBO(any())).thenReturn(wrappedCondition);

        // When: mapping to business object
        NotCondition result = mapper.mapToBO(conditionData);

        // Then: NotCondition should be created with proper properties
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("not-condition-001");
        assertThat(result.getWrappedCondition()).isEqualTo(wrappedCondition);

        verify(mapperSupporter).getMapper(wrappedConditionData.getClass());
    }

    @Test
    @DisplayName("Test 2: mapToBO - resolves nested condition mapper dynamically")
    @SuppressWarnings("unchecked")
    void mapToBO_shouldResolveNestedConditionMapperDynamically() {
        // Given: NotConditionData with specific nested condition type
        HereConditionData hereData = new HereConditionData();
        hereData.setThingId("ancient-key");

        NotConditionData conditionData = new NotConditionData();
        conditionData.setPreCondition(hereData);

        when(mapperSupporter.getMapper(argThat(clazz -> clazz != null && PreConditionData.class.isAssignableFrom(clazz))))
                .thenReturn(hereConditionMapper);
        when(hereConditionMapper.mapToBO(any())).thenReturn(wrappedCondition);

        // When: mapping to business object
        NotCondition result = mapper.mapToBO(conditionData);

        // Then: correct mapper should be resolved for nested condition
        assertThat(result.getWrappedCondition()).isEqualTo(wrappedCondition);
        verify(mapperSupporter).getMapper(HereConditionData.class);
    }

    @Test
    @DisplayName("Test 3: mapToDO - converts NotCondition to NotConditionData")
    @SuppressWarnings("unchecked")
    void mapToDO_shouldConvertNotConditionToNotConditionData() {
        // Given: NotCondition business object wrapping a real HereCondition
        HereCondition hereCondition = new HereCondition(mockItem);
        NotCondition condition = new NotCondition(hereCondition);
        condition.setId("not-condition-002");

        when(mapperSupporter.getMapper(HereCondition.class)).thenReturn(hereConditionMapper);
        when(hereConditionMapper.mapToDO(any())).thenReturn(wrappedConditionData);

        // When: mapping to data object
        NotConditionData result = mapper.mapToDO(condition);

        // Then: NotConditionData should be created with proper properties
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("not-condition-002");
        assertThat(result.getPreCondition()).isEqualTo(wrappedConditionData);

        verify(mapperSupporter).getMapper(HereCondition.class);
    }

    @Test
    @DisplayName("Test 4: mapToDO - preserves ID during conversion")
    @SuppressWarnings("unchecked")
    void mapToDO_shouldPreserveIdDuringConversion() {
        // Given: Multiple NotCondition objects with different IDs
        HereCondition hereCondition = new HereCondition(mockItem);
        NotCondition condition1 = new NotCondition(hereCondition);
        condition1.setId("not-001");

        NotCondition condition2 = new NotCondition(hereCondition);
        condition2.setId("not-002");

        when(mapperSupporter.getMapper(HereCondition.class)).thenReturn(hereConditionMapper);
        when(hereConditionMapper.mapToDO(any())).thenReturn(wrappedConditionData);

        // When: mapping both conditions
        NotConditionData result1 = mapper.mapToDO(condition1);
        NotConditionData result2 = mapper.mapToDO(condition2);

        // Then: IDs should be preserved and different
        assertThat(result1.getId()).isEqualTo("not-001");
        assertThat(result2.getId()).isEqualTo("not-002");
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Test 5: Round-trip mapping - data → BO → data preserves information")
    @SuppressWarnings("unchecked")
    void roundTripMapping_shouldPreserveInformation() {
        // Given: Original NotConditionData
        NotConditionData originalData = new NotConditionData();
        originalData.setId("round-trip-not");
        originalData.setPreCondition(wrappedConditionData);

        HereCondition hereCondition = new HereCondition(mockItem);

        // Mock mapToBO path
        when(mapperSupporter.getMapper(argThat(clazz -> clazz != null && PreConditionData.class.isAssignableFrom(clazz))))
                .thenReturn(hereConditionMapper);
        when(hereConditionMapper.mapToBO(any())).thenReturn(hereCondition);

        // Mock mapToDO path
        when(mapperSupporter.getMapper(HereCondition.class)).thenReturn(hereConditionMapper);
        when(hereConditionMapper.mapToDO(any())).thenReturn(wrappedConditionData);

        // When: mapping to BO and back to DO
        NotCondition businessObject = mapper.mapToBO(originalData);
        NotConditionData roundTripData = mapper.mapToDO(businessObject);

        // Then: data should be preserved
        assertThat(roundTripData.getId()).isEqualTo(originalData.getId());
        assertThat(roundTripData.getPreCondition()).isEqualTo(originalData.getPreCondition());
    }

    @Test
    @DisplayName("Test 6: mapToBO - handles deeply nested Not conditions")
    @SuppressWarnings("unchecked")
    void mapToBO_shouldHandleDeeplyNestedNotConditions() {
        // Given: NotConditionData wrapping another NotConditionData (double negation)
        NotConditionData innerNotData = new NotConditionData();
        innerNotData.setPreCondition(wrappedConditionData);

        NotConditionData outerNotData = new NotConditionData();
        outerNotData.setId("double-not-condition");
        outerNotData.setPreCondition(innerNotData);

        NotCondition innerNotCondition = new NotCondition(wrappedCondition);

        // Mock for inner NOT condition
        when(mapperSupporter.getMapper(argThat(clazz -> clazz != null && PreConditionData.class.isAssignableFrom(clazz))))
                .thenReturn(hereConditionMapper);
        when(hereConditionMapper.mapToBO(wrappedConditionData)).thenReturn(wrappedCondition);
        when(hereConditionMapper.mapToBO(innerNotData)).thenReturn(innerNotCondition);

        // When: mapping outer NOT condition
        NotCondition result = mapper.mapToBO(outerNotData);

        // Then: should properly wrap the nested NOT condition
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("double-not-condition");
        assertThat(result.getWrappedCondition()).isInstanceOf(NotCondition.class);
    }
}
