package com.pdg.adventure.view.location;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.PictureData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.support.RouteIds;

class LocationMapViewTest extends BrowserlessTest {

    private AdventureAccessService accessService;
    private AdventureService adventureService;
    private LocationMapView view;

    @BeforeEach
    void setUp() {
        accessService = mock(AdventureAccessService.class);
        adventureService = mock(AdventureService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new LocationMapView(accessService, adventureService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AdventureData enterAdventure(String worldMapPictureId, LocationData... locations) {
        AdventureData adventure = new AdventureData();
        adventure.setId("adv-1");
        for (LocationData location : locations) {
            adventure.getLocationData().put(location.getId(), location);
        }
        PictureData map = new PictureData();
        map.setName("the map of the world");
        map.setContentType("image/png");
        map.setContent(new byte[] {1, 2, 3});
        adventure.getPictureData().put(map.getId(), map);
        adventure.setWorldMapPictureId(worldMapPictureId == null ? null
                                       : worldMapPictureId.equals("MAP") ? map.getId() : worldMapPictureId);
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class))).thenReturn(Optional.of(adventure));
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1")));
        view.beforeEnter(event);
        return adventure;
    }

    private static LocationData location(String anId, String aNoun, Integer aColumn, Integer aRow) {
        LocationData location = new LocationData();
        location.setId(anId);
        if (aNoun != null) {
            location.getDescriptionData().setNoun(new Word(aNoun, Word.Type.NOUN));
        }
        location.setMapColumn(aColumn);
        location.setMapRow(aRow);
        return location;
    }

    private List<Div> cells() {
        return find(Div.class, view).withClassName("world-map-cell").all();
    }

    @Test
    void showsTheChosenPictureStretchedIntoASixteenByNineFrame() {
        enterAdventure("MAP");

        Image image = find(Image.class, view).single();
        assertThat(image.getSrc()).isNotBlank();
        assertThat(image.getAlt()).hasValue("the map of the world");
        assertThat(image.getStyle().get("object-fit")).isEqualTo("fill");

        Div frame = find(Div.class, view).withClassName("world-map").single();
        assertThat(frame.getStyle().get("aspect-ratio")).isEqualTo("16 / 9");
        assertThat(frame.getStyle().get("max-width")).isEqualTo("min(1280px, calc(50vh * 16 / 9))");
        assertThat(find(Button.class, view).all()).isEmpty();
    }

    @Test
    void overlaysAClickableTenByTenGrid() {
        enterAdventure("MAP");

        assertThat(cells()).hasSize(100);
    }

    @Test
    void clickingAnEmptyCellWithoutUnplacedLocationsSaysSoAndOpensNoDialog() {
        enterAdventure("MAP", location("placed", "cellar", 2, 3));
        Div cell = cells().get(0);

        ComponentUtil.fireEvent(cell, new ClickEvent<>(cell));

        assertThat(test(find(Notification.class).single()).getText()).isEqualTo("No unplaced locations.");
        assertThat(find(Dialog.class).all()).isEmpty();
    }

    @Test
    void theModeToggleStartsInPlaceMode() {
        enterAdventure("MAP");

        assertThat(find(RadioButtonGroup.class, view).single().getValue()).isEqualTo(LocationMapView.Mode.PLACE);
    }

    @Test
    void anOccupiedCellIsMarkedAndShowsTheLocationsDescriptionOnHover() {
        enterAdventure("MAP", location("placed", "cellar", 2, 3));

        Div cell = cells().get(3 * 10 + 2);
        assertThat(cell.hasClassName("world-map-cell-occupied")).isTrue();
        assertThat(cell.getElement().getAttribute("title")).contains("cellar");
        assertThat(cells().stream().filter(c -> c.hasClassName("world-map-cell-occupied"))).hasSize(1);
    }

    @Test
    void clickingAnEmptyCellInPlaceModeOffersOnlyUnplacedLocations() {
        enterAdventure("MAP", location("placed", "cellar", 2, 3), location("u1", "attic", null, null),
                       location("u2", "yard", null, null));
        Div cell = cells().get(0);

        ComponentUtil.fireEvent(cell, new ClickEvent<>(cell));

        Dialog dialog = find(Dialog.class).single();
        Grid<LocationData> grid = find(Grid.class, dialog).single();
        assertThat(grid.getListDataView().getItems().map(LocationData::getId).toList()).containsExactly("u1", "u2");
    }

    @Test
    void placingALocationStoresItsCellSavesItAndMarksTheCell() {
        LocationData unplaced = location("u1", "attic", null, null);
        enterAdventure("MAP", unplaced);
        Div cell = cells().get(3 * 10 + 7);
        ComponentUtil.fireEvent(cell, new ClickEvent<>(cell));
        Dialog dialog = find(Dialog.class).single();
        test(find(Grid.class, dialog).single()).select(0);

        test(find(Button.class, dialog).withText("Place").single()).click();

        assertThat(unplaced.getMapColumn()).isEqualTo(7);
        assertThat(unplaced.getMapRow()).isEqualTo(3);
        verify(adventureService).saveLocationData(unplaced);
        assertThat(cells().get(3 * 10 + 7).hasClassName("world-map-cell-occupied")).isTrue();
    }

    @Test
    void clickingAnOccupiedCellInPlaceModeOpensTheReplaceDialog() {
        enterAdventure("MAP", location("placed", "cellar", 2, 3), location("u1", "attic", null, null));
        Div cell = cells().get(3 * 10 + 2);

        ComponentUtil.fireEvent(cell, new ClickEvent<>(cell));

        assertThat(find(ReplaceLocationDialog.class).all()).hasSize(1);
    }

    @Test
    void clickingAnOccupiedCellWithoutUnplacedLocationsSaysSoAndOpensNoDialog() {
        enterAdventure("MAP", location("placed", "cellar", 2, 3));
        Div cell = cells().get(3 * 10 + 2);

        ComponentUtil.fireEvent(cell, new ClickEvent<>(cell));

        assertThat(test(find(Notification.class).single()).getText()).isEqualTo("No unplaced locations.");
        assertThat(find(Dialog.class).all()).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private RadioButtonGroup<LocationMapView.Mode> modeGroup() {
        return find(RadioButtonGroup.class, view).single();
    }

    private void click(int aColumn, int aRow) {
        Div cell = cells().get(aRow * 10 + aColumn);
        ComponentUtil.fireEvent(cell, new ClickEvent<>(cell));
    }

    private List<String> notificationTexts() {
        return find(Notification.class).all().stream().map(n -> test(n).getText()).toList();
    }

    @Test
    void connectModeNeedsTwoClicksOnOccupiedCells() {
        enterAdventure("MAP", location("l1", "cellar", 2, 3), location("l2", "attic", 6, 6));
        modeGroup().setValue(LocationMapView.Mode.CONNECT);

        click(2, 3);

        assertThat(cells().get(3 * 10 + 2).hasClassName("world-map-cell-selected")).isTrue();
        assertThat(notificationTexts()).containsExactly("Now click the target location.");
        assertThat(find(ConnectLocationsDialog.class).all()).isEmpty();

        click(6, 6);

        assertThat(find(ConnectLocationsDialog.class).all()).hasSize(1);
        assertThat(cells().get(3 * 10 + 2).hasClassName("world-map-cell-selected")).isFalse();
    }

    @Test
    void switchingTheModeDropsThePendingSource() {
        enterAdventure("MAP", location("l1", "cellar", 2, 3), location("l2", "attic", 6, 6));
        modeGroup().setValue(LocationMapView.Mode.CONNECT);
        click(2, 3);

        modeGroup().setValue(LocationMapView.Mode.PLACE);
        modeGroup().setValue(LocationMapView.Mode.CONNECT);

        assertThat(cells().get(3 * 10 + 2).hasClassName("world-map-cell-selected")).isFalse();
        click(6, 6);
        assertThat(find(ConnectLocationsDialog.class).all()).isEmpty();
    }

    @Test
    void anEmptyCellInConnectModeSaysSoAndKeepsThePendingSource() {
        enterAdventure("MAP", location("l1", "cellar", 2, 3), location("l2", "attic", 6, 6));
        modeGroup().setValue(LocationMapView.Mode.CONNECT);
        click(2, 3);

        click(0, 0);

        assertThat(notificationTexts()).contains("Click a cell that holds a location.");
        assertThat(cells().get(3 * 10 + 2).hasClassName("world-map-cell-selected")).isTrue();
        click(6, 6);
        assertThat(find(ConnectLocationsDialog.class).all()).hasSize(1);
    }

    @Test
    void clickingTheSameCellTwiceConnectsALocationToItself() {
        enterAdventure("MAP", location("l1", "cellar", 2, 3));
        modeGroup().setValue(LocationMapView.Mode.CONNECT);

        click(2, 3);
        click(2, 3);

        assertThat(find(ConnectLocationsDialog.class).all()).hasSize(1);
    }

    @Test
    void aDoubleClickOnAnEmptyCellOpensOnlyOneDialog() {
        enterAdventure("MAP", location("u1", "attic", null, null), location("u2", "yard", null, null));

        click(0, 0);
        click(0, 0);

        assertThat(find(Dialog.class).all()).hasSize(1);
    }

    @Test
    void placingWhenTheSaveFailsReportsItAndClosesTheDialog() {
        doThrow(new IllegalStateException("db down")).when(adventureService).saveLocationData(any());
        enterAdventure("MAP", location("u1", "attic", null, null));
        click(0, 0);
        Dialog dialog = find(Dialog.class).single();
        test(find(Grid.class, dialog).single()).select(0);

        test(find(Button.class, dialog).withText("Place").single()).click();

        assertThat(notificationTexts()).anyMatch(text -> text.contains("Could not save"));
        assertThat(find(Dialog.class).all()).isEmpty();
    }

    @Test
    void twoLocationsOnOneCellAndCellsOutOfRangeDoNotBreakTheMap() {
        enterAdventure("MAP", location("l1", "cellar", 1, 1), location("l2", "attic", 1, 1),
                       location("l3", "yard", 10, 10), location("l4", "hall", -1, 0));

        assertThat(cells().stream().filter(c -> c.hasClassName("world-map-cell-occupied"))).hasSize(1);
    }

    @Test
    void aLocationWithoutAnyDescriptionStillGetsAHoverText() {
        enterAdventure("MAP", location("bare", null, 4, 4));

        assertThat(cells().get(4 * 10 + 4).getElement().getAttribute("title")).isNotBlank();
    }

    @Test
    void hintsAtTheAdventureEditorWhenNoPictureIsChosen() {
        enterAdventure(null);

        assertThat(find(Image.class, view).all()).isEmpty();
        assertThat(cells()).isEmpty();
        assertThat(view.getElement().getTextRecursively()).contains("No world map").contains("Adventure Editor");
        assertThat(find(Button.class, view).withText("Open the Adventure Editor").all()).hasSize(1);
    }

    @Test
    void hintsAtTheAdventureEditorWhenTheChosenPictureNoLongerExists() {
        enterAdventure("deleted-picture-id");

        assertThat(find(Image.class, view).all()).isEmpty();
        assertThat(find(Button.class, view).withText("Open the Adventure Editor").all()).hasSize(1);
    }

    @Test
    void reenteringWithADifferentStateReplacesWhatWasShown() {
        enterAdventure("MAP");
        enterAdventure(null);

        assertThat(find(Image.class, view).all()).isEmpty();
        assertThat(cells()).isEmpty();
    }
}
