package com.pdg.adventure.view.adventure;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
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
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.support.FlashNotifier;
import com.pdg.adventure.view.support.RouteIds;

class AdventureEditorViewRoutingTest extends BrowserlessTest {

    private AdventureAccessService accessService;
    private AdventureEditorView view;

    @BeforeEach
    void setUp() {
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new AdventureEditorView(accessService);
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
    void beforeEnter_validAdventureId_setsTitleFromAdventureTitle() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setTitle("The Demo");
        adventure.setLocationData(new HashMap<>());
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithAdventureId("adv-1"));

        assertThat(view.getPageTitle()).isEqualTo("Edit Adventure: The Demo");
    }

    @Test
    void beforeEnter_unknownAdventureId_forwardsToAdventuresMenuInsteadOfCrashing() {
        when(accessService.findAdventureById(eq("missing"), any(UserData.class)))
                .thenReturn(Optional.empty());
        BeforeEnterEvent event = eventWithAdventureId("missing");

        view.beforeEnter(event);

        verify(event).forwardTo(AdventuresMenuView.class);
        FlashNotifier.showPending();
        Notification notification = find(Notification.class).single();
        assertThat(test(notification).getText()).isEqualTo("Adventure not found or access denied: missing");
    }

    @Test
    void beforeEnter_noAdventureId_setsNewAdventureTitle() {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters());

        view.beforeEnter(event);

        assertThat(view.getPageTitle()).isEqualTo("A new adventure awaits!");
    }

    @Test
    void beforeEnter_newAdventure_disablesTestButton() {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters());

        view.beforeEnter(event);

        assertThat(find(Button.class, view).withText("Test").single().isEnabled()).isFalse();
    }

    @Test
    void beforeEnter_savedAdventureWithLocations_enablesTestButton() {
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventureWithOneLocation("adv-1", "The Demo")));

        view.beforeEnter(eventWithAdventureId("adv-1"));

        assertThat(find(Button.class, view).withText("Test").single().isEnabled()).isTrue();
    }

    @Test
    void beforeEnter_adventureWithNoLocations_disablesTestButton() {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-empty");
        adventure.setTitle("Empty");
        adventure.setLocationData(new HashMap<>());
        when(accessService.findAdventureById(eq("adv-empty"), any(UserData.class)))
                .thenReturn(Optional.of(adventure));

        view.beforeEnter(eventWithAdventureId("adv-empty"));

        assertThat(find(Button.class, view).withText("Test").single().isEnabled()).isFalse();
    }

    @Test
    void editingTheTitle_disablesTestButton_untilSaved() {
        AdventureData adventure = adventureWithOneLocation("adv-1", "The Demo");
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class))).thenReturn(Optional.of(adventure));
        view.beforeEnter(eventWithAdventureId("adv-1"));
        Button testButton = find(Button.class, view).withText("Test").single();
        assertThat(testButton.isEnabled()).isTrue();

        TextField titleField = find(TextField.class, view).withLabel("Title").single();
        test(titleField).setValue("A New Title");
        assertThat(testButton.isEnabled())
                .as("unsaved changes must disable Test, which only ever plays the saved adventure")
                .isFalse();

        Button saveButton = find(Button.class, view).withText("Save").single();
        test(saveButton).click();

        assertThat(testButton.isEnabled())
                .as("saving should rebase Binder.hasChanges() so Test re-enables")
                .isTrue();
        verify(accessService).saveAdventureData(eq(adventure), any(UserData.class));
    }

    private static AdventureData adventureWithOneLocation(String anAdventureId, String aTitle) {
        AdventureData adventure = new AdventureData();
        adventure.setId(anAdventureId);
        adventure.setTitle(aTitle);
        HashMap<String, LocationData> locations = new HashMap<>();
        LocationData location = new LocationData();
        location.setId("loc-1");
        locations.put("loc-1", location);
        adventure.setLocationData(locations);
        return adventure;
    }
}
