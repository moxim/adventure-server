package com.pdg.adventure.view.adventure;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.engine.AdventureTestSession;
import com.pdg.adventure.server.engine.AdventureTestSession.TestResult;
import com.pdg.adventure.server.engine.AdventureTestSessionFactory;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.support.FlashNotifier;
import com.pdg.adventure.view.support.RouteIds;

class AdventureTestViewTest extends BrowserlessTest {

    private AdventureTestSessionFactory sessionFactory;
    private AdventureAccessService accessService;
    private AdventureTestSession session;
    private AdventureData adventureData;
    private AdventureTestView view;

    @BeforeEach
    void setUp() {
        sessionFactory = mock(AdventureTestSessionFactory.class);
        accessService = mock(AdventureAccessService.class);
        session = mock(AdventureTestSession.class);

        adventureData = new AdventureData();
        adventureData.setId("adv-1");
        adventureData.setTitle("The Demo");

        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));

        view = new AdventureTestView(sessionFactory, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private BeforeEnterEvent eventForAdv1() {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())));
        when(accessService.findAdventureById(eq(adventureData.getId()), any(UserData.class)))
                .thenReturn(Optional.of(adventureData));
        return event;
    }

    private void enter() {
        when(sessionFactory.start(adventureData)).thenReturn(session);
        view.beforeEnter(eventForAdv1());
    }

    @Test
    void beforeEnter_rendersTheOpeningRoomDescription() {
        when(session.submit("look")).thenReturn(new TestResult(List.of("A grand throne room."), false));

        enter();

        MessageList messageList = find(MessageList.class, view).single();
        assertThat(test(messageList).getMessages()).extracting(MessageListItem::getText)
                .containsExactly("A grand throne room.");
    }

    @Test
    void submittingAMessage_echoesThePlayersInput_thenAppendsTheEngineResponse() {
        when(session.submit("look")).thenReturn(new TestResult(List.of("A grand throne room."), false));
        enter();
        when(session.submit("go north")).thenReturn(new TestResult(List.of("You head north."), false));

        MessageInput messageInput = find(MessageInput.class, view).single();
        test(messageInput).send("go north");

        MessageList messageList = find(MessageList.class, view).single();
        assertThat(test(messageList).getMessages()).extracting(MessageListItem::getText)
                .containsExactly("A grand throne room.", "go north", "You head north.");
    }

    @Test
    void gameOver_disablesTheMessageInput() {
        when(session.submit("look")).thenReturn(new TestResult(List.of("A grand throne room."), false));
        enter();
        when(session.submit("quit")).thenReturn(new TestResult(List.of("Bye bye."), true));

        MessageInput messageInput = find(MessageInput.class, view).single();
        test(messageInput).send("quit");

        assertThat(messageInput.isEnabled()).isFalse();
    }

    @Test
    void beforeEnter_sessionCannotStart_forwardsToEditorWithAFlashMessage() {
        BeforeEnterEvent event = eventForAdv1();
        when(sessionFactory.start(adventureData)).thenThrow(new IllegalStateException("no locations"));

        view.beforeEnter(event);

        verify(event).forwardTo(eq(AdventureEditorView.class), any(RouteParameters.class));
        FlashNotifier.showPending();
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).contains("no locations");
    }
}
