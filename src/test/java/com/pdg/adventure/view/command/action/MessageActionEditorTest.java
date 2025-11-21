package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.model.action.MessageActionData;

class MessageActionEditorTest {

    private AdventureData adventureData;
    private MessageActionData messageActionData;
    private MessageData testMessage1;
    private MessageData testMessage2;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");

        // Create test messages
        testMessage1 = new MessageData();
        testMessage1.setMessageId("welcome");
        testMessage1.setText("Welcome to the adventure!");

        testMessage2 = new MessageData();
        testMessage2.setMessageId("goodbye");
        testMessage2.setText("Thanks for playing!");

        Map<String, MessageData> messages = new HashMap<>();
        messages.put(testMessage1.getMessageId(), testMessage1);
        messages.put(testMessage2.getMessageId(), testMessage2);
        adventureData.setMessages(messages);

        messageActionData = new MessageActionData();
    }

    @Test
    void validate_withNoMessageSelected_shouldReturnFalse() {
        // Given
        MessageActionEditor editor = new MessageActionEditor(messageActionData, adventureData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validate_withEmptyMessageId_shouldReturnFalse() {
        // Given
        messageActionData.setMessageId("   ");
        MessageActionEditor editor = new MessageActionEditor(messageActionData, adventureData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validate_withValidMessageId_shouldReturnTrue() {
        // Given
        messageActionData.setMessageId("welcome");
        MessageActionEditor editor = new MessageActionEditor(messageActionData, adventureData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void constructor_shouldSetActionDataAndAdventureData() {
        // When
        MessageActionEditor editor = new MessageActionEditor(messageActionData, adventureData);

        // Then
        assertThat(editor.getActionData()).isSameAs(messageActionData);
    }

    @Test
    void initialize_shouldBuildUI() {
        // Given
        MessageActionEditor editor = new MessageActionEditor(messageActionData, adventureData);

        // When
        editor.initialize();

        // Then
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void initialize_withPreselectedMessageId_shouldDisplayMessage() {
        // Given
        messageActionData.setMessageId("welcome");
        MessageActionEditor editor = new MessageActionEditor(messageActionData, adventureData);

        // When
        editor.initialize();

        // Then
        assertThat(editor.getActionData()).isSameAs(messageActionData);
        assertThat(messageActionData.getMessageId()).isEqualTo("welcome");
    }

    @Test
    void getActionTypeName_shouldReturnMessageActionName() {
        // Given
        MessageActionEditor editor = new MessageActionEditor(messageActionData, adventureData);

        // When
        String actionTypeName = editor.getActionTypeName();

        // Then
        assertThat(actionTypeName).isEqualTo(messageActionData.getActionName());
    }
}
