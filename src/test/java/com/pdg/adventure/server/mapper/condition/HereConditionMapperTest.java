package com.pdg.adventure.server.mapper.condition;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.condition.HereConditionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.condition.HereCondition;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

/**
 * Comprehensive unit tests for HereConditionMapper covering:
 * 1. Basic mapToBO conversion (HereConditionData → HereCondition)
 * 2. Basic mapToDO conversion (HereCondition → HereConditionData)
 * 3. ID preservation during mapping
 * 4. Item resolution from AdventureConfig
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HereConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private AdventureConfig adventureConfig;

    @Mock
    private GameContext gameContext;

    @Mock
    private Item mockItem;

    private HereConditionMapper mapper;
    private Map<String, Item> allItemsMap;

    @BeforeEach
    void setUp() {
        doNothing().when(mapperSupporter).registerMapper(any(), any(), any());
        mapper = new HereConditionMapper(mapperSupporter, adventureConfig, gameContext);

        // Setup items map for adventureConfig
        allItemsMap = new HashMap<>();
        when(adventureConfig.allItems()).thenReturn(allItemsMap);
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts HereConditionData to HereCondition")
    void mapToBO_shouldConvertHereConditionDataToHereCondition() {
        // Given: HereConditionData with thing ID
        HereConditionData conditionData = new HereConditionData();
        conditionData.setId("here-condition-001");
        conditionData.setThingId("treasure-chest");

        when(mockItem.getId()).thenReturn("treasure-chest");
        allItemsMap.put("treasure-chest", mockItem);

        // When: mapping to business object
        HereCondition result = mapper.mapToBO(conditionData);

        // Then: HereCondition should be created with proper properties
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("here-condition-001");
        assertThat(result.getThing()).isEqualTo(mockItem);
    }

    @Test
    @DisplayName("Test 2: mapToBO - resolves thing from AdventureConfig")
    void mapToBO_shouldResolveThingFromAdventureConfig() {
        // Given: HereConditionData with specific thing ID
        HereConditionData conditionData = new HereConditionData();
        conditionData.setThingId("ancient-statue");

        Item ancientStatue = org.mockito.Mockito.mock(Item.class);
        when(ancientStatue.getId()).thenReturn("ancient-statue");
        allItemsMap.put("ancient-statue", ancientStatue);

        // When: mapping to business object
        HereCondition result = mapper.mapToBO(conditionData);

        // Then: correct thing should be resolved
        assertThat(result.getThing()).isEqualTo(ancientStatue);
        assertThat(result.getThing().getId()).isEqualTo("ancient-statue");
    }

    @Test
    @DisplayName("Test 3: mapToDO - converts HereCondition to HereConditionData")
    void mapToDO_shouldConvertHereConditionToHereConditionData() {
        // Given: HereCondition business object
        when(mockItem.getId()).thenReturn("golden-crown");
        HereCondition condition = new HereCondition(mockItem, gameContext);
        condition.setId("here-condition-002");

        // When: mapping to data object
        HereConditionData result = mapper.mapToDO(condition);

        // Then: HereConditionData should be created with proper properties
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("here-condition-002");
        assertThat(result.getThingId()).isEqualTo("golden-crown");
    }

    @Test
    @DisplayName("Test 4: mapToDO - preserves ID during conversion")
    void mapToDO_shouldPreserveIdDuringConversion() {
        // Given: Multiple HereCondition objects with different IDs
        when(mockItem.getId()).thenReturn("thing-456");

        HereCondition condition1 = new HereCondition(mockItem, gameContext);
        condition1.setId("here-001");

        HereCondition condition2 = new HereCondition(mockItem, gameContext);
        condition2.setId("here-002");

        // When: mapping both conditions
        HereConditionData result1 = mapper.mapToDO(condition1);
        HereConditionData result2 = mapper.mapToDO(condition2);

        // Then: IDs should be preserved and different
        assertThat(result1.getId()).isEqualTo("here-001");
        assertThat(result2.getId()).isEqualTo("here-002");
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Test 5: Round-trip mapping - data → BO → data preserves information")
    void roundTripMapping_shouldPreserveInformation() {
        // Given: Original HereConditionData
        HereConditionData originalData = new HereConditionData();
        originalData.setId("round-trip-here");
        originalData.setThingId("magic-mirror");

        Item magicMirror = org.mockito.Mockito.mock(Item.class);
        when(magicMirror.getId()).thenReturn("magic-mirror");
        allItemsMap.put("magic-mirror", magicMirror);

        // When: mapping to BO and back to DO
        HereCondition businessObject = mapper.mapToBO(originalData);
        HereConditionData roundTripData = mapper.mapToDO(businessObject);

        // Then: data should be preserved
        assertThat(roundTripData.getId()).isEqualTo(originalData.getId());
        assertThat(roundTripData.getThingId()).isEqualTo(originalData.getThingId());
    }
}
