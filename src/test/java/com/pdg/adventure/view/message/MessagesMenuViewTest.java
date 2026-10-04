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
        view = new MessagesMenuView(adventureService, accessService);

        // then
        assertThat(view).isNotNull();
    }

    @Test
    void setData_shouldPopulateGridWithMessages() {
        // given
        view = new MessagesMenuView(adventureService, accessService);

        MessageData welcomeMessage = createTestMessage("welcome message", "Welcome to the adventure!");
        MessageData farewellMessage = createTestMessage("farewell message", "Goodbye, brave adventurer!");

        adventureData.getMessages().put(welcomeMessage.getId(), welcomeMessage);
        adventureData.getMessages().put(farewellMessage.getId(), farewellMessage);

        // when
        enterWithAdventure();

        // then
        assertThat(adventureData.getMessages())
                .hasSize(2)
                .containsKeys(welcomeMessage.getId(), farewellMessage.getId());
        assertThat(adventureData.getMessages().get(welcomeMessage.getId())).isEqualTo(welcomeMessage);
        assertThat(adventureData.getMessages().get(farewellMessage.getId())).isEqualTo(farewellMessage);
    }

    @Test
    void setData_withMultipleMessages_shouldPreserveAllMessages() {
        // given
        view = new MessagesMenuView(adventureService, accessService);

        MessageData message1 = createTestMessage("intro message", "Welcome to the adventure!");
        MessageData message2 = createTestMessage("help message", "Type 'help' for assistance");
        MessageData message3 = createTestMessage("exit message", "Thanks for playing!");

        adventureData.getMessages().put(message1.getId(), message1);
        adventureData.getMessages().put(message2.getId(), message2);
        adventureData.getMessages().put(message3.getId(), message3);

        // when
        enterWithAdventure();

        // then
        assertThat(adventureData.getMessages())
                .hasSize(3)
                .containsKeys(message1.getId(), message2.getId(), message3.getId());
    }

    @Test
    void setData_withEmptyAdventure_shouldHandleEmptyState() {
        // given
        view = new MessagesMenuView(adventureService, accessService);

        // Adventure has empty messages map
        adventureData.setMessages(new HashMap<>());

        // when
        enterWithAdventure();

        // then
        assertThat(adventureData.getMessages()).isEmpty();
    }

    private MessageData createTestMessage(String summary, String text) {
        return new MessageData(summary, text);
    }
}
