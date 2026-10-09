package com.pdg.adventure.view.adventure;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
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
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.PictureData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.support.RouteIds;

class AdventureEditorViewWorldMapTest extends BrowserlessTest {

    private AdventureAccessService accessService;
    private AdventureEditorView view;
    private PictureData worldPicture;
    private PictureData cavePicture;

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

    private AdventureData enterSavedAdventure(boolean withWorldMap) {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        adventure.setTitle("The Demo");
        LocationData startLocation = new LocationData();
        startLocation.getDescriptionData().setNoun(new Word("hall", Word.Type.NOUN));
        adventure.getLocationData().put("loc-1", startLocation);
        adventure.setCurrentLocationId("loc-1");
        worldPicture = picture("the map of the world");
        cavePicture = picture("a dark cave");
        adventure.getPictureData().put(worldPicture.getId(), worldPicture);
        adventure.getPictureData().put(cavePicture.getId(), cavePicture);
        if (withWorldMap) {
            adventure.setWorldMapPictureId(worldPicture.getId());
        }
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class))).thenReturn(Optional.of(adventure));
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1")));
        view.beforeEnter(event);
        return adventure;
    }

    private static PictureData picture(String aName) {
        PictureData picture = new PictureData();
        picture.setName(aName);
        return picture;
    }

    @SuppressWarnings("unchecked")
    private ComboBox<PictureData> worldMapBox() {
        return (ComboBox<PictureData>) find(ComboBox.class, view).withLabel("World Map").single();
    }

    @Test
    void offersTheAdventuresPicturesByNameAndAllowsNone() {
        enterSavedAdventure(false);

        ComboBox<PictureData> box = worldMapBox();

        assertThat(box.getListDataView().getItems()).containsExactlyInAnyOrder(worldPicture, cavePicture);
        assertThat(box.getItemLabelGenerator().apply(worldPicture)).isEqualTo("the map of the world");
        assertThat(box.isClearButtonVisible()).isTrue();
        assertThat(box.isEnabled()).isTrue();
    }

    @Test
    void showsTheWorldMapOfALoadedAdventure() {
        enterSavedAdventure(true);

        assertThat(worldMapBox().getValue()).isEqualTo(worldPicture);
    }

    @Test
    void showsNothingForAnAdventureWithoutAWorldMap() {
        enterSavedAdventure(false);

        assertThat(worldMapBox().getValue()).isNull();
    }

    @Test
    void choosingAPictureSetsTheWorldMapAndMakesTheAdventureUnsaved() {
        AdventureData adventure = enterSavedAdventure(false);
        Button saveButton = find(Button.class, view).withText("Save").single();
        Button testButton = find(Button.class, view).withText("Test").single();
        assertThat(testButton.isEnabled()).isTrue();

        test(worldMapBox()).selectItem(worldPicture.getName());

        assertThat(adventure.getWorldMapPictureId()).isEqualTo(worldPicture.getId());
        assertThat(saveButton.isEnabled()).as("save").isTrue();
        assertThat(testButton.isEnabled()).as("test must wait for the save").isFalse();
    }

    @Test
    void clearingTheFieldRemovesTheWorldMap() {
        AdventureData adventure = enterSavedAdventure(true);

        worldMapBox().clear();

        assertThat(adventure.getWorldMapPictureId()).isNull();
    }

    @Test
    void aNewAdventureCannotChooseAMapYet() {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters());
        view.beforeEnter(event);

        ComboBox<PictureData> box = worldMapBox();

        assertThat(box.isEnabled()).isFalse();
        assertThat(box.getHelperText()).contains("Save");
    }

    @Test
    void loadingAnAdventureDoesNotMarkItUnsaved() {
        enterSavedAdventure(true);

        assertThat(find(Button.class, view).withText("Test").single().isEnabled()).isTrue();
    }
}
