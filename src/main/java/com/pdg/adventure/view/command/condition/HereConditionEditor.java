package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.combobox.ComboBox;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.condition.HereConditionData;
import com.pdg.adventure.view.support.ViewSupporter;

public class HereConditionEditor extends ConditionEditorComponent {
    private final AdventureData adventureData;
    private final HereConditionData hereData;
    private ComboBox<ItemData> itemSelector;

    public HereConditionEditor(HereConditionData conditionData, AdventureData adventureData) {
        super(conditionData);
        this.hereData = conditionData;
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

        if (hereData.getThingId() != null) {
            items.stream().filter(i -> i.getId().equals(hereData.getThingId()))
                 .findFirst().ifPresent(itemSelector::setValue);
        }
        itemSelector.addValueChangeListener(e ->
            hereData.setThingId(e.getValue() != null ? e.getValue().getId() : null));

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
