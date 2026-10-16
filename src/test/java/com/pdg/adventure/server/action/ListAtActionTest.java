package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.api.Container;
import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.storage.message.SystemMessageKey;

class ListAtActionTest {

    @Test
    void execute_printsTheContainersItems_withoutAHeading() {
        Container container = mock(Container.class);
        when(container.isEmpty()).thenReturn(false);
        when(container.listContents()).thenReturn("a lamp, a key.");

        ExecutionResult result = new ListAtAction(container).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEqualTo("a lamp, a key.");
    }

    @Test
    void execute_withoutItems_printsSystemMessage53_inTheAdventuresWording() {
        Container container = mock(Container.class);
        when(container.isEmpty()).thenReturn(true);
        ListAtAction action = new ListAtAction(container);

        assertThat(action.execute().getResultMessage()).isEqualTo("nothing.");
        try (SystemMessageKey.Binding ignored = SystemMessageKey.bindOverrides(Map.of("53", "Not a thing."))) {
            assertThat(action.execute().getResultMessage()).isEqualTo("Not a thing.");
        }
    }

    @Test
    void execute_listsTheContentsAsTheyAreNow() {
        Container container = mock(Container.class);
        when(container.isEmpty()).thenReturn(true, false);
        when(container.listContents()).thenReturn("a coin.");
        ListAtAction action = new ListAtAction(container);

        assertThat(action.execute().getResultMessage()).isEqualTo("nothing.");
        assertThat(action.execute().getResultMessage()).isEqualTo("a coin.");
    }
}
