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
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.action.DestroyActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.DestroyAction;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@ExtendWith(MockitoExtension.class)
class DestroyActionMapperTest {

    private static final String THING_ID = "thing-1";

    @Mock private MapperSupporter mapperSupporter;
    @Mock private AdventureConfig adventureConfig;
    @Mock private MessagesHolder messagesHolder;
    @Mock private Item thing;

    @InjectMocks private DestroyActionMapper mapper;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(adventureConfig.allMessages()).thenReturn(messagesHolder);
    }

    @Test
    void mapToBO_resolvesThingById() {
        DestroyActionData data = new DestroyActionData();
        data.setThingId(THING_ID);
        when(adventureConfig.allItems()).thenReturn(Map.of(THING_ID, thing));

        DestroyAction result = mapper.mapToBO(data);

        assertThat(result).isNotNull().isInstanceOf(DestroyAction.class);
        assertThat(result.getThing()).isSameAs(thing);
    }

    @Test
    void mapToBO_returnsActionWithNullThingWhenIdUnknown() {
        DestroyActionData data = new DestroyActionData();
        data.setThingId("missing");
        when(adventureConfig.allItems()).thenReturn(Map.of());

        DestroyAction result = mapper.mapToBO(data);

        assertThat(result).isNotNull();
        assertThat(result.getThing()).isNull();
    }

    @Test
    void mapToDO_roundTripsThingId() {
        when(thing.getId()).thenReturn(THING_ID);
        DestroyAction action = new DestroyAction(thing, messagesHolder);

        DestroyActionData data = mapper.mapToDO(action);

        assertThat(data).isNotNull();
        assertThat(data.getThingId()).isEqualTo(THING_ID);
    }
}
