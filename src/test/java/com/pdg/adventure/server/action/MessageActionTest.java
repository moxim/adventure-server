package com.pdg.adventure.server.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.api.ExecutionResult;
import com.pdg.adventure.model.AdventureFont;
import com.pdg.adventure.server.engine.FontMarkup;

@ExtendWith(MockitoExtension.class)
class MessageActionTest {

    private MessageAction messageAction;
    private String testMessage;

    @BeforeEach
    void setUp() {
        testMessage = "The ancient door creaks open, revealing a dimly lit corridor.";
    }

    @Test
    void execute_returnsSuccessWithMessage() {
        // Given
        messageAction = new MessageAction(testMessage);

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
        messageAction = new MessageAction(emptyMessage);

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
        messageAction = new MessageAction(testMessage);

        // Then
        assertThat(messageAction.getMessage()).isEqualTo(testMessage);
        assertThat(messageAction.getActionName()).isEqualTo("MessageAction");
    }

    @Test
    void execute_withAFont_wrapsTheTextSoTheRunViewCanShowItInThatFont() {
        messageAction = new MessageAction(testMessage, AdventureFont.SPECIAL_ELITE);

        ExecutionResult result = messageAction.execute();

        assertThat(result.getResultMessage()).isEqualTo(FontMarkup.wrap(testMessage, AdventureFont.SPECIAL_ELITE));
        assertThat(FontMarkup.split(result.getResultMessage()))
                .containsExactly(new FontMarkup.Segment(testMessage, AdventureFont.SPECIAL_ELITE));
        assertThat(messageAction.getMessage()).as("the plain text stays reachable").isEqualTo(testMessage);
    }

    @Test
    void execute_withTheDefaultFontOrNone_returnsThePlainTextExactlyAsBefore() {
        assertThat(new MessageAction(testMessage, AdventureFont.DEFAULT).execute().getResultMessage())
                .isEqualTo(testMessage);
        assertThat(new MessageAction(testMessage, null).execute().getResultMessage()).isEqualTo(testMessage);
    }
}
