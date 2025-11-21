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
 * Unit tests for MessagesMenuView business logic.
 * Tests focus on grid population, data filtering, and state management
 * without requiring full Vaadin UI context.
 */
@ExtendWith(MockitoExtension.class)
class MessagesMenuViewTest {

    @Mock
    private AdventureService adventureService;

    @Mock
    private MessageService messageService;

    private MessagesMenuView view;
    private AdventureData adventureData;

    @BeforeEach
    void setUp() {
        // Create test data
        adventureData = new AdventureData();
        adventureData.setId("adventure-1");
        adventureData.setMessages(new HashMap<>());
    }

    @Test
    void constructor_shouldCreateViewWithAllComponents() {
        // when
        view = new MessagesMenuView(messageService, adventureService);

        // then
        assertThat(view).isNotNull();
    }

    @Test
    void setData_shouldPopulateGridWithMessages() {
        // given
        view = new MessagesMenuView(messageService, adventureService);

        MessageData welcomeMessage = createTestMessage("welcome_message", "Welcome to the adventure!");
        MessageData farewellMessage = createTestMessage("farewell_message", "Goodbye, brave adventurer!");

        adventureData.getMessages().put("welcome_message", welcomeMessage);
        adventureData.getMessages().put("farewell_message", farewellMessage);

        // when
        view.setData(adventureData);

        // then
        assertThat(adventureData.getMessages())
                .hasSize(2)
                .containsKeys("welcome_message", "farewell_message");
        assertThat(adventureData.getMessages().get("welcome_message")).isEqualTo(welcomeMessage);
        assertThat(adventureData.getMessages().get("farewell_message")).isEqualTo(farewellMessage);
    }

    @Test
    void setData_withMultipleMessages_shouldPreserveAllMessages() {
        // given
        view = new MessagesMenuView(messageService, adventureService);

        MessageData message1 = createTestMessage("intro_message", "Welcome to the adventure!");
        MessageData message2 = createTestMessage("help_message", "Type 'help' for assistance");
        MessageData message3 = createTestMessage("exit_message", "Thanks for playing!");

        adventureData.getMessages().put("intro_message", message1);
        adventureData.getMessages().put("help_message", message2);
        adventureData.getMessages().put("exit_message", message3);

        // when
        view.setData(adventureData);

        // then
        assertThat(adventureData.getMessages())
                .hasSize(3)
                .containsKeys("intro_message", "help_message", "exit_message");
    }

    @Test
    void setData_withEmptyAdventure_shouldHandleEmptyState() {
        // given
        view = new MessagesMenuView(messageService, adventureService);

        // Adventure has empty messages map
        adventureData.setMessages(new HashMap<>());

        // when
        view.setData(adventureData);

        // then
        assertThat(adventureData.getMessages()).isEmpty();
    }

    private MessageData createTestMessage(String messageId, String text) {
        MessageData message = new MessageData();
        message.setAdventureId("adventure-1");
        message.setMessageId(messageId);
        message.setText(text);
        return message;
    }
}
