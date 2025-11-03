package com.pdg.adventure.server.storage.message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessagesHolderTest {

    private MessagesHolder messagesHolder;

    @BeforeEach
    void setUp() {
        messagesHolder = new MessagesHolder();
    }

    @Test
    void addMessage_andGetMessage_storesAndRetrievesCorrectly() {
        // Given
        String messageId = "welcome_message";
        String messageText = "Welcome brave adventurer!";

        // When
        messagesHolder.addMessage(messageId, messageText);
        String retrieved = messagesHolder.getMessage(messageId);

        // Then
        assertThat(retrieved).isEqualTo(messageText);
    }

    @Test
    void getMessage_returnsNullForNonExistentMessage() {
        // Given
        String nonExistentMessageId = "non_existent_message";

        // When
        String result = messagesHolder.getMessage(nonExistentMessageId);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void removeMessage_makesMessageUnavailable() {
        // Given
        String messageId = "temp_message";
        String messageText = "This is a temporary message.";
        messagesHolder.addMessage(messageId, messageText);

        // When
        messagesHolder.removeMessage(messageId);
        String result = messagesHolder.getMessage(messageId);

        // Then
        assertThat(result).isNull();
    }
}
