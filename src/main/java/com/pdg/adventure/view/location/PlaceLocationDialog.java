package com.pdg.adventure.view.location;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;

import java.util.List;

import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.support.PictureGrid;
import com.pdg.adventure.view.support.ViewSupporter;

/** Lets the author choose which of the unplaced locations goes into an empty cell of the world map. */
public class PlaceLocationDialog extends Dialog {

    public PlaceLocationDialog(AdventureService aService, List<LocationData> anUnplacedLocations,
                               PictureGrid.Cell aCell, Runnable anOnDone) {
        setHeaderTitle("Place a location at " + aCell.column() + " : " + aCell.row());

        Grid<LocationData> grid = new Grid<>();
        grid.addColumn(ViewSupporter::formatDescription).setHeader("Short Description").setAutoWidth(true);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setItems(anUnplacedLocations);
        add(grid);

        Button place = new Button("Place", _ -> {
            grid.getSelectedItems().stream().findFirst().ifPresent(location -> {
                LocationMapPlacement.place(location, aCell);
                LocationMapSaver.saveAll(aService, List.of(location));
                close();
                anOnDone.run();
            });
        });
        place.setEnabled(false);
        grid.addSelectionListener(event -> place.setEnabled(event.getFirstSelectedItem().isPresent()));
        Button cancel = new Button("Cancel", _ -> close());
        getFooter().add(cancel, place);
    }
}
