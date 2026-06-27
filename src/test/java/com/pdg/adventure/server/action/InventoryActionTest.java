package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.ContainerSupplier;
import com.pdg.adventure.server.storage.message.MessagesHolder;

@ExtendWith(MockitoExtension.class)
class InventoryActionTest {

    @Mock private Container pocket;
    @Mock private MessagesHolder messagesHolder;

    @Test
    void execute_sendsInventoryHeaderAndContentsToConsumer() {
        when(messagesHolder.getMessage("-10")).thenReturn("You are carrying:");
        when(pocket.listContents()).thenReturn("- a torch\n- a key");

        List<String> captured = new ArrayList<>();
        Consumer<String> consumer = captured::add;

        new InventoryAction(consumer, new ContainerSupplier(pocket), messagesHolder).execute();

        assertThat(captured).containsExactly("You are carrying:", "- a torch\n- a key");
    }

    @Test
    void execute_returnsSuccess() {
        when(messagesHolder.getMessage("-10")).thenReturn("Inventory:");
        when(pocket.listContents()).thenReturn("");

        ExecutionResult result = new InventoryAction(_ -> {}, new ContainerSupplier(pocket), messagesHolder).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }
}
