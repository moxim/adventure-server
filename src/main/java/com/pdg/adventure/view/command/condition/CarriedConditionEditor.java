package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.combobox.ComboBox;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.condition.CarriedConditionData;
import com.pdg.adventure.view.support.ViewSupporter;

public class CarriedConditionEditor extends ConditionEditorComponent {
    private final AdventureData adventureData;
    private final CarriedConditionData carriedData;
    private ComboBox<ItemData> itemSelector;

    public CarriedConditionEditor(CarriedConditionData conditionData, AdventureData adventureData) {
        super(conditionData);
        this.carriedData = conditionData;
        this.adventureData = adventureData;
    }

    @Override
    protected void buildUI() {
        List<ItemData> items = ConditionEditorSupport.allItems(adventureData);
        itemSelector = new ComboBox<>("Item");
        itemSelector.setItems(items);
        itemSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        itemSelector.setPlaceholder("Select item");
        itemSelector.setWidthFull();
        itemSelector.setRequired(true);

        if (carriedData.getItemId() != null) {
            items.stream().filter(i -> i.getId().equals(carriedData.getItemId()))
                 .findFirst().ifPresent(itemSelector::setValue);
        }
        itemSelector.addValueChangeListener(e ->
            carriedData.setItemId(e.getValue() != null ? e.getValue().getId() : null));

        add(itemSelector);
    }

    @Override
    public boolean validate() {
        boolean valid = itemSelector.getValue() != null;
        itemSelector.setInvalid(!valid);
        if (!valid) itemSelector.setErrorMessage("Please select an item");
        return valid;
    }

    @Override
    public String getConditionSummary() {
        if (itemSelector == null || itemSelector.getValue() == null) return "(none)";
        return ViewSupporter.formatDescription(itemSelector.getValue());
    }
}
