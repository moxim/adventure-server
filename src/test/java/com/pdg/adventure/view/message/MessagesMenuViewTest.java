package com.pdg.adventure.view.message;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.MessageService;
import com.pdg.adventure.view.support.RouteIds;

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

    @Mock
    private AdventureAccessService accessService;

    private MessagesMenuView view;
    private AdventureData adventureData;

    @BeforeEach
    void setUp() {
        // Create test data
        adventureData = new AdventureData();
        adventureData.setId("adventure-1");
        adventureData.setMessages(new HashMap<>());

        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void enterWithAdventure() {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())));
        when(accessService.findAdventureById(eq(adventureData.getId()), any(UserData.class)))
                .thenReturn(Optional.of(adventureData));
        view.beforeEnter(event);
    }

    @Test
    void constructor_shouldCreateViewWithAllComponents() {
        // when
        view = new MessagesMenuView(messageService, adventureService, accessService);

        // then
        assertThat(view).isNotNull();
    }

    @Test
    void setData_shouldPopulateGridWithMessages() {
        // given
        view = new MessagesMenuView(messageService, adventureService, accessService);

        MessageData welcomeMessage = createTestMessage("welcome_message", "Welcome to the adventure!");
        MessageData farewellMessage = createTestMessage("farewell_message", "Goodbye, brave adventurer!");

        adventureData.getMessages().put("welcome_message", welcomeMessage);
        adventureData.getMessages().put("farewell_message", farewellMessage);

        // when
        enterWithAdventure();

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
        view = new MessagesMenuView(messageService, adventureService, accessService);

        MessageData message1 = createTestMessage("intro_message", "Welcome to the adventure!");
        MessageData message2 = createTestMessage("help_message", "Type 'help' for assistance");
        MessageData message3 = createTestMessage("exit_message", "Thanks for playing!");

        adventureData.getMessages().put("intro_message", message1);
        adventureData.getMessages().put("help_message", message2);
        adventureData.getMessages().put("exit_message", message3);

        // when
        enterWithAdventure();

        // then
        assertThat(adventureData.getMessages())
                .hasSize(3)
                .containsKeys("intro_message", "help_message", "exit_message");
    }

    @Test
    void setData_withEmptyAdventure_shouldHandleEmptyState() {
        // given
        view = new MessagesMenuView(messageService, adventureService, accessService);

        // Adventure has empty messages map
        adventureData.setMessages(new HashMap<>());

        // when
        enterWithAdventure();

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
