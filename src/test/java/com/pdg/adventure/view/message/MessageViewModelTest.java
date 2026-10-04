package com.pdg.adventure.view.message;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.MessageData;

class MessageViewModelTest {

    @Test
    void constructor_default_shouldCreateNewMessage() {
        // When
        MessageViewModel viewModel = new MessageViewModel();

        // Then
        assertThat(viewModel.getId()).isEmpty();
        assertThat(viewModel.getSummary()).isEmpty();
        assertThat(viewModel.getMessageText()).isEmpty();
        assertThat(viewModel.isNew()).isTrue();
        assertThat(viewModel.getUsageCount()).isZero();
    }

    @Test
    void constructor_withIdSummaryAndText_shouldCreateExistingMessage() {
        // Given
        String messageId = "01abc";
        String summary = "Welcome on arrival";
        String messageText = "Welcome to the adventure!";

        // When
        MessageViewModel viewModel = new MessageViewModel(messageId, summary, messageText);

        // Then
        assertThat(viewModel.getId()).isEqualTo(messageId);
        assertThat(viewModel.getSummary()).isEqualTo(summary);
        assertThat(viewModel.getMessageText()).isEqualTo(messageText);
        assertThat(viewModel.isNew()).isFalse();
        assertThat(viewModel.getUsageCount()).isZero();
    }

    @Test
    void constructor_withIdSummaryTextAndUsage_shouldSetAllFields() {
        // Given
        String messageId = "01def";
        String summary = "Locked door";
        String messageText = "The door is locked.";
        int usageCount = 5;

        // When
        MessageViewModel viewModel = new MessageViewModel(messageId, summary, messageText, usageCount);

        // Then
        assertThat(viewModel.getId()).isEqualTo(messageId);
        assertThat(viewModel.getSummary()).isEqualTo(summary);
        assertThat(viewModel.getMessageText()).isEqualTo(messageText);
        assertThat(viewModel.isNew()).isFalse();
        assertThat(viewModel.getUsageCount()).isEqualTo(usageCount);
    }

    @Test
    void constructor_fromMessageData_shouldMapAllFields() {
        // Given
        MessageData messageData = new MessageData("Test summary", "Test message");

        // When
        MessageViewModel viewModel = new MessageViewModel(messageData);

        // Then
        assertThat(viewModel.getId()).isEqualTo(messageData.getId());
        assertThat(viewModel.getSummary()).isEqualTo("Test summary");
        assertThat(viewModel.getMessageText()).isEqualTo("Test message");
        assertThat(viewModel.isNew()).isFalse();
        assertThat(viewModel.getUsageCount()).isZero();
    }

    @Test
    void constructor_fromMessageDataWithUsage_shouldMapAllFieldsIncludingUsage() {
        // Given
        MessageData messageData = new MessageData("Test summary", "Test message");
        int usageCount = 10;

        // When
        MessageViewModel viewModel = new MessageViewModel(messageData, usageCount);

        // Then
        assertThat(viewModel.getId()).isEqualTo(messageData.getId());
        assertThat(viewModel.getSummary()).isEqualTo("Test summary");
        assertThat(viewModel.getMessageText()).isEqualTo("Test message");
        assertThat(viewModel.getUsageCount()).isEqualTo(usageCount);
    }

    @Test
    void constructor_withNullMessageText_shouldHandleGracefully() {
        // Given
        String messageId = "null_msg";

        // When
        MessageViewModel viewModel = new MessageViewModel(messageId, null, null);

        // Then
        assertThat(viewModel.getId()).isEqualTo(messageId);
        assertThat(viewModel.getSummary()).isEmpty();
        assertThat(viewModel.getMessageText()).isEmpty();
    }

    @Test
    void getPreview_shouldReturnFullText_whenShorterThanMaxLength() {
        // Given
        MessageViewModel viewModel = new MessageViewModel("id", "summary", "Short text");

        // When
        String preview = viewModel.getPreview(50);

        // Then
        assertThat(preview).isEqualTo("Short text");
    }

    @Test
    void getPreview_shouldReturnTruncatedText_whenLongerThanMaxLength() {
        // Given
        MessageViewModel viewModel = new MessageViewModel("id", "summary", "This is a very long message that should be truncated");

        // When
        String preview = viewModel.getPreview(20);

        // Then
        assertThat(preview).hasSize(21). // 20 chars + "…"
                                                 startsWith("This is a very long").
                                         endsWith("…");
    }

    @Test
    void getPreview_shouldReturnEmptyMessageText_whenMessageIsEmpty() {
        // Given
        MessageViewModel viewModel = new MessageViewModel("id", "summary", "");

        // When
        String preview = viewModel.getPreview(50);

        // Then
        assertThat(preview).isEqualTo("(empty message)");
    }

    @Test
    void getPreview_shouldReturnEmptyMessageText_whenMessageIsNull() {
        // Given
        MessageViewModel viewModel = new MessageViewModel();
        viewModel.setMessageText(null);

        // When
        String preview = viewModel.getPreview(50);

        // Then
        assertThat(preview).isEqualTo("(empty message)");
    }
}
