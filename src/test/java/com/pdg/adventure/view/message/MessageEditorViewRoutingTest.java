package com.pdg.adventure.view.message;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
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
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.support.FlashNotifier;
import com.pdg.adventure.view.support.RouteIds;

class MessageEditorViewRoutingTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private MessageEditorView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new MessageEditorView(adventureService, accessService);
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

    private static AdventureData adventureWith(MessageData... messages) {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        Map<String, MessageData> byId = new HashMap<>();
        for (MessageData message : messages) {
            byId.put(message.getId(), message);
        }
        adventure.setMessages(byId);
        return adventure;
    }

    private void enter(AdventureData adventure, RouteParam... extraParams) {
        when(accessService.findAdventureById(eq(adventure.getId()), any(UserData.class)))
                .thenReturn(Optional.of(adventure));
        RouteParam[] params = new RouteParam[extraParams.length + 1];
        params[0] = new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventure.getId());
        System.arraycopy(extraParams, 0, params, 1, extraParams.length);
        view.beforeEnter(eventWithParams(params));
    }

    @Test
    void beforeEnter_validIds_populatesSummaryAndTextOfTheMessageWithThatId() {
        MessageData message = new MessageData("Greeting", "Welcome!");
        MessageData other = new MessageData("Other", "Something else");
        enter(adventureWith(message, other), new RouteParam(RouteIds.MESSAGE_ID.getValue(), message.getId()));

        assertThat(find(TextField.class, view).single().getValue()).isEqualTo("Greeting");
        assertThat(find(TextArea.class, view).single().getValue()).isEqualTo("Welcome!");
        assertThat(view.getPageTitle()).isEqualTo("Edit Message: Greeting");
    }

    @Test
    void beforeEnter_withoutMessageId_opensEmptyNewMessageForm() {
        enter(adventureWith(new MessageData("Greeting", "Welcome!")));

        assertThat(find(TextField.class, view).single().getValue()).isEmpty();
        assertThat(find(TextArea.class, view).single().getValue()).isEmpty();
        assertThat(view.getPageTitle()).isEqualTo("New Message");
    }

    @Test
    void beforeEnter_messageIdMatchingNoMessage_opensEmptyNewMessageForm() {
        enter(adventureWith(new MessageData("Greeting", "Welcome!")),
              new RouteParam(RouteIds.MESSAGE_ID.getValue(), "no-such-id"));

        assertThat(find(TextField.class, view).single().getValue()).isEmpty();
        assertThat(view.getPageTitle()).isEqualTo("New Message");
    }

    @Test
    void summaryField_acceptsASummaryAlreadyUsedByAnotherMessage() {
        enter(adventureWith(new MessageData("Duplicate me", "Already here.")));

        TextField summaryField = find(TextField.class, view).single();
        summaryField.setValue("Duplicate me");

        assertThat(summaryField.isInvalid()).isFalse();
    }

    @Test
    void summaryField_acceptsFreeText_notJustAlphanumericIds() {
        enter(adventureWith());

        TextField summaryField = find(TextField.class, view).single();
        summaryField.setValue("The door's locked - try the key!");

        assertThat(summaryField.isInvalid()).isFalse();
    }

    @Test
    void summaryField_rejectsBlankSummary() {
        enter(adventureWith());

        TextField summaryField = find(TextField.class, view).single();
        summaryField.setValue("   ");

        assertThat(summaryField.isInvalid()).isTrue();
    }

    @Test
    void savingANewMessage_storesItUnderItsOwnId_andNeverUnderItsSummary() {
        AdventureData adventure = adventureWith();
        enter(adventure);

        find(TextField.class, view).single().setValue("Locked door");
        find(TextArea.class, view).single().setValue("The door is locked.");
        test(find(Button.class, view).withText("Save").single()).click();

        assertThat(adventure.getMessages()).hasSize(1);
        Map.Entry<String, MessageData> stored = adventure.getMessages().entrySet().iterator().next();
        assertThat(stored.getKey()).isEqualTo(stored.getValue().getId()).isNotEqualTo("Locked door");
        assertThat(stored.getValue().getSummary()).isEqualTo("Locked door");
        assertThat(stored.getValue().getText()).isEqualTo("The door is locked.");
        verify(adventureService).saveAdventureData(adventure);
    }

    @Test
    void changingTheSummaryOfAnExistingMessage_keepsItsIdAndMapKey() {
        MessageData message = new MessageData("Old summary", "Some text");
        String originalId = message.getId();
        AdventureData adventure = adventureWith(message);
        enter(adventure, new RouteParam(RouteIds.MESSAGE_ID.getValue(), originalId));

        find(TextField.class, view).single().setValue("New summary");
        test(find(Button.class, view).withText("Save").single()).click();

        assertThat(adventure.getMessages()).containsOnlyKeys(originalId);
        assertThat(adventure.getMessages().get(originalId).getId()).isEqualTo(originalId);
        assertThat(adventure.getMessages().get(originalId).getSummary()).isEqualTo("New summary");
        verify(adventureService).saveAdventureData(adventure);
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
        FlashNotifier.showPending();
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Adventure not found or access denied: missing");
    }
}
