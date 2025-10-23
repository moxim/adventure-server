package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.action.MovePlayerActionData;
import com.pdg.adventure.view.support.ViewSupporter;

/**
 * Editor component for MovePlayerActionData.
 * Allows selecting a destination location where the player will be moved to.
 */
public class MovePlayerActionEditor extends ActionEditorComponent {
    private final AdventureData adventureData;
    private final MovePlayerActionData moveActionData;
    private ComboBox<LocationData> locationSelector;

    public MovePlayerActionEditor(MovePlayerActionData actionData, AdventureData adventureData) {
        super(actionData);
        this.moveActionData = actionData;
        this.adventureData = adventureData;
        // UI will be built when initialize() is called
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Move Player Action");
        Span description = new Span("Move the player to a different location");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        locationSelector = new ComboBox<>("Destination Location");
        locationSelector.setItems(adventureData.getLocationData().values());
        locationSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        locationSelector.setPlaceholder("Select destination location");
        locationSelector.setWidthFull();
        locationSelector.setRequired(true);

        // Pre-select if action already has a location
        if (moveActionData.getLocationId() != null) {
            LocationData location = adventureData.getLocationData().get(moveActionData.getLocationId());
            if (location != null) {
                locationSelector.setValue(location);
            }
        }

        // Update action data when location changes
        locationSelector.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                moveActionData.setLocationId(e.getValue().getId());
            } else {
                moveActionData.setLocationId(null);
            }
        });

        add(title, description, locationSelector);
    }

    @Override
    public boolean validate() {
        boolean isValid = locationSelector.getValue() != null;
        if (!isValid) {
            locationSelector.setErrorMessage("Please select a destination location");
            locationSelector.setInvalid(true);
        } else {
            locationSelector.setInvalid(false);
        }
        return isValid;
    }
}
