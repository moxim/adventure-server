package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.combobox.ComboBox;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.condition.WornConditionData;
import com.pdg.adventure.view.support.ViewSupporter;

public class WornConditionEditor extends ConditionEditorComponent {
    private final AdventureData adventureData;
    private final WornConditionData wornData;
    private ComboBox<ItemData> itemSelector;

    public WornConditionEditor(WornConditionData conditionData, AdventureData adventureData) {
        super(conditionData);
        this.wornData = conditionData;
        this.adventureData = adventureData;
    }

    @Override
    protected void buildUI() {
        List<ItemData> items = ConditionEditorSupport.wearableItems(adventureData);
        itemSelector = new ComboBox<>("Wearable Item");
        itemSelector.setItems(items);
        itemSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        itemSelector.setPlaceholder("Select wearable item");
        itemSelector.setWidthFull();
        itemSelector.setRequired(true);

        if (wornData.getThingId() != null) {
            items.stream().filter(i -> i.getId().equals(wornData.getThingId()))
                 .findFirst().ifPresent(itemSelector::setValue);
        }
        itemSelector.addValueChangeListener(e ->
            wornData.setThingId(e.getValue() != null ? e.getValue().getId() : null));

        add(itemSelector);
    }

    @Override
    public boolean validate() {
        boolean valid = itemSelector.getValue() != null;
        itemSelector.setInvalid(!valid);
        if (!valid) itemSelector.setErrorMessage("Please select a wearable item");
        return valid;
    }

    @Override
    public String getConditionSummary() {
        if (itemSelector == null || itemSelector.getValue() == null) return "(none)";
        return ViewSupporter.formatDescription(itemSelector.getValue());
    }
}
