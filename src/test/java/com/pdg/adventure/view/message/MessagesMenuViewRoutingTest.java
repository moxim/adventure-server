package com.pdg.adventure.view.message;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.AdventureFont;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.support.FlashNotifier;
import com.pdg.adventure.view.support.RouteIds;

class MessagesMenuViewRoutingTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private MessagesMenuView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new MessagesMenuView(adventureService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static BeforeEnterEvent eventWithAdventureId(String adventureId) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureId)));
        return event;
    }

    @Test
    void beforeEnter_validAdventureId_populatesMessagesGrid() {
        MessageData message = new MessageData("Greeting", "Welcome!");
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setTitle("The Demo");
        adventure.setMessages(Map.of(message.getId(), message));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithAdventureId("adv-1"));

        assertThat(view.getPageTitle()).isEqualTo("Messages for The Demo");
        Grid<?> grid = find(Grid.class, view).single();
        assertThat(test(grid).size()).isEqualTo(1);
    }

    // Delete goes through a GridContextMenu item click, which has no reliable way to be driven
    // from a browserless test - this test instead builds the confirmation dialog directly
    // (buildDeleteConfirmDialog is package-private for exactly this) and drives it as a user
    // would: open it, then confirm.
    @Test
    void confirmingDeleteDialog_removesTheMessage_andDoesNotConsultMessageService() {
        Map<String, MessageData> messages = new HashMap<>();
        MessageData message = new MessageData("Greeting", "Welcome!");
        messages.put(message.getId(), message);
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setTitle("The Demo");
        adventure.setMessages(messages);
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        view.beforeEnter(eventWithAdventureId("adv-1"));

        MessageDescriptionAdapter adapter = new MessageDescriptionAdapter(
                new MessageViewModel(message));
        ConfirmDialog dialog = view.buildDeleteConfirmDialog(adapter);
        UI.getCurrent().add(dialog);
        dialog.open();

        test(dialog).confirm();

        assertThat(adventure.getMessages()).doesNotContainKey(message.getId());
        verify(adventureService).saveAdventureData(adventure);
    }

    @Test
    void beforeEnter_unknownAdventureId_forwardsToAdventuresMenuView() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class)))
                .thenReturn(Optional.empty());
        BeforeEnterEvent event = eventWithAdventureId("missing");

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
        FlashNotifier.showPending();
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Adventure not found or access denied: missing");
    }

    // Like delete, duplicate sits behind a GridContextMenu click that a browserless test can't drive, so
    // duplicateMessage is package-private and called directly.
    @Test
    void duplicatingAMessageKeepsItsFont() {
        Map<String, MessageData> messages = new HashMap<>();
        MessageData note = new MessageData("The note", "Meet me at midnight.");
        note.setFont(AdventureFont.SPECIAL_ELITE);
        messages.put(note.getId(), note);
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setTitle("The Demo");
        adventure.setMessages(messages);
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        view.beforeEnter(eventWithAdventureId("adv-1"));

        view.duplicateMessage(new MessageDescriptionAdapter(new MessageViewModel(note)));

        assertThat(adventure.getMessages().values()).hasSize(2)
                .filteredOn(message -> !message.getId().equals(note.getId())).singleElement()
                .satisfies(copy -> {
                    assertThat(copy.getSummary()).isEqualTo("The note (copy)");
                    assertThat(copy.getFont()).isEqualTo(AdventureFont.SPECIAL_ELITE);
                });
    }
}
