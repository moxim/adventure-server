package com.pdg.adventure.view.location;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.storage.service.AdventureService;

@ExtendWith(MockitoExtension.class)
class LocationProviderTest {

    @Mock private AdventureService adventureService;

    @Test
    void getLocation_withLocationId_loadsLocationFromService() {
        LocationData expected = new LocationData();
        expected.setId("loc-1");
        when(adventureService.findLocationById("loc-1")).thenReturn(expected);

        LocationData result = LocationProvider.getLocation(adventureService, "loc-1");

        assertThat(result).isSameAs(expected);
    }

    @Test
    void getLocation_withNullLocationId_returnsNewLocationData() {
        LocationData result = LocationProvider.getLocation(adventureService, null);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull().isNotEmpty();
    }
}
