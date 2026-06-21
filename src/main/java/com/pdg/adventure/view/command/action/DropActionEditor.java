package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.action.DropActionData;
import com.pdg.adventure.view.support.ViewSupporter;

public class DropActionEditor extends ActionEditorComponent {
    private final AdventureData adventureData;
    private final DropActionData dropActionData;
    private ComboBox<ItemData> itemSelector;

    public DropActionEditor(DropActionData actionData, AdventureData adventureData) {
        super(actionData);
        this.dropActionData = actionData;
        this.adventureData = adventureData;
    }

    @Override
    protected void buildUI() {
        List<ItemData> allItems = collectAllItems();

        itemSelector = new ComboBox<>("Item to Drop");
        itemSelector.setItems(allItems);
        itemSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        itemSelector.setPlaceholder("Select item to drop");
        itemSelector.setWidthFull();
        itemSelector.setRequired(true);

        if (dropActionData.getThingId() != null) {
            allItems.stream()
                    .filter(item -> item.getId().equals(dropActionData.getThingId()))
                    .findFirst()
                    .ifPresent(itemSelector::setValue);
        }

        itemSelector.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                dropActionData.setThingId(e.getValue().getId());
            } else {
                dropActionData.setThingId(null);
            }
        });

        add(new H4("Drop Action"), new Span("Select the item the player will drop."), itemSelector);
    }

    @Override
    public boolean validate() {
        boolean valid = itemSelector.getValue() != null;
        if (!valid) {
            itemSelector.setErrorMessage("Please select an item to drop");
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
