package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.action.DestroyActionData;
import com.pdg.adventure.view.support.ViewSupporter;

public class DestroyActionEditor extends ActionEditorComponent {
    private final AdventureData adventureData;
    private final DestroyActionData destroyActionData;
    private ComboBox<ItemData> itemSelector;

    public DestroyActionEditor(DestroyActionData actionData, AdventureData adventureData) {
        super(actionData);
        this.destroyActionData = actionData;
        this.adventureData = adventureData;
    }

    @Override
    protected void buildUI() {
        List<ItemData> allItems = collectAllItems();

        itemSelector = new ComboBox<>("Item to Destroy");
        itemSelector.setItems(allItems);
        itemSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        itemSelector.setPlaceholder("Select item to destroy");
        itemSelector.setWidthFull();
        itemSelector.setRequired(true);

        if (destroyActionData.getThingId() != null) {
            allItems.stream()
                    .filter(item -> item.getId().equals(destroyActionData.getThingId()))
                    .findFirst()
                    .ifPresent(itemSelector::setValue);
        }

        itemSelector.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                destroyActionData.setThingId(e.getValue().getId());
            } else {
                destroyActionData.setThingId(null);
            }
        });

        add(itemSelector);
    }

    @Override
    public boolean validate() {
        boolean valid = itemSelector.getValue() != null;
        if (!valid) {
            itemSelector.setErrorMessage("Please select an item to destroy");
            itemSelector.setInvalid(true);
        } else {
            itemSelector.setInvalid(false);
        }
        return valid;
    }

    @Override
    public String getActionSummary() {
        if (itemSelector == null || itemSelector.getValue() == null) return "(none)";
        return ViewSupporter.formatDescription(itemSelector.getValue());
    }

    private List<ItemData> collectAllItems() {
        List<ItemData> allItems = new ArrayList<>();

        for (LocationData location : adventureData.getLocationData().values()) {
            if (location.getItemContainerData() != null) {
                List<ItemData> items = location.getItemContainerData().getItems();
                if (items != null) {
                    allItems.addAll(items);
                }
            }
        }

        if (adventureData.getPlayerPocket() != null) {
            List<ItemData> pocketItems = adventureData.getPlayerPocket().getItems();
            if (pocketItems != null) {
                allItems.addAll(pocketItems);
            }
        }

        return allItems;
    }
}
