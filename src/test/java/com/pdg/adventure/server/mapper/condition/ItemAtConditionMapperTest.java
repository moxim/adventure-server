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

import com.pdg.adventure.model.condition.ItemAtConditionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.condition.ItemAtCondition;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ItemAtConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private AdventureConfig adventureConfig;

    @Mock
    private Item mockItem;

    @Mock
    private Location mockLocation;

    private ItemAtConditionMapper mapper;
    private Map<String, Item> allItemsMap;
    private Map<String, Location> allLocationsMap;

    @BeforeEach
    void setUp() {
        doNothing().when(mapperSupporter).registerMapper(any(), any(), any());
        mapper = new ItemAtConditionMapper(mapperSupporter, adventureConfig);

        allItemsMap = new HashMap<>();
        allLocationsMap = new HashMap<>();
        when(adventureConfig.allItems()).thenReturn(allItemsMap);
        when(adventureConfig.allLocations()).thenReturn(allLocationsMap);
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts ItemAtConditionData to ItemAtCondition")
    void mapToBO_shouldConvertItemAtConditionDataToItemAtCondition() {
        ItemAtConditionData data = new ItemAtConditionData();
        data.setId("itemAt-001");
        data.setThingId("golden-key");
        data.setLocationId("treasure-room");

        when(mockItem.getId()).thenReturn("golden-key");
        when(mockLocation.getId()).thenReturn("treasure-room");
        allItemsMap.put("golden-key", mockItem);
        allLocationsMap.put("treasure-room", mockLocation);

        ItemAtCondition result = mapper.mapToBO(data);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("itemAt-001");
        assertThat(result.getThing()).isEqualTo(mockItem);
        assertThat(result.getLocation()).isEqualTo(mockLocation);
    }

    @Test
    @DisplayName("Test 2: mapToBO - resolves both item and location from AdventureConfig")
    void mapToBO_shouldResolveBothItemAndLocationFromAdventureConfig() {
        ItemAtConditionData data = new ItemAtConditionData();
        data.setThingId("silver-sword");
        data.setLocationId("armoury");

        Item silverSword = org.mockito.Mockito.mock(Item.class);
        Location armoury = org.mockito.Mockito.mock(Location.class);
        allItemsMap.put("silver-sword", silverSword);
        allLocationsMap.put("armoury", armoury);

        ItemAtCondition result = mapper.mapToBO(data);

        assertThat(result.getThing()).isEqualTo(silverSword);
        assertThat(result.getLocation()).isEqualTo(armoury);
    }

    @Test
    @DisplayName("Test 3: mapToDO - converts ItemAtCondition to ItemAtConditionData")
    void mapToDO_shouldConvertItemAtConditionToItemAtConditionData() {
        when(mockItem.getId()).thenReturn("old-lamp");
        when(mockLocation.getId()).thenReturn("cave-entrance");
        ItemAtCondition condition = new ItemAtCondition(mockItem, mockLocation);
        condition.setId("itemAt-002");

        ItemAtConditionData result = mapper.mapToDO(condition);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("itemAt-002");
        assertThat(result.getThingId()).isEqualTo("old-lamp");
        assertThat(result.getLocationId()).isEqualTo("cave-entrance");
    }

    @Test
    @DisplayName("Test 4: mapToDO - preserves ID during conversion")
    void mapToDO_shouldPreserveIdDuringConversion() {
        when(mockItem.getId()).thenReturn("item-123");
        when(mockLocation.getId()).thenReturn("loc-123");

        ItemAtCondition condition1 = new ItemAtCondition(mockItem, mockLocation);
        condition1.setId("itemAt-id-001");

        ItemAtCondition condition2 = new ItemAtCondition(mockItem, mockLocation);
        condition2.setId("itemAt-id-002");

        ItemAtConditionData result1 = mapper.mapToDO(condition1);
        ItemAtConditionData result2 = mapper.mapToDO(condition2);

        assertThat(result1.getId()).isEqualTo("itemAt-id-001");
        assertThat(result2.getId()).isEqualTo("itemAt-id-002");
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Test 5: Round-trip mapping - data → BO → data preserves information")
    void roundTripMapping_shouldPreserveInformation() {
        ItemAtConditionData original = new ItemAtConditionData();
        original.setId("round-trip-itemAt");
        original.setThingId("ruby-gem");
        original.setLocationId("hidden-vault");

        Item rubyGem = org.mockito.Mockito.mock(Item.class);
        Location hiddenVault = org.mockito.Mockito.mock(Location.class);
        when(rubyGem.getId()).thenReturn("ruby-gem");
        when(hiddenVault.getId()).thenReturn("hidden-vault");
        allItemsMap.put("ruby-gem", rubyGem);
        allLocationsMap.put("hidden-vault", hiddenVault);

        ItemAtCondition bo = mapper.mapToBO(original);
        ItemAtConditionData roundTrip = mapper.mapToDO(bo);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getThingId()).isEqualTo(original.getThingId());
        assertThat(roundTrip.getLocationId()).isEqualTo(original.getLocationId());
    }
}
