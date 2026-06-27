package com.pdg.adventure.server.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.server.storage.repository.ItemRepository;
import com.pdg.adventure.server.storage.service.ItemService;

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
        when(itemRepository.findByAdventureIdAndId(adventureId, itemId)).thenReturn(Optional.of(itemData));

        Optional<ItemData> result = itemService.getItemById(adventureId, itemId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(itemId);
        verify(itemRepository).findByAdventureIdAndId(adventureId, itemId);
    }

    @Test
    void getItemById_notFound_returnsEmpty() {
        when(itemRepository.findByAdventureIdAndId(adventureId, itemId)).thenReturn(Optional.empty());

        assertThat(itemService.getItemById(adventureId, itemId)).isEmpty();
    }

    @Test
    void createItem_withExistingId_preservesExistingId() {
        itemData.setId("existing-id");
        when(itemRepository.save(any(ItemData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemData result = itemService.createItem(adventureId, locationId, itemData);

        assertThat(result.getId()).isEqualTo("existing-id");
    }

    @Test
    void updateItem_notFound_throwsException() {
        when(itemRepository.findByAdventureIdAndId(adventureId, itemId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.updateItem(itemData))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void saveItem_callsRepositoryAndUpdatesTimestamp() {
        when(itemRepository.save(any(ItemData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemData result = itemService.saveItem(itemData);

        assertThat(result.getUpdatedAt()).isNotNull();
        verify(itemRepository).save(itemData);
    }

    @Test
    void deleteItem_delegatesToRepository() {
        itemService.deleteItem(adventureId, itemId);

        verify(itemRepository).deleteByAdventureIdAndId(adventureId, itemId);
    }

    @Test
    void getAllItemsForAdventure_returnsAll() {
        when(itemRepository.findByAdventureId(adventureId)).thenReturn(List.of(itemData));

        assertThat(itemService.getAllItemsForAdventure(adventureId)).hasSize(1);
    }

    @Test
    void getItemsForLocation_returnsFiltered() {
        when(itemRepository.findByAdventureIdAndLocationId(adventureId, locationId)).thenReturn(List.of(itemData));

        assertThat(itemService.getItemsForLocation(adventureId, locationId)).hasSize(1);
    }

    @Test
    void deleteAllItemsForLocation_delegatesToRepository() {
        itemService.deleteAllItemsForLocation(adventureId, locationId);

        verify(itemRepository).deleteByAdventureIdAndLocationId(adventureId, locationId);
    }

    @Test
    void deleteAllItemsForAdventure_delegatesToRepository() {
        itemService.deleteAllItemsForAdventure(adventureId);

        verify(itemRepository).deleteByAdventureId(adventureId);
    }

    @Test
    void countItemsInLocation_returnsCount() {
        when(itemRepository.countByAdventureIdAndLocationId(adventureId, locationId)).thenReturn(4L);

        assertThat(itemService.countItemsInLocation(adventureId, locationId)).isEqualTo(4L);
    }

    @Test
    void countItemsInAdventure_returnsCount() {
        when(itemRepository.countByAdventureId(adventureId)).thenReturn(12L);

        assertThat(itemService.countItemsInAdventure(adventureId)).isEqualTo(12L);
    }
}
