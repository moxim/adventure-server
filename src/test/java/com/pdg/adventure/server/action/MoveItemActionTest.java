package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.parser.CommandExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.tangible.Item;

@ExtendWith(MockitoExtension.class)
class MoveItemActionTest {

    @Mock
    private Item item;

    @Mock
    private Container sourceContainer;

    @Mock
    private Container destinationContainer;

    @Mock
    private MessagesHolder messagesHolder;

    private MoveItemAction moveItemAction;

    @Test
    void execute_movesItemBetweenContainers() {
        // Given
        when(messagesHolder.getMessage("-9")).thenReturn("You move %s to %s.");
        moveItemAction = new MoveItemAction(item, destinationContainer, messagesHolder);

        when(item.getParentContainer()).thenReturn(sourceContainer);
        when(item.getEnrichedShortDescription()).thenReturn("a golden key");
        when(destinationContainer.getSize()).thenReturn(5);
        when(destinationContainer.getMaxSize()).thenReturn(10);
        when(destinationContainer.getEnrichedBasicDescription()).thenReturn("your pocket");

        ExecutionResult successResult = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        when(sourceContainer.remove(item)).thenReturn(successResult);
        when(destinationContainer.add(item)).thenReturn(successResult);

        // When
        ExecutionResult result = moveItemAction.execute();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).contains("golden key");
        verify(sourceContainer).remove(item);
        verify(destinationContainer).add(item);
    }

    @Test
    void execute_returnsFailureWhenContainerFull() {
        // Given
        when(messagesHolder.getMessage("-8")).thenReturn("%s is full.");
        moveItemAction = new MoveItemAction(item, destinationContainer, messagesHolder);

        when(destinationContainer.getSize()).thenReturn(10);
        when(destinationContainer.getMaxSize()).thenReturn(10);
        when(destinationContainer.getShortDescription()).thenReturn("the chest");

        // When
        ExecutionResult result = moveItemAction.execute();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.FAILURE);
        assertThat(result.getResultMessage()).contains("full");
        verify(sourceContainer, never()).remove(any());
        verify(destinationContainer, never()).add(any());
    }

    @Test
    void execute_handlesNullParentContainerGracefully() {
        // Given
        when(messagesHolder.getMessage("-9")).thenReturn("You move %s to %s.");
        moveItemAction = new MoveItemAction(item, destinationContainer, messagesHolder);

        when(item.getParentContainer()).thenReturn(null);
        when(item.getEnrichedShortDescription()).thenReturn("a magic wand");
        when(destinationContainer.getSize()).thenReturn(3);
        when(destinationContainer.getMaxSize()).thenReturn(10);
        when(destinationContainer.getEnrichedBasicDescription()).thenReturn("your backpack");

        ExecutionResult successResult = new CommandExecutionResult(ExecutionResult.State.SUCCESS);
        when(destinationContainer.add(item)).thenReturn(successResult);

        // When
        ExecutionResult result = moveItemAction.execute();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).contains("magic wand");
        verify(destinationContainer).add(item);
    }
}
