package com.pdg.adventure.server.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@ExtendWith(MockitoExtension.class)
class ItemMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private DescriptionMapper descriptionMapper;

    @Mock
    private CommandProviderMapper commandProviderMapper;

    @Mock
    private CommandMapper commandMapper;

    @Mock
    private DescriptionProvider descriptionProvider;

    @Mock
    private DescriptionData descriptionData;

    private ItemMapper itemMapper;

    @BeforeEach
    void setUp() {
        itemMapper = new ItemMapper(mapperSupporter, descriptionMapper, commandProviderMapper, commandMapper);
    }

    @Test
    void mapToDO_convertsItemToItemData() {
        // Given
        Item item = new Item(descriptionProvider, true);
        item.setId("golden-sword");
        item.setIsWearable(false);
        item.setIsWorn(false);

        when(descriptionMapper.mapToDO(descriptionProvider)).thenReturn(descriptionData);

        // When
        ItemData result = itemMapper.mapToDO(item);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("golden-sword");
        assertThat(result.isContainable()).isTrue();
        assertThat(result.isWearable()).isFalse();
        assertThat(result.isWorn()).isFalse();
        assertThat(result.getDescriptionData()).isEqualTo(descriptionData);
    }

    @Test
    void mapToBO_convertsItemDataToItem() {
        // Given
        ItemData itemData = new ItemData();
        itemData.setId("magic-ring");
        itemData.setContainable(true);
        itemData.setWearable(true);
        itemData.setWorn(false);
        itemData.setDescriptionData(descriptionData);

        when(descriptionMapper.mapToBO(descriptionData)).thenReturn(descriptionProvider);

        // When
        Item result = itemMapper.mapToBO(itemData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("magic-ring");
        assertThat(result.isContainable()).isTrue();
        assertThat(result.isWearable()).isTrue();
        assertThat(result.isWorn()).isFalse();
    }

    @Test
    void mapToBO_preservesWearableAndContainableFlags() {
        // Given
        ItemData itemData = new ItemData();
        itemData.setId("leather-boots");
        itemData.setContainable(true);
        itemData.setWearable(true);
        itemData.setWorn(true);
        itemData.setDescriptionData(descriptionData);

        when(descriptionMapper.mapToBO(descriptionData)).thenReturn(descriptionProvider);

        // When
        Item result = itemMapper.mapToBO(itemData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("leather-boots");
        assertThat(result.isContainable()).isTrue();
        assertThat(result.isWearable()).isTrue();
        assertThat(result.isWorn()).isTrue();
    }
}
