package com.pdg.adventure.view.location;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.location.LocationMapPlacement.IncomingDirection;
import com.pdg.adventure.view.support.ViewSupporter;

/**
 * Takes a location off the world map. The location itself stays, as an unplaced location. The author ticks which of
 * its directions are removed with it, those leading away from it as well as those leading to it; nothing is ticked
 * at first, and directions that stay still lead to the location.
 */
public class RemoveLocationDialog extends Dialog {

    private final transient AdventureData adventure;
    private final transient AdventureService service;
    private final transient LocationData location;
    private final transient Runnable onDone;
    private final CheckboxGroup<DirectionData> outgoing = new CheckboxGroup<>();
    private final CheckboxGroup<IncomingDirection> incoming = new CheckboxGroup<>();

    public RemoveLocationDialog(AdventureData anAdventure, AdventureService aService, LocationData aLocation,
                                Runnable anOnDone) {
        adventure = anAdventure;
        service = aService;
        location = aLocation;
        onDone = anOnDone;

        setHeaderTitle("Remove " + name(location) + " from the map");

        VerticalLayout content = new VerticalLayout(new Paragraph(
                "The location stays in the adventure and can be placed again. Tick the directions that are removed "
                + "with it; the others stay."));
        content.setPadding(false);

        List<DirectionData> outgoingDirections = location.getDirectionsData().stream()
                .sorted(Comparator.comparing(RemoveLocationDialog::describe, String.CASE_INSENSITIVE_ORDER)).toList();
        List<IncomingDirection> incomingDirections = LocationMapPlacement.incomingDirections(adventure, location)
                .stream().sorted(Comparator.comparing(RemoveLocationDialog::describe, String.CASE_INSENSITIVE_ORDER))
                .toList();

        if (outgoingDirections.isEmpty() && incomingDirections.isEmpty()) {
            content.add(new Paragraph("This location has no directions."));
        }
        addSection(content, outgoing, "Directions leading away from here", outgoingDirections,
                   RemoveLocationDialog::describe);
        addSection(content, incoming, "Directions leading here", incomingDirections, RemoveLocationDialog::describe);
        add(content);

        getFooter().add(new Button("Cancel", _ -> close()), new Button("Remove", _ -> onRemove()));
    }

    private static <T> void addSection(VerticalLayout aContent, CheckboxGroup<T> aGroup, String aLabel,
                                       List<T> anItems, Function<T, String> aLabelGenerator) {
        if (anItems.isEmpty()) {
            return;
        }
        aGroup.setLabel(aLabel);
        aGroup.setItems(anItems);
        aGroup.setItemLabelGenerator(aLabelGenerator::apply);
        aGroup.setThemeName("vertical");
        Checkbox all = new Checkbox("Select all");
        all.addValueChangeListener(event -> {
            if (event.isFromClient()) {
                if (event.getValue()) {
                    aGroup.select(anItems);
                } else {
                    aGroup.deselectAll();
                }
            }
        });
        aContent.add(aGroup, all);
    }

    private void onRemove() {
        Collection<DirectionData> removed = new ArrayList<>(outgoing.getValue());
        incoming.getValue().forEach(entry -> removed.add(entry.direction()));
        // Not transactional: a failure part-way leaves the map half-updated (see the design document).
        LocationMapSaver.saveAll(service, LocationMapPlacement.remove(adventure, location, removed));
        close();
        onDone.run();
    }

    private static String name(LocationData aLocation) {
        return ViewSupporter.formatDescription(aLocation).strip();
    }

    private static String describe(DirectionData aDirection) {
        return ViewSupporter.formatDescription(aDirection.getCommandData().getCommandDescription());
    }

    private static String describe(IncomingDirection anIncoming) {
        return name(anIncoming.owner()) + ": " + describe(anIncoming.direction());
    }
}
