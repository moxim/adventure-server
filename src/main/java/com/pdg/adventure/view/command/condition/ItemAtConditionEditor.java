package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.combobox.ComboBox;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.condition.ItemAtConditionData;
import com.pdg.adventure.view.support.ViewSupporter;

@AutoRegisterConditionEditor
public class ItemAtConditionEditor extends ConditionEditorComponent<ItemAtConditionData> {
    private final AdventureData adventureData;
    private final ItemAtConditionData itemAtData;
    private ComboBox<ItemData> itemSelector;
    private ComboBox<LocationData> locationSelector;

    public ItemAtConditionEditor(ItemAtConditionData conditionData, AdventureData adventureData) {
        super(conditionData);
        this.itemAtData = conditionData;
        this.adventureData = adventureData;
    }

    @Override
    protected void buildUI() {
        List<ItemData> items = ViewSupporter.collectAllItems(adventureData);
        itemSelector = new ComboBox<>("Item");
        itemSelector.setItems(items);
        itemSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        itemSelector.setPlaceholder("Select item");
        itemSelector.setWidthFull();
        itemSelector.setRequired(true);

        List<LocationData> locations = ViewSupporter.collectAllLocations(adventureData);
        locationSelector = new ComboBox<>("Location");
        locationSelector.setItems(locations);
        locationSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        locationSelector.setPlaceholder("Select location");
        locationSelector.setWidthFull();
        locationSelector.setRequired(true);

        if (itemAtData.getThingId() != null) {
            items.stream().filter(i -> i.getId().equals(itemAtData.getThingId()))
                 .findFirst().ifPresent(itemSelector::setValue);
        }
        if (itemAtData.getLocationId() != null) {
            locations.stream().filter(l -> l.getId().equals(itemAtData.getLocationId()))
                     .findFirst().ifPresent(locationSelector::setValue);
        }

        itemSelector.addValueChangeListener(e ->
            itemAtData.setThingId(e.getValue() != null ? e.getValue().getId() : null));
        locationSelector.addValueChangeListener(e ->
            itemAtData.setLocationId(e.getValue() != null ? e.getValue().getId() : null));

        add(itemSelector, locationSelector);
    }

    @Override
    public boolean validate() {
        boolean itemValid = itemSelector.getValue() != null;
        boolean locValid = locationSelector.getValue() != null;
        itemSelector.setInvalid(!itemValid);
        locationSelector.setInvalid(!locValid);
        if (!itemValid) itemSelector.setErrorMessage("Please select an item");
        if (!locValid) locationSelector.setErrorMessage("Please select a location");
        return itemValid && locValid;
    }

    @Override
    public String getConditionSummary() {
        String item = (itemSelector != null && itemSelector.getValue() != null)
                ? ViewSupporter.formatDescription(itemSelector.getValue()) : "?";
        String loc = (locationSelector != null && locationSelector.getValue() != null)
                ? ViewSupporter.formatDescription(locationSelector.getValue()) : "?";
        return item + " @ " + loc;
    }
}
