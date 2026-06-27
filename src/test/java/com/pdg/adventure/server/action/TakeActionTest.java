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
class TakeActionTest {

    @Mock private Item item;
    @Mock private Container pocket;
    @Mock private MessagesHolder messagesHolder;

    @Test
    void execute_delegatesToMoveItemAction_movingItemToPocket() {
        when(messagesHolder.getMessage("-9")).thenReturn("You take %s.");
        when(item.getParentContainer()).thenReturn(mock(Container.class));
        when(item.getEnrichedShortDescription()).thenReturn("the key");
        when(pocket.getSize()).thenReturn(0);
        when(pocket.getMaxSize()).thenReturn(10);
        when(pocket.getEnrichedBasicDescription()).thenReturn("your pocket");
        when(item.getParentContainer().remove(item)).thenReturn(new CommandExecutionResult(ExecutionResult.State.SUCCESS));
        when(pocket.add(item)).thenReturn(new CommandExecutionResult(ExecutionResult.State.SUCCESS));

        ExecutionResult result = new TakeAction(item, new ContainerSupplier(pocket), messagesHolder).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        verify(pocket).add(item);
    }
}
