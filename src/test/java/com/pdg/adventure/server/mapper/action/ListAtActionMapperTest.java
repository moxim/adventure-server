package com.pdg.adventure.server.mapper.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.model.action.ListAtActionData;
import com.pdg.adventure.server.action.ListAtAction;
import com.pdg.adventure.server.support.MapperSupporter;

@ExtendWith(MockitoExtension.class)
class ListAtActionMapperTest {

    @Mock private MapperSupporter mapperSupporter;
    @Mock private Container container;

    @Test
    void mapToBO_resolvesTheContainer() {
        ListAtActionData data = new ListAtActionData();
        data.setContainerId("c1");
        data.setId("id");
        when(mapperSupporter.requireMappedContainer("c1", data)).thenReturn(container);

        ListAtAction result = new ListAtActionMapper(mapperSupporter).mapToBO(data);

        assertThat(result.getContainer()).isSameAs(container);
        assertThat(result.getId()).isEqualTo("id");
    }

    @Test
    void mapToDO_keepsTheContainerId() {
        when(container.getId()).thenReturn("c1");
        ListAtAction action = new ListAtAction(container);

        ListAtActionData data = new ListAtActionMapper(mapperSupporter).mapToDO(action);

        assertThat(data.getContainerId()).isEqualTo("c1");
        assertThat(data.getId()).isEqualTo(action.getId());
    }
}
