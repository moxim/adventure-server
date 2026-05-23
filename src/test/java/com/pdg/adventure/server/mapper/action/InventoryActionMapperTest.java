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

import com.pdg.adventure.model.action.InventoryActionData;
import com.pdg.adventure.server.AdventureConfig;
import com.pdg.adventure.server.action.InventoryAction;
import com.pdg.adventure.server.engine.GameContext;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;

@ExtendWith(MockitoExtension.class)
class InventoryActionMapperTest {

    @Mock private MapperSupporter mapperSupporter;
    @Mock private AdventureConfig adventureConfig;
    @Mock private MessagesHolder messagesHolder;
    @Mock private GameContext gameContext;

    @InjectMocks private InventoryActionMapper mapper;

    @BeforeEach
    void setUp() {
        Mockito.lenient().when(adventureConfig.allMessages()).thenReturn(messagesHolder);
    }

    @Test
    void mapToBO_buildsActionIgnoringDtoFields() {
        InventoryActionData data = new InventoryActionData();
        data.setMessageConsumerId("ignored");
        data.setContainerProviderId("ignored");

        InventoryAction result = mapper.mapToBO(data);

        assertThat(result).isNotNull().isInstanceOf(InventoryAction.class);
    }

    @Test
    void mapToDO_throwsUnsupportedOperation() {
        assertThatThrownBy(() -> mapper.mapToDO(Mockito.mock(InventoryAction.class)))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
