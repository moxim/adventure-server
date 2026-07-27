package com.pdg.adventure.view.adventure;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.Location;
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
import com.pdg.adventure.server.engine.AdventureRunSession;
import com.pdg.adventure.server.engine.AdventureRunSession.RunResult;
import com.pdg.adventure.server.engine.AdventureRunSessionFactory;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.player.PlayerLibraryView;
import com.pdg.adventure.view.support.FlashNotifier;
import com.pdg.adventure.view.support.RouteIds;

class AdventureRunViewTest extends BrowserlessTest {

    private AdventureRunSessionFactory sessionFactory;
    private AdventureAccessService accessService;
    private AdventureRunSession session;
    private AdventureData adventureData;
    private AdventureRunView view;

    @BeforeEach
    void setUp() {
        sessionFactory = mock(AdventureRunSessionFactory.class);
        accessService = mock(AdventureAccessService.class);
        session = mock(AdventureRunSession.class);

        adventureData = new AdventureData();
        adventureData.setId("adv-1");
        adventureData.setTitle("The Demo");

        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));

        view = new AdventureRunView(sessionFactory, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private BeforeEnterEvent eventFor(String path) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getLocation()).thenReturn(new Location(path));
        when(event.getRouteParameters()).thenReturn(new RouteParameters(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())));
        when(accessService.findAdventureById(eq(adventureData.getId()), any(UserData.class)))
                .thenReturn(Optional.of(adventureData));
        return event;
    }

    private void enterViaAuthorRoute() {
        when(sessionFactory.start(adventureData)).thenReturn(session);
        view.beforeEnter(eventFor("author/adventures/adv-1/test"));
    }

    @Test
    void beforeEnter_rendersTheOpeningRoomDescription() {
        when(session.submit("look")).thenReturn(new RunResult(List.of("A grand throne room."), false));

        enterViaAuthorRoute();

        MessageList messageList = find(MessageList.class, view).single();
        assertThat(test(messageList).getMessages()).extracting(MessageListItem::getText)
                .containsExactly("A grand throne room.");
    }

    @Test
    void submittingAMessage_echoesThePlayersInput_thenAppendsTheEngineResponse() {
        when(session.submit("look")).thenReturn(new RunResult(List.of("A grand throne room."), false));
        enterViaAuthorRoute();
        when(session.submit("go north")).thenReturn(new RunResult(List.of("You head north."), false));

        MessageInput messageInput = find(MessageInput.class, view).single();
        test(messageInput).send("go north");

        MessageList messageList = find(MessageList.class, view).single();
        assertThat(test(messageList).getMessages()).extracting(MessageListItem::getText)
                .containsExactly("A grand throne room.", "go north", "You head north.");
    }

    @Test
    void multipleNarratorLinesFromOneTurn_arePooledIntoASingleMessageListItem() {
        when(session.submit("look")).thenReturn(
                new RunResult(List.of("You carry:", "a rusty key"), false));

        enterViaAuthorRoute();

        MessageList messageList = find(MessageList.class, view).single();
        assertThat(test(messageList).getMessages()).extracting(MessageListItem::getText)
                .containsExactly("You carry:\na rusty key");
    }

    @Test
    void gameOver_disablesTheMessageInput() {
        when(session.submit("look")).thenReturn(new RunResult(List.of("A grand throne room."), false));
        enterViaAuthorRoute();
        when(session.submit("quit")).thenReturn(new RunResult(List.of("Bye bye."), true));

        MessageInput messageInput = find(MessageInput.class, view).single();
        test(messageInput).send("quit");

        assertThat(messageInput.isEnabled()).isFalse();
    }

    @Test
    void beforeEnter_viaAuthorRoute_sessionCannotStart_forwardsToEditorWithAFlashMessage() {
        BeforeEnterEvent event = eventFor("author/adventures/adv-1/test");
        when(sessionFactory.start(adventureData)).thenThrow(new IllegalStateException("no locations"));

        view.beforeEnter(event);

        verify(event).forwardTo(eq(AdventureEditorView.class), any(RouteParameters.class));
        FlashNotifier.showPending();
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).contains("no locations");
    }

    @Test
    void beforeEnter_viaPlayerRoute_sessionCannotStart_forwardsToPlayerLibraryInstead() {
        BeforeEnterEvent event = eventFor("player/library/adv-1/run");
        when(sessionFactory.start(adventureData)).thenThrow(new IllegalStateException("no locations"));

        view.beforeEnter(event);

        verify(event).forwardTo(PlayerLibraryView.class);
    }

    @Test
    void beforeEnter_viaPlayerRoute_adventureNotFound_forwardsToPlayerLibraryNotAuthorMenu() {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getLocation()).thenReturn(new Location("player/library/missing/run"));
        when(event.getRouteParameters()).thenReturn(new RouteParameters(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "missing")));
        when(accessService.findAdventureById(eq("missing"), any(UserData.class))).thenReturn(Optional.empty());

        view.beforeEnter(event);

        verify(event).forwardTo(PlayerLibraryView.class);
    }

    @Test
    void beforeEnter_viaPlayerRoute_rendersTheOpeningRoomDescription() {
        when(session.submit("look")).thenReturn(new RunResult(List.of("A grand throne room."), false));
        when(sessionFactory.start(adventureData)).thenReturn(session);

        view.beforeEnter(eventFor("player/library/adv-1/run"));

        MessageList messageList = find(MessageList.class, view).single();
        assertThat(test(messageList).getMessages()).extracting(MessageListItem::getText)
                .containsExactly("A grand throne room.");
    }

    // The author-only "Run Adventure" (AdventuresMenuView) and "Test" (AdventureEditorView)
    // buttons both land on the same author/adventures/:id/test path - AdventureRunView.menuRunPath
    // adds ?from=menu to tell them apart, since the class-based navigate(AdventureRunView.class,
    // RouteParameters) that would normally distinguish two @Route/@RouteAlias registrations can't:
    // both routes share an identically-shaped :adventureId parameter, so Vaadin always resolves
    // outbound navigation to the primary @Route regardless of which button was actually clicked.
    @Test
    void beforeEnter_viaMenuRoute_sessionCannotStart_forwardsToAdventuresMenuNotEditor() {
        BeforeEnterEvent event = eventFor(AdventureRunView.menuRunPath("adv-1"));
        when(sessionFactory.start(adventureData)).thenThrow(new IllegalStateException("no locations"));

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
    }

    @Test
    void beforeEnter_viaMenuRoute_adventureNotFound_forwardsToAdventuresMenu() {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getLocation()).thenReturn(new Location(AdventureRunView.menuRunPath("missing")));
        when(event.getRouteParameters()).thenReturn(new RouteParameters(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "missing")));
        when(accessService.findAdventureById(eq("missing"), any(UserData.class))).thenReturn(Optional.empty());

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
    }

    @Test
    void beforeEnter_viaMenuRoute_rendersTheOpeningRoomDescription() {
        when(session.submit("look")).thenReturn(new RunResult(List.of("A grand throne room."), false));
        when(sessionFactory.start(adventureData)).thenReturn(session);

        view.beforeEnter(eventFor(AdventureRunView.menuRunPath("adv-1")));

        MessageList messageList = find(MessageList.class, view).single();
        assertThat(test(messageList).getMessages()).extracting(MessageListItem::getText)
                .containsExactly("A grand throne room.");
    }
}
