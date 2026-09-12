package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.action.LightActionData;
import com.pdg.adventure.server.action.LightAction;
import com.pdg.adventure.server.support.MapperSupporter;
import com.pdg.adventure.server.tangible.Item;

@ExtendWith(MockitoExtension.class)
class LightActionMapperTest {

    private static final String THING_ID = "torch-1";

    @Mock private MapperSupporter mapperSupporter;
    @Mock private Item torch;

    @InjectMocks private LightActionMapper mapper;

    @Test
    void mapToBO_resolvesItemByIdAndCarriesOverTheLumen() {
        LightActionData data = new LightActionData();
        data.setThingId(THING_ID);
        data.setLumen(50);
        when(mapperSupporter.requireMappedItem(eq(THING_ID), any())).thenReturn(torch);

        LightAction result = mapper.mapToBO(data);

        assertThat(result).isNotNull();
        assertThat(result.getItem()).isSameAs(torch);
        assertThat(result.getLumen()).isEqualTo(50);
    }

    @Test
    void mapToBO_failsFastWhenIdUnknown() {
        LightActionData data = new LightActionData();
        data.setThingId("missing");
        when(mapperSupporter.requireMappedItem(eq("missing"), any()))
                .thenThrow(new IllegalStateException("Unknown item id 'missing'"));

        assertThatThrownBy(() -> mapper.mapToBO(data))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("missing");
    }

    @Test
    void mapToDO_roundTripsThingIdAndLumen() {
        when(torch.getId()).thenReturn(THING_ID);
        LightAction action = new LightAction(torch, 75);

        LightActionData data = mapper.mapToDO(action);

        assertThat(data).isNotNull();
        assertThat(data.getThingId()).isEqualTo(THING_ID);
        assertThat(data.getLumen()).isEqualTo(75);
    }
}
