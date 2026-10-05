package com.pdg.adventure.server.storage.message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import com.pdg.adventure.model.AdventureFont;

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

    @Test
    void getFont_isDefaultForAMessageStoredWithoutOneAndForAnUnknownId() {
        messagesHolder.addMessage("plain", "text");

        assertThat(messagesHolder.getFont("plain")).isEqualTo(AdventureFont.DEFAULT);
        assertThat(messagesHolder.getFont("unknown")).isEqualTo(AdventureFont.DEFAULT);
    }

    @Test
    void addMessage_withFont_storesTextAndFontTogether() {
        messagesHolder.addMessage("note", "Meet me at midnight.", AdventureFont.SPECIAL_ELITE);

        assertThat(messagesHolder.getMessage("note")).isEqualTo("Meet me at midnight.");
        assertThat(messagesHolder.getFont("note")).isEqualTo(AdventureFont.SPECIAL_ELITE);
    }

    @Test
    void aNullFontMeansTheDefaultFont() {
        messagesHolder.addMessage("note", "text", null);

        assertThat(messagesHolder.getFont("note")).isEqualTo(AdventureFont.DEFAULT);
    }

    @Test
    void removeAndClear_dropTheFontWithTheMessage() {
        messagesHolder.addMessage("a", "text", AdventureFont.CINZEL);
        messagesHolder.addMessage("b", "text", AdventureFont.CINZEL);

        messagesHolder.removeMessage("a");
        assertThat(messagesHolder.getFont("a")).isEqualTo(AdventureFont.DEFAULT);

        messagesHolder.clear();
        assertThat(messagesHolder.getFont("b")).isEqualTo(AdventureFont.DEFAULT);
        assertThat(messagesHolder.getMessage("b")).isNull();
    }
}
