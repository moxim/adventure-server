package com.pdg.adventure.view.message;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.MessageService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.support.RouteIds;

class MessageEditorViewRoutingTest extends BrowserlessTest {

    private AdventureService adventureService;
    private MessageService messageService;
    private AdventureAccessService accessService;
    private MessageEditorView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        messageService = mock(MessageService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new MessageEditorView(adventureService, messageService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static BeforeEnterEvent eventWithParams(RouteParam... params) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(params));
        return event;
    }

    @Test
    void beforeEnter_validIds_populatesMessageTextFromResolvedAdventure() {
        MessageData message = new MessageData();
        message.setText("Welcome!");
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setMessages(Map.of("msg-1", message));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.MESSAGE_ID.getValue(), "msg-1")));

        TextArea messageText = find(TextArea.class, view).single();
        assertThat(messageText.getValue()).isEqualTo("Welcome!");
    }

    @Test
    void beforeEnter_percentEncodedMessageId_decodesAndPopulates() {
        // Simulates cold-load browser navigation: Vaadin hands beforeEnter the route
        // parameter still percent-encoded (my%5Fmessage) instead of the raw messageId
        // (my_message) that in-app navigate() preserves. Message ids are constrained to
        // [a-zA-Z0-9_]+ by the UI binder, but legacy/imported data has no such guarantee.
        MessageData message = new MessageData();
        message.setText("Welcome!");
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setMessages(Map.of("my_message", message));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
                new RouteParam(RouteIds.MESSAGE_ID.getValue(), "my%5Fmessage")));

        TextArea messageText = find(TextArea.class, view).single();
        assertThat(messageText.getValue()).isEqualTo("Welcome!");
    }

    @Test
    void beforeEnter_unknownAdventureId_forwardsToAdventuresMenuView() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class)))
                .thenReturn(Optional.empty());
        BeforeEnterEvent event = eventWithParams(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "missing"),
                new RouteParam(RouteIds.MESSAGE_ID.getValue(), "msg-1"));

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Adventure not found or access denied: missing");
    }
}
