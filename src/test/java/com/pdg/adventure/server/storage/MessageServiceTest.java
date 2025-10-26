package com.pdg.adventure.server.storage;

import com.pdg.adventure.model.MessageData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessageService messageService;

    private String adventureId;
    private String messageId;
    private String messageText;

    @BeforeEach
    void setUp() {
        adventureId = "test-adventure-123";
        messageId = "welcome_message";
        messageText = "Welcome to the adventure!";
    }

    @Test
    void createMessage_shouldCreateNewMessage_whenMessageDoesNotExist() {
        // Given
        when(messageRepository.existsByAdventureIdAndMessageId(adventureId, messageId)).thenReturn(false);
        when(messageRepository.save(any(MessageData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MessageData result = messageService.createMessage(adventureId, messageId, messageText);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAdventureId()).isEqualTo(adventureId);
        assertThat(result.getMessageId()).isEqualTo(messageId);
        assertThat(result.getText()).isEqualTo(messageText);
        assertThat(result.getCreatedDate()).isNotNull();
        assertThat(result.getModifiedDate()).isNotNull();

        verify(messageRepository).existsByAdventureIdAndMessageId(adventureId, messageId);
        verify(messageRepository).save(any(MessageData.class));
    }

    @Test
    void createMessage_shouldThrowException_whenMessageAlreadyExists() {
        // Given
        when(messageRepository.existsByAdventureIdAndMessageId(adventureId, messageId)).thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> messageService.createMessage(adventureId, messageId, messageText))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(messageRepository).existsByAdventureIdAndMessageId(adventureId, messageId);
        verify(messageRepository, never()).save(any());
    }

    @Test
    void updateMessage_shouldUpdateExistingMessage_whenSameMessageId() {
        // Given
        MessageData existingMessage = new MessageData(adventureId, messageId, "Old text");
        existingMessage.setId("mongo-id-123");
        String newText = "New updated text";

        when(messageRepository.findByAdventureIdAndMessageId(adventureId, messageId))
                .thenReturn(Optional.of(existingMessage));
        when(messageRepository.save(any(MessageData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MessageData result = messageService.updateMessage(adventureId, messageId, messageId, newText);

        // Then
        assertThat(result.getText()).isEqualTo(newText);
        assertThat(result.getMessageId()).isEqualTo(messageId);

        ArgumentCaptor<MessageData> captor = ArgumentCaptor.forClass(MessageData.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().getText()).isEqualTo(newText);
    }

    @Test
    void updateMessage_shouldChangeMessageId_whenNewIdDoesNotExist() {
        // Given
        MessageData existingMessage = new MessageData(adventureId, messageId, messageText);
        String newMessageId = "updated_message";
        String newText = "Updated text";

        when(messageRepository.findByAdventureIdAndMessageId(adventureId, messageId))
                .thenReturn(Optional.of(existingMessage));
        when(messageRepository.existsByAdventureIdAndMessageId(adventureId, newMessageId))
                .thenReturn(false);
        when(messageRepository.save(any(MessageData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MessageData result = messageService.updateMessage(adventureId, messageId, newMessageId, newText);

        // Then
        assertThat(result.getMessageId()).isEqualTo(newMessageId);
        assertThat(result.getText()).isEqualTo(newText);
    }

    @Test
    void updateMessage_shouldThrowException_whenMessageNotFound() {
        // Given
        when(messageRepository.findByAdventureIdAndMessageId(adventureId, messageId))
                .thenReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> messageService.updateMessage(adventureId, messageId, messageId, "new text"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");

        verify(messageRepository, never()).save(any());
    }

    @Test
    void updateMessage_shouldThrowException_whenNewIdAlreadyExists() {
        // Given
        MessageData existingMessage = new MessageData(adventureId, messageId, messageText);
        String newMessageId = "conflicting_id";

        when(messageRepository.findByAdventureIdAndMessageId(adventureId, messageId))
                .thenReturn(Optional.of(existingMessage));
        when(messageRepository.existsByAdventureIdAndMessageId(adventureId, newMessageId))
                .thenReturn(true);

        // When / Then
        assertThatThrownBy(() -> messageService.updateMessage(adventureId, messageId, newMessageId, "text"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(messageRepository, never()).save(any());
    }

    @Test
    void deleteMessage_shouldCallRepository() {
        // When
        messageService.deleteMessage(adventureId, messageId);

        // Then
        verify(messageRepository).deleteByAdventureIdAndMessageId(adventureId, messageId);
    }

    @Test
    void getAllMessagesForAdventure_shouldReturnAllMessages() {
        // Given
        List<MessageData> messages = List.of(
                new MessageData(adventureId, "msg1", "Text 1"),
                new MessageData(adventureId, "msg2", "Text 2")
        );
        when(messageRepository.findByAdventureId(adventureId)).thenReturn(messages);

        // When
        List<MessageData> result = messageService.getAllMessagesForAdventure(adventureId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(MessageData::getMessageId)
                .containsExactly("msg1", "msg2");
    }

    @Test
    void getMessageByIdForAdventure_shouldReturnMessage_whenExists() {
        // Given
        MessageData message = new MessageData(adventureId, messageId, messageText);
        when(messageRepository.findByAdventureIdAndMessageId(adventureId, messageId))
                .thenReturn(Optional.of(message));

        // When
        Optional<MessageData> result = messageService.getMessageByIdForAdventure(adventureId, messageId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getMessageId()).isEqualTo(messageId);
    }

    @Test
    void getMessageByIdForAdventure_shouldReturnEmpty_whenNotExists() {
        // Given
        when(messageRepository.findByAdventureIdAndMessageId(adventureId, messageId))
                .thenReturn(Optional.empty());

        // When
        Optional<MessageData> result = messageService.getMessageByIdForAdventure(adventureId, messageId);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void getMessageText_shouldReturnText_whenMessageExists() {
        // Given
        MessageData message = new MessageData(adventureId, messageId, messageText);
        when(messageRepository.findByAdventureIdAndMessageId(adventureId, messageId))
                .thenReturn(Optional.of(message));

        // When
        String result = messageService.getMessageText(adventureId, messageId);

        // Then
        assertThat(result).isEqualTo(messageText);
    }

    @Test
    void getMessageText_shouldReturnNull_whenMessageNotExists() {
        // Given
        when(messageRepository.findByAdventureIdAndMessageId(adventureId, messageId))
                .thenReturn(Optional.empty());

        // When
        String result = messageService.getMessageText(adventureId, messageId);

        // Then
        assertThat(result).isNull();
    }

    @Test
    void messageExists_shouldReturnTrue_whenMessageExists() {
        // Given
        when(messageRepository.existsByAdventureIdAndMessageId(adventureId, messageId)).thenReturn(true);

        // When
        boolean result = messageService.messageExists(adventureId, messageId);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void messageExists_shouldReturnFalse_whenMessageDoesNotExist() {
        // Given
        when(messageRepository.existsByAdventureIdAndMessageId(adventureId, messageId)).thenReturn(false);

        // When
        boolean result = messageService.messageExists(adventureId, messageId);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getMessagesByCategory_shouldReturnFilteredMessages() {
        // Given
        String category = "greetings";
        List<MessageData> messages = List.of(
                new MessageData(adventureId, "msg1", "Hello"),
                new MessageData(adventureId, "msg2", "Welcome")
        );
        when(messageRepository.findByAdventureIdAndCategory(adventureId, category)).thenReturn(messages);

        // When
        List<MessageData> result = messageService.getMessagesByCategory(adventureId, category);

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    void deleteAllMessagesForAdventure_shouldCallRepository() {
        // When
        messageService.deleteAllMessagesForAdventure(adventureId);

        // Then
        verify(messageRepository).deleteByAdventureId(adventureId);
    }

    @Test
    void countMessages_shouldReturnCount() {
        // Given
        when(messageRepository.countByAdventureId(adventureId)).thenReturn(5L);

        // When
        long result = messageService.countMessages(adventureId);

        // Then
        assertThat(result).isEqualTo(5L);
    }

    @Test
    void saveMessage_shouldCallRepositoryAndUpdateTimestamp() {
        // Given
        MessageData message = new MessageData(adventureId, messageId, messageText);
        when(messageRepository.save(any(MessageData.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        MessageData result = messageService.saveMessage(message);

        // Then
        assertThat(result.getModifiedDate()).isNotNull();
        verify(messageRepository).save(message);
    }
}
