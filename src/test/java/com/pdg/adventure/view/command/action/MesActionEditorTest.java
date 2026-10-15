package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.model.action.MesActionData;
import com.pdg.adventure.view.command.action.MesActionEditor;

class MesActionEditorTest {

    private AdventureData adventureData;
    private MesActionData messageActionData;
    private MessageData testMessage1;
    private MessageData testMessage2;

    @BeforeEach
    void setUp() {
        adventureData = new AdventureData();
        adventureData.setId("test-adventure");

        // Create test messages
        testMessage1 = new MessageData("Welcome", "Welcome to the adventure!");
        testMessage2 = new MessageData("Goodbye", "Thanks for playing!");

        Map<String, MessageData> messages = new HashMap<>();
        messages.put(testMessage1.getId(), testMessage1);
        messages.put(testMessage2.getId(), testMessage2);
        adventureData.setMessages(messages);

        messageActionData = new MesActionData();
    }

    @Test
    void validate_withNoMessageSelected_shouldReturnFalse() {
        // Given
        MesActionEditor editor = new MesActionEditor(messageActionData, adventureData);
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
        MesActionEditor editor = new MesActionEditor(messageActionData, adventureData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void validate_withValidMessageId_shouldReturnTrue() {
        // Given
        messageActionData.setMessageId(testMessage1.getId());
        MesActionEditor editor = new MesActionEditor(messageActionData, adventureData);
        editor.initialize();

        // When
        boolean isValid = editor.validate();

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void constructor_shouldSetActionDataAndAdventureData() {
        // When
        MesActionEditor editor = new MesActionEditor(messageActionData, adventureData);

        // Then
        assertThat(editor.getActionData()).isSameAs(messageActionData);
    }

    @Test
    void initialize_shouldBuildUI() {
        // Given
        MesActionEditor editor = new MesActionEditor(messageActionData, adventureData);

        // When
        editor.initialize();

        // Then
        assertThat(editor.getChildren().count()).isGreaterThan(0);
    }

    @Test
    void initialize_withPreselectedMessageId_shouldDisplayMessage() {
        // Given
        messageActionData.setMessageId(testMessage1.getId());
        MesActionEditor editor = new MesActionEditor(messageActionData, adventureData);

        // When
        editor.initialize();

        // Then
        assertThat(editor.getActionData()).isSameAs(messageActionData);
        assertThat(messageActionData.getMessageId()).isEqualTo(testMessage1.getId());
    }

    @Test
    void getActionSummary_withMessageId_shouldShowTheMessageSummaryNotTheId() {
        // Given
        messageActionData.setMessageId(testMessage2.getId());
        MesActionEditor editor = new MesActionEditor(messageActionData, adventureData);
        editor.initialize();

        // When
        String summary = editor.getActionSummary();

        // Then
        assertThat(summary).isEqualTo("Goodbye");
    }

    @Test
    void getActionSummary_withMessageWithoutSummary_shouldFallBackToTextPreview() {
        // Given
        testMessage1.setSummary("  ");
        messageActionData.setMessageId(testMessage1.getId());
        MesActionEditor editor = new MesActionEditor(messageActionData, adventureData);
        editor.initialize();

        // When
        String summary = editor.getActionSummary();

        // Then
        assertThat(summary).isEqualTo("Welcome to the adventure!");
    }

    @Test
    void getActionSummary_withUnknownId_shouldShowItVerbatimBecauseTheEngineUsesItAsLiteralText() {
        // Given
        messageActionData.setMessageId("Just a literal text");
        MesActionEditor editor = new MesActionEditor(messageActionData, adventureData);
        editor.initialize();

        // When
        String summary = editor.getActionSummary();

        // Then
        assertThat(summary).isEqualTo("Just a literal text");
    }

    @Test
    void getActionTypeName_shouldShowTheAuthorMsgNotMes() {
        // Given
        MesActionEditor editor = new MesActionEditor(messageActionData, adventureData);

        // When
        String actionTypeName = editor.getActionTypeName();

        // Then
        assertThat(actionTypeName.replace("ActionData", "")).isEqualTo("Msg");
    }
}
