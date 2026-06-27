package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.combobox.ComboBox;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.condition.PlayerAtConditionData;
import com.pdg.adventure.view.support.ViewSupporter;

public class PlayerAtConditionEditor extends ConditionEditorComponent {
    private final AdventureData adventureData;
    private final PlayerAtConditionData playerAtData;
    private ComboBox<LocationData> locationSelector;

    public PlayerAtConditionEditor(PlayerAtConditionData conditionData, AdventureData adventureData) {
        super(conditionData);
        this.playerAtData = conditionData;
        this.adventureData = adventureData;
    }

    @Override
    protected void buildUI() {
        List<LocationData> locations = ViewSupporter.collectAllLocations(adventureData);
        locationSelector = new ComboBox<>("Location");
        locationSelector.setItems(locations);
        locationSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        locationSelector.setPlaceholder("Select location");
        locationSelector.setWidthFull();
        locationSelector.setRequired(true);

        if (playerAtData.getLocationId() != null) {
            locations.stream().filter(l -> l.getId().equals(playerAtData.getLocationId()))
                     .findFirst().ifPresent(locationSelector::setValue);
        }
        locationSelector.addValueChangeListener(e ->
            playerAtData.setLocationId(e.getValue() != null ? e.getValue().getId() : null));

        add(locationSelector);
    }

    @Override
    public boolean validate() {
        boolean valid = locationSelector.getValue() != null;
        locationSelector.setInvalid(!valid);
        if (!valid) locationSelector.setErrorMessage("Please select a location");
        return valid;
    }

    @Override
    public String getConditionSummary() {
        if (locationSelector == null || locationSelector.getValue() == null) return "(none)";
        return ViewSupporter.formatDescription(locationSelector.getValue());
    }
}
