package com.pdg.adventure.view.location;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.support.ViewSupporter;

/**
 * Replaces the location in a cell of the world map by one of the unplaced locations. Directions that led to the old
 * location lead to the new one; the author ticks which of the old location's own directions move to the new one.
 */
public class ReplaceLocationDialog extends Dialog {

    private final transient AdventureData adventure;
    private final transient AdventureService service;
    private final transient LocationData oldLocation;
    private final transient Runnable onDone;
    private final Grid<LocationData> unplacedGrid = new Grid<>();
    private final Grid<DirectionData> directionGrid = new Grid<>();
    private final Paragraph note = new Paragraph();
    private final Button replace = new Button("Replace", _ -> onReplace());

    public ReplaceLocationDialog(AdventureData anAdventure, AdventureService aService, LocationData anOldLocation,
                                 Runnable anOnDone) {
        adventure = anAdventure;
        service = aService;
        oldLocation = anOldLocation;
        onDone = anOnDone;

        setHeaderTitle("Replace " + ViewSupporter.formatDescription(oldLocation));

        Paragraph intro = new Paragraph("Choose the unplaced location that takes this cell. Directions that lead here "
                                        + "will lead to the new location.");
        unplacedGrid.addColumn(ViewSupporter::formatDescription).setHeader("New location").setAutoWidth(true);
        unplacedGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        unplacedGrid.setItems(LocationMapPlacement.unplaced(adventure));
        unplacedGrid.addSelectionListener(event -> onNewLocationChosen(event.getFirstSelectedItem()));

        directionGrid.addColumn(ReplaceLocationDialog::describe)
                     .setHeader("Directions to move to the new location").setAutoWidth(true);
        directionGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        directionGrid.setItems(sorted(oldLocation.getDirectionsData()));

        VerticalLayout content = new VerticalLayout(intro, unplacedGrid, directionGrid, note);
        content.setPadding(false);
        add(content);

        replace.setEnabled(false);
        getFooter().add(new Button("Cancel", _ -> close()), replace);
    }

    private void onNewLocationChosen(Optional<LocationData> aNewLocation) {
        replace.setEnabled(aNewLocation.isPresent());
        if (aNewLocation.isEmpty()) {
            note.setText("");
            return;
        }
        LocationData newLocation = aNewLocation.get();
        List<DirectionData> unmovable = LocationMapPlacement.unmovable(oldLocation, newLocation);
        // setItems clears the selection, so what the author already ticked is ticked again
        Set<DirectionData> ticked = new HashSet<>(directionGrid.getSelectedItems());
        List<DirectionData> movable = sorted(oldLocation.getDirectionsData().stream()
                                                        .filter(direction -> !unmovable.contains(direction)).toList());
        directionGrid.setItems(movable);
        movable.stream().filter(ticked::contains).forEach(directionGrid::select);

        List<String> sentences = new ArrayList<>();
        List<DirectionData> dropped = LocationMapPlacement.directionsToDrop(oldLocation, newLocation);
        if (!dropped.isEmpty()) {
            sentences.add("Dropped, because they would only lead back to the new location: " + describe(dropped) + ".");
        }
        if (!unmovable.isEmpty()) {
            sentences.add("Not moved, because they lead to the new location: " + describe(unmovable) + ".");
        }
        note.setText(String.join(" ", sentences));
    }

    private void onReplace() {
        Optional<LocationData> selected = unplacedGrid.getSelectedItems().stream().findFirst();
        if (selected.isEmpty()) {
            return;
        }
        LocationData newLocation = selected.get();
        List<DirectionData> moved = new ArrayList<>(directionGrid.getSelectedItems());
        List<DirectionData> clashes = LocationMapPlacement.clashes(newLocation, moved);
        if (clashes.isEmpty()) {
            apply(newLocation, moved);
            return;
        }
        ConfirmDialog confirm = new ConfirmDialog();
        confirm.setHeader("Same words");
        confirm.setText("The new location already has " + describe(clashes)
                        + ". It is replaced by the direction moved from the old location.");
        confirm.setCancelable(true);
        confirm.setConfirmText("Replace");
        confirm.addConfirmListener(_ -> apply(newLocation, moved));
        confirm.open();
    }

    private void apply(LocationData aNewLocation, Collection<DirectionData> aMovedDirections) {
        // Not transactional: a failure part-way leaves the map half-updated (see the design document).
        LocationMapSaver.saveAll(service, LocationMapPlacement.replace(adventure, oldLocation, aNewLocation,
                                                                       aMovedDirections));
        close();
        onDone.run();
    }

    private static List<DirectionData> sorted(Collection<DirectionData> aDirections) {
        return aDirections.stream().sorted(Comparator.comparing(ReplaceLocationDialog::describe,
                                                                String.CASE_INSENSITIVE_ORDER)).toList();
    }

    private static String describe(DirectionData aDirection) {
        return ViewSupporter.formatDescription(aDirection.getCommandData().getCommandDescription());
    }

    private static String describe(Collection<DirectionData> aDirections) {
        return aDirections.stream().map(ReplaceLocationDialog::describe).collect(Collectors.joining(", "));
    }
}
