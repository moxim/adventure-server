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

import com.pdg.adventure.model.condition.CarriedConditionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.condition.CarriedCondition;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

/**
 * Comprehensive unit tests for CarriedConditionMapper covering:
 * 1. Basic mapToBO conversion (CarriedConditionData → CarriedCondition)
 * 2. Basic mapToDO conversion (CarriedCondition → CarriedConditionData)
 * 3. ID preservation during mapping
 * 4. Item resolution from AdventureConfig
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CarriedConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private AdventureConfig adventureConfig;

    @Mock
    private GameContext gameContext;

    @Mock
    private Item mockItem;

    private CarriedConditionMapper mapper;
    private Map<String, Item> allItemsMap;

    @BeforeEach
    void setUp() {
        doNothing().when(mapperSupporter).registerMapper(any(), any(), any());
        mapper = new CarriedConditionMapper(mapperSupporter, adventureConfig, gameContext);

        // Setup items map for adventureConfig
        allItemsMap = new HashMap<>();
        when(adventureConfig.allItems()).thenReturn(allItemsMap);
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts CarriedConditionData to CarriedCondition")
    void mapToBO_shouldConvertCarriedConditionDataToCarriedCondition() {
        // Given: CarriedConditionData with item ID
        CarriedConditionData conditionData = new CarriedConditionData();
        conditionData.setId("carried-condition-001");
        conditionData.setItemId("sword-item");

        when(mockItem.getId()).thenReturn("sword-item");
        allItemsMap.put("sword-item", mockItem);

        // When: mapping to business object
        CarriedCondition result = mapper.mapToBO(conditionData);

        // Then: CarriedCondition should be created with proper properties
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("carried-condition-001");
        assertThat(result.getItem()).isEqualTo(mockItem);
    }

    @Test
    @DisplayName("Test 2: mapToBO - resolves item from AdventureConfig")
    void mapToBO_shouldResolveItemFromAdventureConfig() {
        // Given: CarriedConditionData with specific item ID
        CarriedConditionData conditionData = new CarriedConditionData();
        conditionData.setItemId("magic-key");

        Item magicKey = org.mockito.Mockito.mock(Item.class);
        when(magicKey.getId()).thenReturn("magic-key");
        allItemsMap.put("magic-key", magicKey);

        // When: mapping to business object
        CarriedCondition result = mapper.mapToBO(conditionData);

        // Then: correct item should be resolved
        assertThat(result.getItem()).isEqualTo(magicKey);
        assertThat(result.getItem().getId()).isEqualTo("magic-key");
    }

    @Test
    @DisplayName("Test 3: mapToDO - converts CarriedCondition to CarriedConditionData")
    void mapToDO_shouldConvertCarriedConditionToCarriedConditionData() {
        // Given: CarriedCondition business object
        when(mockItem.getId()).thenReturn("torch-item");
        CarriedCondition condition = new CarriedCondition(mockItem, gameContext);
        condition.setId("carried-condition-002");

        // When: mapping to data object
        CarriedConditionData result = mapper.mapToDO(condition);

        // Then: CarriedConditionData should be created with proper properties
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("carried-condition-002");
        assertThat(result.getItemId()).isEqualTo("torch-item");
    }

    @Test
    @DisplayName("Test 4: mapToDO - preserves ID during conversion")
    void mapToDO_shouldPreserveIdDuringConversion() {
        // Given: Multiple CarriedCondition objects with different IDs
        when(mockItem.getId()).thenReturn("item-123");

        CarriedCondition condition1 = new CarriedCondition(mockItem, gameContext);
        condition1.setId("condition-001");

        CarriedCondition condition2 = new CarriedCondition(mockItem, gameContext);
        condition2.setId("condition-002");

        // When: mapping both conditions
        CarriedConditionData result1 = mapper.mapToDO(condition1);
        CarriedConditionData result2 = mapper.mapToDO(condition2);

        // Then: IDs should be preserved and different
        assertThat(result1.getId()).isEqualTo("condition-001");
        assertThat(result2.getId()).isEqualTo("condition-002");
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Test 5: Round-trip mapping - data → BO → data preserves information")
    void roundTripMapping_shouldPreserveInformation() {
        // Given: Original CarriedConditionData
        CarriedConditionData originalData = new CarriedConditionData();
        originalData.setId("round-trip-condition");
        originalData.setItemId("crystal-orb");

        Item crystalOrb = org.mockito.Mockito.mock(Item.class);
        when(crystalOrb.getId()).thenReturn("crystal-orb");
        allItemsMap.put("crystal-orb", crystalOrb);

        // When: mapping to BO and back to DO
        CarriedCondition businessObject = mapper.mapToBO(originalData);
        CarriedConditionData roundTripData = mapper.mapToDO(businessObject);

        // Then: data should be preserved
        assertThat(roundTripData.getId()).isEqualTo(originalData.getId());
        assertThat(roundTripData.getItemId()).isEqualTo(originalData.getItemId());
    }
}
