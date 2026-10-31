package com.pdg.adventure.view.location;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.action.MovePlayerActionData;
import com.pdg.adventure.view.direction.DirectionBuilder;
import com.pdg.adventure.view.support.PictureGrid;

class LocationMapPlacementTest {

    private final Word go = new Word("go", Word.Type.VERB);
    private final Word enter = new Word("enter", Word.Type.VERB);
    private final Word north = new Word("north", Word.Type.NOUN);
    private final Word door = new Word("door", Word.Type.NOUN);

    private AdventureData adventure;
    private LocationData a;
    private LocationData x;
    private LocationData y;

    @BeforeEach
    void setUp() {
        adventure = new AdventureData();
        a = location("a", "attic", 0, 0);
        x = location("x", "cellar", 5, 5);
        y = location("y", "yard", null, null);
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

    private static String moveTarget(DirectionData aDirection) {
        return ((MovePlayerActionData) aDirection.getCommandData().getActions().getFirst()).getLocationId();
    }

    @Test
    void locationAtFindsOccupiedCellAndNothingOtherwise() {
        assertThat(LocationMapPlacement.locationAt(adventure, new PictureGrid.Cell(5, 5))).contains(x);
        assertThat(LocationMapPlacement.locationAt(adventure, new PictureGrid.Cell(6, 5))).isEmpty();
    }

    @Test
    void unplacedListsOnlyLocationsWithoutCell() {
        assertThat(LocationMapPlacement.unplaced(adventure)).containsExactly(y);
    }

    @Test
    void placeStoresTheCell() {
        LocationMapPlacement.place(y, new PictureGrid.Cell(3, 7));

        assertThat(y.getMapColumn()).isEqualTo(3);
        assertThat(y.getMapRow()).isEqualTo(7);
    }

    @Test
    void directionsToListsOnlyDirectionsWithThatDestination() {
        DirectionData toX = DirectionBuilder.create(a, "x", go, null, north);
        DirectionBuilder.create(a, "y", go, null, door);

        assertThat(LocationMapPlacement.directionsTo(a, "x")).containsExactly(toX);
    }

    @Test
    void findSameWordsComparesVerbAdjectiveAndNoun() {
        DirectionData goNorth = DirectionBuilder.create(a, "x", go, null, north);

        assertThat(LocationMapPlacement.findSameWords(a, go, null, north)).contains(goNorth);
        assertThat(LocationMapPlacement.findSameWords(a, enter, null, north)).isEmpty();
        assertThat(LocationMapPlacement.findSameWords(a, go, null, door)).isEmpty();
        assertThat(LocationMapPlacement.findSameWords(a, go, new Word("dark", Word.Type.ADJECTIVE), north)).isEmpty();
    }

    @Test
    void replaceMovesCellAndRepointsIncomingDirectionsAndTheirMoveActions() {
        DirectionData aToX = DirectionBuilder.create(a, "x", go, null, north);

        List<LocationData> changed = LocationMapPlacement.replace(adventure, x, y, List.of());

        assertThat(y.getMapColumn()).isEqualTo(5);
        assertThat(y.getMapRow()).isEqualTo(5);
        assertThat(x.getMapColumn()).isNull();
        assertThat(x.getMapRow()).isNull();
        assertThat(aToX.getDestinationId()).isEqualTo("y");
        assertThat(moveTarget(aToX)).isEqualTo("y");
        assertThat(changed).contains(a, x, y);
    }

    @Test
    void replaceMovesOnlyTheTickedOutgoingDirections() {
        DirectionData ticked = DirectionBuilder.create(x, "a", go, null, north);
        DirectionData unticked = DirectionBuilder.create(x, "a", enter, null, door);

        LocationMapPlacement.replace(adventure, x, y, List.of(ticked));

        assertThat(y.getDirectionsData()).containsExactly(ticked);
        assertThat(x.getDirectionsData()).containsExactly(unticked);
    }

    @Test
    void replaceReplacesSameWordedDirectionOnYAndGivesTheMovedOneYsNoun() {
        DirectionData moved = DirectionBuilder.create(x, "a", go, null, north);
        DirectionData clashing = DirectionBuilder.create(y, "a", go, null, north);
        assertThat(LocationMapPlacement.clashes(y, List.of(moved))).containsExactly(clashing);

        LocationMapPlacement.replace(adventure, x, y, List.of(moved));

        assertThat(y.getDirectionsData()).containsExactly(moved);
        assertThat(moved.getDescriptionData().getNoun().getText()).isEqualTo("yard");
    }

    @Test
    void replaceDropsYsDirectionToXAndDoesNotMoveXsDirectionToY() {
        DirectionData yToX = DirectionBuilder.create(y, "x", go, null, north);
        DirectionData xToY = DirectionBuilder.create(x, "y", enter, null, door);
        assertThat(LocationMapPlacement.directionsToDrop(x, y)).containsExactly(yToX);
        assertThat(LocationMapPlacement.unmovable(x, y)).containsExactly(xToY);

        LocationMapPlacement.replace(adventure, x, y, List.of(xToY));

        assertThat(y.getDirectionsData()).doesNotContain(yToX, xToY);
        assertThat(x.getDirectionsData()).containsExactly(xToY);
    }

    @Test
    void replaceOfSelfLoopOnXMakesItAYToYLoopWhenTickedAndAnXToYDirectionWhenNot() {
        // TODO: Review needed — loop produced by replace
        DirectionData loopTicked = DirectionBuilder.create(x, "x", go, null, north);
        DirectionData loopUnticked = DirectionBuilder.create(x, "x", enter, null, door);

        LocationMapPlacement.replace(adventure, x, y, List.of(loopTicked));

        assertThat(y.getDirectionsData()).containsExactly(loopTicked);
        assertThat(loopTicked.getDestinationId()).isEqualTo("y");
        assertThat(x.getDirectionsData()).containsExactly(loopUnticked);
        assertThat(loopUnticked.getDestinationId()).isEqualTo("y");
    }

    @Test
    void replaceReturnsEveryChangedLocationOnce() {
        DirectionBuilder.create(a, "x", go, null, north);
        DirectionBuilder.create(a, "x", enter, null, door);

        List<LocationData> changed = LocationMapPlacement.replace(adventure, x, y, List.of());

        assertThat(changed).doesNotHaveDuplicates().containsExactlyInAnyOrder(a, x, y);
    }

    @Test
    void replaceKeepsTwoMovedDirectionsThatShareTheirWords() {
        DirectionData first = DirectionBuilder.create(x, "a", go, null, north);
        DirectionData second = DirectionBuilder.create(x, "b", go, null, north);

        LocationMapPlacement.replace(adventure, x, y, List.of(first, second));

        assertThat(y.getDirectionsData()).containsExactlyInAnyOrder(first, second);
    }
}
