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

import com.pdg.adventure.model.condition.PlayerAtConditionData;
import com.pdg.adventure.server.condition.PlayerAtCondition;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.support.MapperSupporter;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlayerAtConditionMapperTest {

    @Mock
    private MapperSupporter mapperSupporter;

    @Mock
    private GameContext gameContext;

    @Mock
    private Location mockLocation;

    private PlayerAtConditionMapper mapper;
    private Map<String, Location> allLocationsMap;

    @BeforeEach
    void setUp() {
        doNothing().when(mapperSupporter).registerMapper(any(), any(), any());
        mapper = new PlayerAtConditionMapper(mapperSupporter, gameContext);

        allLocationsMap = new HashMap<>();
        when(mapperSupporter.requireMappedLocation(anyString(), any())).thenAnswer(invocation -> {
            Location location = allLocationsMap.get(invocation.getArgument(0, String.class));
            if (location == null) {
                throw new IllegalStateException("Unknown location id '%s'".formatted(
                        invocation.getArgument(0, String.class)));
            }
            return location;
        });
    }

    @Test
    @DisplayName("Test 1: mapToBO - converts PlayerAtConditionData to PlayerAtCondition")
    void mapToBO_shouldConvertPlayerAtConditionDataToPlayerAtCondition() {
        PlayerAtConditionData data = new PlayerAtConditionData();
        data.setId("playerAt-001");
        data.setLocationId("forest-clearing");

        when(mockLocation.getId()).thenReturn("forest-clearing");
        allLocationsMap.put("forest-clearing", mockLocation);

        PlayerAtCondition result = mapper.mapToBO(data);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("playerAt-001");
        assertThat(result.getLocation()).isEqualTo(mockLocation);
    }

    @Test
    @DisplayName("Test 2: mapToBO - resolves location from AdventureConfig")
    void mapToBO_shouldResolveLocationFromAdventureConfig() {
        PlayerAtConditionData data = new PlayerAtConditionData();
        data.setLocationId("dark-cave");

        Location darkCave = org.mockito.Mockito.mock(Location.class);
        when(darkCave.getId()).thenReturn("dark-cave");
        allLocationsMap.put("dark-cave", darkCave);

        PlayerAtCondition result = mapper.mapToBO(data);

        assertThat(result.getLocation()).isEqualTo(darkCave);
    }

    @Test
    @DisplayName("Test 3: mapToDO - converts PlayerAtCondition to PlayerAtConditionData")
    void mapToDO_shouldConvertPlayerAtConditionToPlayerAtConditionData() {
        when(mockLocation.getId()).thenReturn("town-square");
        PlayerAtCondition condition = new PlayerAtCondition(mockLocation, gameContext);
        condition.setId("playerAt-002");

        PlayerAtConditionData result = mapper.mapToDO(condition);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("playerAt-002");
        assertThat(result.getLocationId()).isEqualTo("town-square");
    }

    @Test
    @DisplayName("Test 4: mapToDO - preserves ID during conversion")
    void mapToDO_shouldPreserveIdDuringConversion() {
        when(mockLocation.getId()).thenReturn("loc-123");

        PlayerAtCondition condition1 = new PlayerAtCondition(mockLocation, gameContext);
        condition1.setId("playerAt-id-001");

        PlayerAtCondition condition2 = new PlayerAtCondition(mockLocation, gameContext);
        condition2.setId("playerAt-id-002");

        PlayerAtConditionData result1 = mapper.mapToDO(condition1);
        PlayerAtConditionData result2 = mapper.mapToDO(condition2);

        assertThat(result1.getId()).isEqualTo("playerAt-id-001");
        assertThat(result2.getId()).isEqualTo("playerAt-id-002");
        assertThat(result1.getId()).isNotEqualTo(result2.getId());
    }

    @Test
    @DisplayName("Test 5: Round-trip mapping - data → BO → data preserves information")
    void roundTripMapping_shouldPreserveInformation() {
        PlayerAtConditionData original = new PlayerAtConditionData();
        original.setId("round-trip-playerAt");
        original.setLocationId("ancient-ruins");

        Location ancientRuins = org.mockito.Mockito.mock(Location.class);
        when(ancientRuins.getId()).thenReturn("ancient-ruins");
        allLocationsMap.put("ancient-ruins", ancientRuins);

        PlayerAtCondition bo = mapper.mapToBO(original);
        PlayerAtConditionData roundTrip = mapper.mapToDO(bo);

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getLocationId()).isEqualTo(original.getLocationId());
    }
}
