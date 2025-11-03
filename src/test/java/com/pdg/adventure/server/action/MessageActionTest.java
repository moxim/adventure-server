package com.pdg.adventure.server.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.server.storage.message.MessagesHolder;

@ExtendWith(MockitoExtension.class)
class MessageActionTest {

    @Mock
    private MessagesHolder messagesHolder;

    private MessageAction messageAction;
    private String testMessage;

    @BeforeEach
    void setUp() {
        testMessage = "The ancient door creaks open, revealing a dimly lit corridor.";
    }

    @Test
    void execute_returnsSuccessWithMessage() {
        // Given
        messageAction = new MessageAction(testMessage, messagesHolder);

        // When
        ExecutionResult result = messageAction.execute();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEqualTo(testMessage);
    }

    @Test
    void execute_handlesEmptyMessage() {
        // Given
        String emptyMessage = "";
        messageAction = new MessageAction(emptyMessage, messagesHolder);

        // When
        ExecutionResult result = messageAction.execute();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getExecutionState()).isEqualTo(ExecutionResult.State.SUCCESS);
        assertThat(result.getResultMessage()).isEmpty();
    }

    @Test
    void constructor_setsMessageCorrectly() {
        // Given/When
        messageAction = new MessageAction(testMessage, messagesHolder);

        // Then
        assertThat(messageAction.getMessage()).isEqualTo(testMessage);
        assertThat(messageAction.getActionName()).isEqualTo("MessageAction");
    }
}
