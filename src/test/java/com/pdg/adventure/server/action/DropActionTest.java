package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.ContainerSupplier;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.tangible.Item;

@ExtendWith(MockitoExtension.class)
class DropActionTest {

    @Mock private Item item;
    @Mock private Container locationContainer;
    @Mock private MessagesHolder messagesHolder;

    @Test
    void execute_delegatesToMoveItemAction_movingItemToLocation() {
        when(messagesHolder.getMessage("-9")).thenReturn("You drop %s.");
        when(item.getParentContainer()).thenReturn(mock(Container.class));
        when(item.getEnrichedShortDescription()).thenReturn("the torch");
        when(locationContainer.getSize()).thenReturn(0);
        when(locationContainer.getMaxSize()).thenReturn(9999);
        when(locationContainer.getEnrichedBasicDescription()).thenReturn("the cave");
        when(item.getParentContainer().remove(item)).thenReturn(new CommandExecutionResult(ExecutionResult.State.SUCCESS));
        when(locationContainer.add(item)).thenReturn(new CommandExecutionResult(ExecutionResult.State.SUCCESS));

        ExecutionResult result = new DropAction(item, new ContainerSupplier(locationContainer), messagesHolder).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        verify(locationContainer).add(item);
    }
}
