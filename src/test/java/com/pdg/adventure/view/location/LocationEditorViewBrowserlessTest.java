package com.pdg.adventure.view.location;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.support.RouteIds;

class LocationEditorViewBrowserlessTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private AdventureData adventureData;
    private LocationData locationData;
    private LocationEditorView view;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);
        adventureData = buildAdventureData();
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class)))
                .thenReturn(Optional.of(adventureData));
        view = new LocationEditorView(adventureService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AdventureData buildAdventureData() {
        AdventureData data = new AdventureData();
        data.setId("adv-1");

        locationData = new LocationData();
        locationData.setId("loc-1");
        locationData.setLumen(50);
        Word noun = new Word("cave", Word.Type.NOUN);
        Word adjective = new Word("dark", Word.Type.ADJECTIVE);
        DescriptionData desc = new DescriptionData();
        desc.setNoun(noun);
        desc.setAdjective(adjective);
        desc.setShortDescription("The dark cave");
        desc.setLongDescription("A very dark cave.");
        locationData.setDescriptionData(desc);

        HashMap<String, LocationData> locations = new HashMap<>();
        locations.put(locationData.getId(), locationData);
        data.setLocationData(locations);

        VocabularyData vocab = new VocabularyData();
        vocab.setWords(List.of(noun, adjective));
        data.setVocabularyData(vocab);

        return data;
    }

    private void enterWithLocationId(String locationId) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        RouteParameters params = mock(RouteParameters.class);
        when(event.getRouteParameters()).thenReturn(params);
        when(params.get(RouteIds.ADVENTURE_ID.getValue())).thenReturn(Optional.of("adv-1"));
        when(params.get(RouteIds.LOCATION_ID.getValue())).thenReturn(Optional.ofNullable(locationId));
        view.beforeEnter(event);
    }

    @Test
    void view_hasManageCommandsButton() {
        view.setData(adventureData);
        assertThat(find(Button.class, view).withText("Manage Commands").single()).isNotNull();
    }

    @Test
    void view_hasManageItemsButton() {
        view.setData(adventureData);
        assertThat(find(Button.class, view).withText("Manage Items").single()).isNotNull();
    }

    @Test
    void view_hasManageExitsButton() {
        view.setData(adventureData);
        assertThat(find(Button.class, view).withText("Manage Exits").single()).isNotNull();
    }

    @Test
    void saveButton_isDisabled_afterSetData() {
        view.setData(adventureData);
        assertThat(find(Button.class, view).withText("Save").single().isEnabled()).isFalse();
    }

    @Test
    void resetButton_isDisabled_afterSetData() {
        view.setData(adventureData);
        assertThat(find(Button.class, view).withText("Reset").single().isEnabled()).isFalse();
    }

    @Test
    void beforeEnter_withLocationId_setsEditPageTitle() {
        enterWithLocationId("loc-1");
        assertThat(view.getPageTitle()).contains("Edit Location");
    }

    @Test
    void beforeEnter_withoutLocationId_setsNewPageTitle() {
        enterWithLocationId(null);
        assertThat(view.getPageTitle()).isEqualTo("New Location");
    }

    @Test
    void setData_withMatchingLocationId_populatesView() {
        enterWithLocationId("loc-1");
        view.setData(adventureData);

        // save should still be disabled (no changes yet)
        assertThat(find(Button.class, view).withText("Save").single().isEnabled()).isFalse();
    }

    @Test
    void shortDescriptionChange_enablesResetButton() {
        enterWithLocationId("loc-1");
        view.setData(adventureData);

        Button reset = find(Button.class, view).withText("Reset").single();
        assertThat(reset.isEnabled()).isFalse();

        TextArea shortDesc = find(TextArea.class, view).all().getFirst();
        test(shortDesc).setValue("Updated short description");

        assertThat(reset.isEnabled()).isTrue();
    }
}
