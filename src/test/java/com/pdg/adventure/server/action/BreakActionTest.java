package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;

@ExtendWith(MockitoExtension.class)
class BreakActionTest {

    @Mock private MessagesHolder messagesHolder;

    @Test
    void execute_returnsSuccess() {
        ExecutionResult result = new BreakAction(messagesHolder).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }

    @Test
    void isBreak_returnsTrue() {
        assertThat(new BreakAction(messagesHolder).isBreak()).isTrue();
    }
}
