package com.pdg.adventure.view.location;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.direction.DirectionBuilder;
import com.pdg.adventure.view.support.ViewSupporter;

class ReplaceLocationDialogTest extends BrowserlessTest {

    private final Word go = new Word("go", Word.Type.VERB);
    private final Word enter = new Word("enter", Word.Type.VERB);
    private final Word north = new Word("north", Word.Type.NOUN);
    private final Word door = new Word("door", Word.Type.NOUN);

    private AdventureService service;
    private AdventureData adventure;
    private LocationData a;
    private LocationData x;
    private LocationData y;
    private AtomicBoolean done;
    private ReplaceLocationDialog dialog;

    @BeforeEach
    void setUp() {
        service = mock(AdventureService.class);
        adventure = new AdventureData();
        a = location("a", "attic", 0, 0);
        x = location("x", "cellar", 5, 5);
        y = location("y", "yard", null, null);
        done = new AtomicBoolean();
    }

    private LocationData location(String anId, String aNoun, Integer aColumn, Integer aRow) {
        LocationData location = new LocationData();
        location.setId(anId);
        location.getDescriptionData().setNoun(new Word(aNoun, Word.Type.NOUN));
        location.setMapColumn(aColumn);
        location.setMapRow(aRow);
        adventure.getLocationData().put(anId, location);
        return location;
    }

    private void open() {
        dialog = new ReplaceLocationDialog(adventure, service, x, () -> done.set(true));
        dialog.open();
    }

    @SuppressWarnings("unchecked")
    private Grid<LocationData> unplacedGrid() {
        return (Grid<LocationData>) find(Grid.class, dialog).atIndex(1);
    }

    @SuppressWarnings("unchecked")
    private Grid<DirectionData> directionGrid() {
        return (Grid<DirectionData>) find(Grid.class, dialog).atIndex(2);
    }

    private Button button(String aText) {
        return find(Button.class, dialog).withText(aText).single();
    }

    private static String describe(DirectionData aDirection) {
        return ViewSupporter.formatDescription(aDirection.getCommandData().getCommandDescription());
    }

    @Test
    void replaceWithoutClashSavesEveryChangedLocationOnceAndRepointsIncomingDirections() {
        DirectionData aToX = DirectionBuilder.create(a, "x", go, null, north);
        open();
        test(unplacedGrid()).select(0);

        test(button("Replace")).click();

        assertThat(aToX.getDestinationId()).isEqualTo("y");
        verify(service).saveLocationData(a);
        verify(service).saveLocationData(x);
        verify(service).saveLocationData(y);
        assertThat(done).isTrue();
    }

    @Test
    void replaceStaysDisabledUntilANewLocationIsChosen() {
        open();

        assertThat(button("Replace").isEnabled()).isFalse();
        test(unplacedGrid()).select(0);
        assertThat(button("Replace").isEnabled()).isTrue();
    }

    @Test
    void aClashAsksForConfirmationAndSavesNothingUntilConfirmed() {
        DirectionData moved = DirectionBuilder.create(x, "a", go, null, north);
        DirectionBuilder.create(y, "a", go, null, north);
        open();
        test(unplacedGrid()).select(0);
        directionGrid().select(moved);

        test(button("Replace")).click();

        ConfirmDialog confirm = find(ConfirmDialog.class).single();
        assertThat(test(confirm).getText()).contains(describe(moved));
        verify(service, never()).saveLocationData(any());

        test(confirm).confirm();

        verify(service).saveLocationData(y);
        assertThat(y.getDirectionsData()).containsExactly(moved);
        assertThat(done).isTrue();
    }

    @Test
    void cancellingTheClashConfirmationLeavesTheDataUntouched() {
        DirectionData moved = DirectionBuilder.create(x, "a", go, null, north);
        DirectionData clashing = DirectionBuilder.create(y, "a", go, null, north);
        open();
        test(unplacedGrid()).select(0);
        directionGrid().select(moved);
        test(button("Replace")).click();

        test(find(ConfirmDialog.class).single()).cancel();

        verify(service, never()).saveLocationData(any());
        assertThat(x.getDirectionsData()).containsExactly(moved);
        assertThat(y.getDirectionsData()).containsExactly(clashing);
        assertThat(x.getMapColumn()).isEqualTo(5);
        assertThat(done).isFalse();
    }

    @Test
    void theDialogNamesTheDirectionsThatAreDroppedOrCannotMoveBeforeConfirming() {
        DirectionData yToX = DirectionBuilder.create(y, "x", go, null, north);
        DirectionData xToY = DirectionBuilder.create(x, "y", enter, null, door);
        open();

        test(unplacedGrid()).select(0);

        String text = dialog.getElement().getTextRecursively();
        assertThat(text).contains(describe(yToX)).contains(describe(xToY));
        assertThat(directionGrid().getListDataView().getItems().toList()).doesNotContain(xToY);
    }

    @Test
    void directionsTickedBeforeTheNewLocationIsChosenStayTicked() {
        DirectionData ticked = DirectionBuilder.create(x, "a", go, null, north);
        open();
        directionGrid().select(ticked);

        test(unplacedGrid()).select(0);
        test(button("Replace")).click();

        assertThat(y.getDirectionsData()).containsExactly(ticked);
    }

    @Test
    void aFailedSaveIsReportedAndTheDialogClosedSoNothingCanBeReplayed() {
        doThrow(new IllegalStateException("db down")).when(service).saveLocationData(any());
        open();
        test(unplacedGrid()).select(0);

        test(button("Replace")).click();

        assertThat(find(Notification.class).all().stream().map(n -> test(n).getText()).toList())
                .anyMatch(text -> text.contains("Could not save"));
        assertThat(dialog.isOpened()).isFalse();
        assertThat(done).isTrue();
    }
}
