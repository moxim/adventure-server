package com.pdg.adventure.server.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.ItemData;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private String adventureId;
    private String locationId;
    private String itemId;
    private ItemData itemData;

    @BeforeEach
    void setUp() {
        adventureId = "test-adventure-123";
        locationId = "test-location-456";
        itemId = "golden-key";

        itemData = new ItemData();
        itemData.setId(itemId);
        itemData.setAdventureId(adventureId);
        itemData.setLocationId(locationId);
        itemData.setContainable(true);
        itemData.setWearable(false);
    }

    @Test
    void createItem_shouldCreateNewItem() {
        // Given
        ItemData newItem = new ItemData();
        newItem.setContainable(true);
        when(itemRepository.save(any(ItemData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ItemData result = itemService.createItem(adventureId, locationId, newItem);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAdventureId()).isEqualTo(adventureId);
        assertThat(result.getLocationId()).isEqualTo(locationId);
        assertThat(result.getId()).isNotNull().isNotEmpty();
        verify(itemRepository).save(any(ItemData.class));
    }

    @Test
    void updateItem_shouldUpdateExistingItem() {
        // Given
        when(itemRepository.findByAdventureIdAndId(adventureId, itemId)).thenReturn(Optional.of(itemData));
        when(itemRepository.save(any(ItemData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ItemData result = itemService.updateItem(itemData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(itemId);
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(itemRepository).findByAdventureIdAndId(adventureId, itemId);
        verify(itemRepository).save(itemData);
    }

    @Test
    void getItemById_shouldReturnItem_whenExists() {
        // Given
        when(itemRepository.findByAdventureIdAndId(adventureId, itemId)).thenReturn(Optional.of(itemData));

        // When
        Optional<ItemData> result = itemService.getItemById(adventureId, itemId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(itemId);
        assertThat(result.get().getAdventureId()).isEqualTo(adventureId);
        verify(itemRepository).findByAdventureIdAndId(adventureId, itemId);
    }
}
