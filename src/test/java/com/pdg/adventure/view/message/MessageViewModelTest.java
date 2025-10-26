package com.pdg.adventure.view.message;

import com.pdg.adventure.model.MessageData;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageViewModelTest {

    @Test
    void constructor_default_shouldCreateNewMessage() {
        // When
        MessageViewModel viewModel = new MessageViewModel();

        // Then
        assertThat(viewModel.getId()).isEmpty();
        assertThat(viewModel.getMessageText()).isEmpty();
        assertThat(viewModel.isNew()).isTrue();
        assertThat(viewModel.getUsageCount()).isZero();
    }

    @Test
    void constructor_withIdAndText_shouldCreateExistingMessage() {
        // Given
        String messageId = "welcome_msg";
        String messageText = "Welcome to the adventure!";

        // When
        MessageViewModel viewModel = new MessageViewModel(messageId, messageText);

        // Then
        assertThat(viewModel.getId()).isEqualTo(messageId);
        assertThat(viewModel.getMessageText()).isEqualTo(messageText);
        assertThat(viewModel.isNew()).isFalse();
        assertThat(viewModel.getUsageCount()).isZero();
    }

    @Test
    void constructor_withIdTextAndUsage_shouldSetAllFields() {
        // Given
        String messageId = "door_locked";
        String messageText = "The door is locked.";
        int usageCount = 5;

        // When
        MessageViewModel viewModel = new MessageViewModel(messageId, messageText, usageCount);

        // Then
        assertThat(viewModel.getId()).isEqualTo(messageId);
        assertThat(viewModel.getMessageText()).isEqualTo(messageText);
        assertThat(viewModel.isNew()).isFalse();
        assertThat(viewModel.getUsageCount()).isEqualTo(usageCount);
    }

    @Test
    void constructor_fromMessageData_shouldMapAllFields() {
        // Given
        MessageData messageData = new MessageData("adventure-123", "test_msg", "Test message");
        messageData.setCategory("greetings");
        messageData.setNotes("Test notes");

        // When
        MessageViewModel viewModel = new MessageViewModel(messageData);

        // Then
        assertThat(viewModel.getId()).isEqualTo("test_msg");
        assertThat(viewModel.getMessageText()).isEqualTo("Test message");
        assertThat(viewModel.isNew()).isFalse();
        assertThat(viewModel.getUsageCount()).isZero();
        assertThat(viewModel.getCategory()).isEqualTo("greetings");
        assertThat(viewModel.getNotes()).isEqualTo("Test notes");
    }

    @Test
    void constructor_fromMessageDataWithUsage_shouldMapAllFieldsIncludingUsage() {
        // Given
        MessageData messageData = new MessageData("adventure-123", "test_msg", "Test message");
        int usageCount = 10;

        // When
        MessageViewModel viewModel = new MessageViewModel(messageData, usageCount);

        // Then
        assertThat(viewModel.getId()).isEqualTo("test_msg");
        assertThat(viewModel.getMessageText()).isEqualTo("Test message");
        assertThat(viewModel.getUsageCount()).isEqualTo(usageCount);
    }

    @Test
    void constructor_withNullMessageText_shouldHandleGracefully() {
        // Given
        String messageId = "null_msg";
        String messageText = null;

        // When
        MessageViewModel viewModel = new MessageViewModel(messageId, messageText);

        // Then
        assertThat(viewModel.getId()).isEqualTo(messageId);
        assertThat(viewModel.getMessageText()).isEmpty();
    }

    @Test
    void getPreview_shouldReturnFullText_whenShorterThanMaxLength() {
        // Given
        MessageViewModel viewModel = new MessageViewModel("id", "Short text");

        // When
        String preview = viewModel.getPreview(50);

        // Then
        assertThat(preview).isEqualTo("Short text");
    }

    @Test
    void getPreview_shouldReturnTruncatedText_whenLongerThanMaxLength() {
        // Given
        MessageViewModel viewModel = new MessageViewModel("id", "This is a very long message that should be truncated");

        // When
        String preview = viewModel.getPreview(20);

        // Then
        assertThat(preview).hasSize(23); // 20 chars + "..."
        assertThat(preview).startsWith("This is a very long");
        assertThat(preview).endsWith("...");
    }

    @Test
    void getPreview_shouldReturnEmptyMessageText_whenMessageIsEmpty() {
        // Given
        MessageViewModel viewModel = new MessageViewModel("id", "");

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

    @Test
    void isValidId_shouldReturnTrue_forValidAlphanumericId() {
        // Given
        MessageViewModel viewModel = new MessageViewModel();
        viewModel.setId("valid_message_123");

        // When
        boolean isValid = viewModel.isValidId();

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void isValidId_shouldReturnTrue_forValidUppercaseId() {
        // Given
        MessageViewModel viewModel = new MessageViewModel();
        viewModel.setId("VALID_MESSAGE");

        // When
        boolean isValid = viewModel.isValidId();

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void isValidId_shouldReturnFalse_forIdWithSpaces() {
        // Given
        MessageViewModel viewModel = new MessageViewModel();
        viewModel.setId("invalid message");

        // When
        boolean isValid = viewModel.isValidId();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void isValidId_shouldReturnFalse_forIdWithSpecialCharacters() {
        // Given
        MessageViewModel viewModel = new MessageViewModel();
        viewModel.setId("invalid-message!");

        // When
        boolean isValid = viewModel.isValidId();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void isValidId_shouldReturnFalse_forEmptyId() {
        // Given
        MessageViewModel viewModel = new MessageViewModel();
        viewModel.setId("");

        // When
        boolean isValid = viewModel.isValidId();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void isValidId_shouldReturnFalse_forNullId() {
        // Given
        MessageViewModel viewModel = new MessageViewModel();
        viewModel.setId(null);

        // When
        boolean isValid = viewModel.isValidId();

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void isValidId_shouldReturnFalse_forWhitespaceOnlyId() {
        // Given
        MessageViewModel viewModel = new MessageViewModel();
        viewModel.setId("   ");

        // When
        boolean isValid = viewModel.isValidId();

        // Then
        assertThat(isValid).isFalse();
    }
}
