package com.pdg.adventure.view.location;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.view.direction.DirectionBuilder;
import com.pdg.adventure.view.support.PictureGrid;
import com.pdg.adventure.view.support.ViewSupporter;

/**
 * The rules behind the world map: which location sits in a grid cell, which locations are still unplaced, which
 * directions have the same words, and what replacing a placed location by another one does to the directions.
 * Works on the data only and never saves; the caller saves the locations that were changed.
 */
public final class LocationMapPlacement {

    private LocationMapPlacement() {
    }

    /** The first location of the adventure that sits in the cell. */
    public static Optional<LocationData> locationAt(AdventureData anAdventure, PictureGrid.Cell aCell) {
        return anAdventure.getLocationData().values().stream()
                          .filter(location -> Objects.equals(location.getMapColumn(), aCell.column())
                                              && Objects.equals(location.getMapRow(), aCell.row()))
                          .findFirst();
    }

    /** The locations that have no cell yet, sorted by their description. */
    public static List<LocationData> unplaced(AdventureData anAdventure) {
        return anAdventure.getLocationData().values().stream()
                          .filter(location -> location.getMapColumn() == null || location.getMapRow() == null)
                          .sorted(Comparator.comparing(ViewSupporter::formatDescription,
                                                       String.CASE_INSENSITIVE_ORDER))
                          .toList();
    }

    public static void place(LocationData aLocation, PictureGrid.Cell aCell) {
        aLocation.setMapColumn(aCell.column());
        aLocation.setMapRow(aCell.row());
    }

    /** The directions of the source that lead to the target. */
    public static List<DirectionData> directionsTo(LocationData aSource, String aTargetId) {
        return aSource.getDirectionsData().stream()
                      .filter(direction -> aTargetId.equals(direction.getDestinationId())).toList();
    }

    /** A direction of the owner that is followed with the same verb, adjective and noun. */
    public static Optional<DirectionData> findSameWords(LocationData anOwner, Word aVerb, Word anAdjective,
                                                        Word aNoun) {
        return anOwner.getDirectionsData().stream()
                      .filter(direction -> sameWords(direction, aVerb, anAdjective, aNoun)).findFirst();
    }

    /** The directions of Y that lead to X: after a replace they would only loop on Y, so they are dropped. */
    public static List<DirectionData> directionsToDrop(LocationData anX, LocationData aY) {
        return directionsTo(aY, anX.getId());
    }

    /** The directions of X that lead to Y: after a replace they would only loop on Y, so they cannot be moved. */
    public static List<DirectionData> unmovable(LocationData anX, LocationData aY) {
        return directionsTo(anX, aY.getId());
    }

    /** The directions of Y that have the same words as one of the moved directions. */
    public static List<DirectionData> clashes(LocationData aY, Collection<DirectionData> aMovedDirections) {
        return aY.getDirectionsData().stream()
                 .filter(direction -> aMovedDirections.stream().anyMatch(moved -> sameWords(direction, moved)))
                 .toList();
    }

    /**
     * Y takes over the cell of X: every direction that led to X leads to Y, Y's own directions to X are dropped and
     * the given directions of X (except those that lead to Y) move to Y, replacing Y's directions with the same words.
     * Returns the locations that changed, each once, for the caller to save.
     */
    public static List<LocationData> replace(AdventureData anAdventure, LocationData anX, LocationData aY,
                                             Collection<DirectionData> aMovedDirections) {
        final Set<LocationData> changed = new LinkedHashSet<>();
        final List<DirectionData> dropped = directionsToDrop(anX, aY);
        final List<DirectionData> unmovable = unmovable(anX, aY);
        final List<DirectionData> toMove = aMovedDirections.stream().filter(direction -> !unmovable.contains(direction))
                                                          .toList();

        if (!dropped.isEmpty()) {
            aY.getDirectionsData().removeAll(dropped);
            changed.add(aY);
        }

        aY.setMapColumn(anX.getMapColumn());
        aY.setMapRow(anX.getMapRow());
        anX.setMapColumn(null);
        anX.setMapRow(null);
        changed.add(anX);
        changed.add(aY);

        // TODO: Review needed — a direction of X that leads to X is re-pointed to Y; if it is moved, it loops on Y
        for (LocationData owner : anAdventure.getLocationData().values()) {
            for (DirectionData direction : List.copyOf(owner.getDirectionsData())) {
                if (anX.getId().equals(direction.getDestinationId())) {
                    direction.setDestinationId(aY.getId());
                    DirectionBuilder.attach(direction, owner);
                    changed.add(owner);
                }
            }
        }

        // Only what Y had before replaces anything; moved directions that share their words all stay.
        aY.getDirectionsData().removeAll(clashes(aY, toMove));
        for (DirectionData direction : toMove) {
            anX.getDirectionsData().remove(direction);
            DirectionBuilder.attach(direction, aY);
        }
        return List.copyOf(changed);
    }

    private static boolean sameWords(DirectionData aDirection, DirectionData anOther) {
        final CommandDescriptionData words = anOther.getCommandData().getCommandDescription();
        return sameWords(aDirection, words.getVerb(), words.getAdjective(), words.getNoun());
    }

    private static boolean sameWords(DirectionData aDirection, Word aVerb, Word anAdjective, Word aNoun) {
        final CommandDescriptionData words = aDirection.getCommandData().getCommandDescription();
        return Objects.equals(words.getVerb(), aVerb) && Objects.equals(words.getAdjective(), anAdjective)
               && Objects.equals(words.getNoun(), aNoun);
    }
}
