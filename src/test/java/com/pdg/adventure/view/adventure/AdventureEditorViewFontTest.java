package com.pdg.adventure.view.adventure;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.AdventureFont;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.support.RouteIds;

class AdventureEditorViewFontTest extends BrowserlessTest {

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

    private AdventureData enterSavedAdventure(AdventureFont font) {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setTitle("The Demo");
        adventure.setFont(font);
        LocationData startLocation = new LocationData();
        startLocation.getDescriptionData().setNoun(new Word("hall", Word.Type.NOUN));
        adventure.getLocationData().put("loc-1", startLocation);
        adventure.setCurrentLocationId("loc-1");
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class))).thenReturn(Optional.of(adventure));
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1")));
        view.beforeEnter(event);
        return adventure;
    }

    @SuppressWarnings("unchecked")
    private Select<AdventureFont> fontSelect() {
        return (Select<AdventureFont>) find(Select.class, view).withLabel("Run Font").single();
    }

    @Test
    void offersEveryFontAndNoEmptySelection() {
        enterSavedAdventure(AdventureFont.DEFAULT);

        Select<AdventureFont> select = fontSelect();

        assertThat(select.getListDataView().getItems()).containsExactly(AdventureFont.values());
        assertThat(select.isEmptySelectionAllowed()).isFalse();
    }

    @Test
    void showsTheFontOfALoadedAdventure() {
        enterSavedAdventure(AdventureFont.LORA);

        assertThat(fontSelect().getValue()).isEqualTo(AdventureFont.LORA);
    }

    @Test
    void aNewAdventureStartsWithTheDefaultFont() {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters());
        view.beforeEnter(event);

        assertThat(fontSelect().getValue()).isEqualTo(AdventureFont.DEFAULT);
    }

    @Test
    void choosingAFontChangesTheAdventureAndMakesItUnsaved() {
        AdventureData adventure = enterSavedAdventure(AdventureFont.DEFAULT);
        Button saveButton = find(Button.class, view).withText("Save").single();
        Button testButton = find(Button.class, view).withText("Test").single();
        assertThat(testButton.isEnabled()).isTrue();

        test(fontSelect()).selectItem(AdventureFont.CINZEL.label());

        assertThat(adventure.getFont()).isEqualTo(AdventureFont.CINZEL);
        assertThat(saveButton.isEnabled()).as("save").isTrue();
        assertThat(testButton.isEnabled()).as("test must wait for the save").isFalse();
    }

    @Test
    void loadingAnAdventureDoesNotMarkItUnsaved() {
        enterSavedAdventure(AdventureFont.CINZEL);

        assertThat(find(Button.class, view).withText("Test").single().isEnabled()).isTrue();
    }
}
