package com.pdg.adventure.server.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.storage.mongo.CascadeDeleteHelper;
import com.pdg.adventure.server.storage.repository.AdventureRepository;
import com.pdg.adventure.server.storage.repository.LocationRepository;
import com.pdg.adventure.server.storage.repository.VocabularyReporitory;
import com.pdg.adventure.server.storage.repository.WordRepository;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.MessageService;
import com.pdg.adventure.server.support.MapperSupporter;

@ExtendWith(MockitoExtension.class)
class AdventureServiceTest {

    @Mock private LocationRepository locationRepository;
    @Mock private AdventureRepository adventureRepository;
    @Mock private WordRepository wordRepository;
    @Mock private VocabularyReporitory vocabularyRepository;
    @Mock private MapperSupporter mapperSupporter;
    @Mock private MessageService messageService;
    @Mock private CascadeDeleteHelper cascadeDeleteHelper;

    private AdventureService adventureService;

    @BeforeEach
    void setUp() {
        adventureService = new AdventureService(locationRepository, adventureRepository, wordRepository,
                vocabularyRepository, mapperSupporter, messageService, cascadeDeleteHelper);
    }

    @Test
    void findLocationById_found_returnsExistingLocation() {
        LocationData location = new LocationData();
        location.setId("loc-1");
        when(locationRepository.findById("loc-1")).thenReturn(Optional.of(location));

        LocationData result = adventureService.findLocationById("loc-1");

        assertThat(result.getId()).isEqualTo("loc-1");
    }

    @Test
    void findLocationById_notFound_returnsNewLocationWithGeneratedId() {
        when(locationRepository.findById("missing")).thenReturn(Optional.empty());

        LocationData result = adventureService.findLocationById("missing");

        assertThat(result.getId()).isNotNull().isNotEmpty();
    }

    @Test
    void saveLocationData_delegatesToRepository() {
        LocationData location = new LocationData();
        location.setId("loc-1");

        adventureService.saveLocationData(location);

        verify(locationRepository).save(location);
    }

    @Test
    void getLocations_returnsAll() {
        List<LocationData> locations = List.of(new LocationData(), new LocationData());
        when(locationRepository.findAll()).thenReturn(locations);

        assertThat(adventureService.getLocations()).hasSize(2);
    }

    @Test
    void getCountOfLocations_returnsSize() {
        when(locationRepository.findAll()).thenReturn(List.of(new LocationData(), new LocationData(), new LocationData()));

        assertThat(adventureService.getCountOfLocations()).isEqualTo(3);
    }

    @Test
    void saveAdventureData_delegatesToRepository() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");

        adventureService.saveAdventureData(adventure);

        verify(adventureRepository).save(adventure);
    }

    @Test
    void findAdventureById_found_returnsAdventure() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        when(adventureRepository.findById("adv-1")).thenReturn(Optional.of(adventure));

        Optional<AdventureData> result = adventureService.findAdventureById("adv-1");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo("adv-1");
    }

    @Test
    void findAdventureById_notFound_returnsEmpty() {
        when(adventureRepository.findById("missing")).thenReturn(Optional.empty());

        assertThat(adventureService.findAdventureById("missing")).isEmpty();
    }

    @Test
    void getAdventures_returnsAll() {
        List<AdventureData> adventures = List.of(new AdventureData(), new AdventureData());
        when(adventureRepository.findAll()).thenReturn(adventures);

        assertThat(adventureService.getAdventures()).hasSize(2);
    }

    @Test
    void getAdventuresByIds_returnsFilteredAdventures() {
        List<String> ids = List.of("adv-1", "adv-2");
        List<AdventureData> adventures = List.of(new AdventureData());
        when(adventureRepository.findAllById(ids)).thenReturn(adventures);

        assertThat(adventureService.getAdventuresByIds(ids)).hasSize(1);
    }

    @Test
    void deleteLocation_delegatesToRepository() {
        adventureService.deleteLocation("loc-1");

        verify(locationRepository).deleteById("loc-1");
    }

    @Test
    void deleteAdventure_found_cascadeDeletesThenDeletesAdventure() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        when(adventureRepository.findById("adv-1")).thenReturn(Optional.of(adventure));

        adventureService.deleteAdventure("adv-1");

        verify(cascadeDeleteHelper).cascadeDelete(adventure);
        verify(adventureRepository).delete(adventure);
    }

    @Test
    void deleteAdventure_notFound_doesNothing() {
        when(adventureRepository.findById("missing")).thenReturn(Optional.empty());

        adventureService.deleteAdventure("missing");

        verify(cascadeDeleteHelper, never()).cascadeDelete(any());
        verify(adventureRepository, never()).delete(any(AdventureData.class));
    }
}
