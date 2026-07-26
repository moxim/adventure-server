package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.action.RemoveActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.RemoveAction;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@ExtendWith(MockitoExtension.class)
class RemoveActionMapperTest {

    private static final String THING_ID = "wearable-1";

    @Mock private MapperSupporter mapperSupporter;
    @Mock private AdventureConfig adventureConfig;
    @Mock private MessagesHolder messagesHolder;
    @Mock private Item wearable;

    @InjectMocks private RemoveActionMapper mapper;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(adventureConfig.allMessages()).thenReturn(messagesHolder);
    }

    @Test
    void mapToBO_resolvesWearableById() {
        RemoveActionData data = new RemoveActionData();
        data.setThingId(THING_ID);
        when(mapperSupporter.requireMappedItem(eq(THING_ID), any())).thenReturn(wearable);

        RemoveAction result = mapper.mapToBO(data);

        assertThat(result).isNotNull().isInstanceOf(RemoveAction.class);
        assertThat(result.getThing()).isSameAs(wearable);
    }

    @Test
    void mapToBO_failsFastWhenIdUnknown() {
        RemoveActionData data = new RemoveActionData();
        data.setThingId("missing");
        when(mapperSupporter.requireMappedItem(eq("missing"), any()))
                .thenThrow(new IllegalStateException("Unknown item id 'missing'"));

        assertThatThrownBy(() -> mapper.mapToBO(data))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void mapToDO_roundTripsThingId() {
        when(wearable.getId()).thenReturn(THING_ID);
        RemoveAction action = new RemoveAction(wearable, messagesHolder);

        RemoveActionData data = mapper.mapToDO(action);

        assertThat(data).isNotNull();
        assertThat(data.getThingId()).isEqualTo(THING_ID);
    }
}
