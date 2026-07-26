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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.condition.WornConditionData;
import com.pdg.adventure.server.condition.WornCondition;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WornConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private Item mockItem;

    private WornConditionMapper mapper;
    private Map<String, Item> allItemsMap;

    @BeforeEach
    void setUp() {
        doNothing().when(mapperSupporter).registerMapper(any(), any(), any());
        mapper = new WornConditionMapper(mapperSupporter);

        allItemsMap = new HashMap<>();
        when(mapperSupporter.requireMappedItem(anyString(), any())).thenAnswer(invocation -> {
            Item item = allItemsMap.get(invocation.getArgument(0, String.class));
            if (item == null) {
                throw new IllegalStateException("Unknown item id '%s'".formatted(
                        invocation.getArgument(0, String.class)));
            }
            return item;
        });
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts WornConditionData to WornCondition")
    void mapToBO_shouldConvertWornConditionDataToWornCondition() {
        WornConditionData data = new WornConditionData();
        data.setId("worn-001");
        data.setThingId("magic-ring");

        when(mockItem.getId()).thenReturn("magic-ring");
        allItemsMap.put("magic-ring", mockItem);

        WornCondition result = mapper.mapToBO(data);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("worn-001");
        assertThat(result.getThing()).isEqualTo(mockItem);
    }

    @Test
    @DisplayName("Test 2: mapToBO - resolves item from AdventureConfig")
    void mapToBO_shouldResolveItemFromAdventureConfig() {
        WornConditionData data = new WornConditionData();
        data.setThingId("golden-crown");

        Item goldenCrown = org.mockito.Mockito.mock(Item.class);
        when(goldenCrown.getId()).thenReturn("golden-crown");
        allItemsMap.put("golden-crown", goldenCrown);

        WornCondition result = mapper.mapToBO(data);

        assertThat(result.getThing()).isEqualTo(goldenCrown);
    }

    @Test
    @DisplayName("Test 3: mapToDO - converts WornCondition to WornConditionData")
    void mapToDO_shouldConvertWornConditionToWornConditionData() {
        when(mockItem.getId()).thenReturn("silver-gloves");
        WornCondition condition = new WornCondition(mockItem);
        condition.setId("worn-002");

        WornConditionData result = mapper.mapToDO(condition);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("worn-002");
        assertThat(result.getThingId()).isEqualTo("silver-gloves");
    }

    @Test
    @DisplayName("Test 4: mapToDO - preserves ID during conversion")
    void mapToDO_shouldPreserveIdDuringConversion() {
        when(mockItem.getId()).thenReturn("ring-123");

        WornCondition condition1 = new WornCondition(mockItem);
        condition1.setId("worn-id-001");

        WornCondition condition2 = new WornCondition(mockItem);
        condition2.setId("worn-id-002");

        WornConditionData result1 = mapper.mapToDO(condition1);
        WornConditionData result2 = mapper.mapToDO(condition2);

        assertThat(result1.getId()).isEqualTo("worn-id-001");
        assertThat(result2.getId()).isEqualTo("worn-id-002");
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Test 5: Round-trip mapping - data → BO → data preserves information")
    void roundTripMapping_shouldPreserveInformation() {
        WornConditionData original = new WornConditionData();
        original.setId("round-trip-worn");
        original.setThingId("crystal-bracelet");

        Item crystalBracelet = org.mockito.Mockito.mock(Item.class);
        when(crystalBracelet.getId()).thenReturn("crystal-bracelet");
        allItemsMap.put("crystal-bracelet", crystalBracelet);

        WornCondition bo = mapper.mapToBO(original);
        WornConditionData roundTrip = mapper.mapToDO(bo);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getThingId()).isEqualTo(original.getThingId());
    }
}
