package com.pdg.adventure.server.action;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;

@ExtendWith(MockitoExtension.class)
class DescribeActionTest {

    @Mock private MessagesHolder messagesHolder;

    @Test
    void execute_returnsDescriptionFromSupplier() {
        String description = "You are in a dimly lit cave. Exits: north.";

        ExecutionResult result = new DescribeAction(() -> description, messagesHolder).execute();

        assertThat(result.getResultMessage()).isEqualTo(description);
    }

    @Test
    void execute_returnsSuccess() {
        ExecutionResult result = new DescribeAction(() -> "A dark room.", messagesHolder).execute();

        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
    }
}
