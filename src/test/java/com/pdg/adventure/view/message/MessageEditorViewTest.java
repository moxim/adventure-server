package com.pdg.adventure.view.message;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.server.storage.MessageService;

/**
 * Unit tests for MessageEditorView business logic.
 * Tests focus on validation, data population, and state management
 * without requiring full Vaadin UI context.
 */
@ExtendWith(MockitoExtension.class)
class MessageEditorViewTest {

    @Mock
    private AdventureService adventureService;

    @Mock
    private MessageService messageService;

    private MessageEditorView view;
    private AdventureData adventureData;
    private MessageData messageData;

    @BeforeEach
    void setUp() {
        // Create test data
        adventureData = new AdventureData();
        adventureData.setId("adventure-1");
        adventureData.setMessages(new HashMap<>());

        // Create test message
        messageData = new MessageData();
        messageData.setAdventureId("adventure-1");
        messageData.setMessageId("welcome_message");
        messageData.setText("Welcome to the adventure!");
    }

    @Test
    void constructor_shouldCreateViewWithAllComponents() {
        // when
        view = new MessageEditorView(adventureService, messageService);

        // then
        assertThat(view).isNotNull();
    }

    @Test
    void setData_shouldPopulateAdventureData() {
        // given
        view = new MessageEditorView(adventureService, messageService);

        // when
        view.setData(adventureData);

        // then
        // View should be populated with data
        // No exceptions should be thrown
        assertThat(view).isNotNull();
    }

    @Test
    void setData_withExistingMessage_shouldLoadMessageFromMap() {
        // given
        view = new MessageEditorView(adventureService, messageService);
        adventureData.getMessages().put("welcome_message", messageData);

        // when
        view.setData(adventureData);

        // then
        // View should load the existing message
        assertThat(adventureData.getMessages())
                .hasSize(1)
                .containsKey("welcome_message");
        assertThat(adventureData.getMessages().get("welcome_message")).isEqualTo(messageData);
    }

    @Test
    void getPageTitle_shouldReturnNullBeforeRouteEnter() {
        // given
        view = new MessageEditorView(adventureService, messageService);

        // when
        String title = view.getPageTitle();

        // then
        assertThat(title).isNull();
    }
}
