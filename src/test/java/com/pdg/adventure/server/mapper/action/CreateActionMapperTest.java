package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.action.CreateActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.CreateAction;
import com.pdg.adventure.server.location.Location;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.GenericContainer;
import com.pdg.adventure.server.tangible.Item;

@ExtendWith(MockitoExtension.class)
class CreateActionMapperTest {

    private static final String THING_ID = "thing-1";
    private static final String CONTAINER_ID = "container-1";
    private static final String LOCATION_ID = "location-1";

    @Mock private MapperSupporter mapperSupporter;
    @Mock private AdventureConfig adventureConfig;
    @Mock private MessagesHolder messagesHolder;
    @Mock private Item thing;
    @Mock private GenericContainer container;
    @Mock private Location location;
    @Mock private GenericContainer locationContainer;

    @InjectMocks private CreateActionMapper mapper;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(adventureConfig.allMessages()).thenReturn(messagesHolder);
    }

    @Test
    void mapToBO_resolvesContainerDirectly() {
        CreateActionData data = new CreateActionData();
        data.setThingId(THING_ID);
        data.setContainerProviderId(CONTAINER_ID);
        when(mapperSupporter.requireMappedItem(eq(THING_ID), any())).thenReturn(thing);
        when(adventureConfig.allContainers()).thenReturn(Map.of(CONTAINER_ID, container));

        CreateAction result = mapper.mapToBO(data);

        assertThat(result).isNotNull().isInstanceOf(CreateAction.class);
    }

    @Test
    void mapToBO_fallsBackToLocationItemContainer() {
        CreateActionData data = new CreateActionData();
        data.setThingId(THING_ID);
        data.setContainerProviderId(LOCATION_ID);
        when(mapperSupporter.requireMappedItem(eq(THING_ID), any())).thenReturn(thing);
        when(adventureConfig.allContainers()).thenReturn(Map.of());
        when(adventureConfig.allLocations()).thenReturn(Map.of(LOCATION_ID, location));
        when(location.getItemContainer()).thenReturn(locationContainer);

        CreateAction result = mapper.mapToBO(data);

        assertThat(result).isNotNull();
    }

    @Test
    void mapToBO_throwsWhenContainerProviderUnresolved() {
        CreateActionData data = new CreateActionData();
        data.setThingId(THING_ID);
        data.setContainerProviderId("missing");
        when(mapperSupporter.requireMappedItem(eq(THING_ID), any())).thenReturn(thing);
        when(adventureConfig.allContainers()).thenReturn(Map.of());
        when(adventureConfig.allLocations()).thenReturn(Map.of());

        assertThatThrownBy(() -> mapper.mapToBO(data))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void mapToDO_throwsUnsupportedOperation() {
        assertThatThrownBy(() -> mapper.mapToDO(Mockito.mock(CreateAction.class)))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
