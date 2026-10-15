package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.engine.ContainerSupplier;
import com.pdg.adventure.server.storage.message.SystemMessageKey;

@ExtendWith(MockitoExtension.class)
class InventoryActionTest {

    @Mock private Container pocket;

    @Test
    void execute_returnsInventoryHeaderAndContentsAsTheResultMessage() {
        when(pocket.listContents()).thenReturn("- a torch\n- a key");

        ExecutionResult result = new InventoryAction(new ContainerSupplier(pocket)).execute();

        assertThat(result.getResultMessage())
                .isEqualTo(SystemMessageKey.SM9.defaultText() + System.lineSeparator() + "- a torch\n- a key");
    }

    @Test
    void execute_returnsSuccess() {
        when(pocket.listContents()).thenReturn("");

        ExecutionResult result = new InventoryAction(new ContainerSupplier(pocket)).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }
}
