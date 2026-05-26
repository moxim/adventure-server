package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.action.TakeActionData;
import com.pdg.adventure.view.support.ViewSupporter;

public class TakeActionEditor extends ActionEditorComponent {
    private final AdventureData adventureData;
    private final TakeActionData takeActionData;
    private ComboBox<ItemData> itemSelector;

    public TakeActionEditor(TakeActionData actionData, AdventureData adventureData) {
        super(actionData);
        this.takeActionData = actionData;
        this.adventureData = adventureData;
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Take Action");
        Span description = new Span("Select the item the player will pick up.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        List<ItemData> allItems = collectAllItems();

        itemSelector = new ComboBox<>("Item to Take");
        itemSelector.setItems(allItems);
        itemSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        itemSelector.setPlaceholder("Select item to take");
        itemSelector.setWidthFull();
        itemSelector.setRequired(true);

        if (takeActionData.getThingId() != null) {
            allItems.stream()
                    .filter(item -> item.getId().equals(takeActionData.getThingId()))
                    .findFirst()
                    .ifPresent(itemSelector::setValue);
        }

        itemSelector.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                takeActionData.setThingId(e.getValue().getId());
            } else {
                takeActionData.setThingId(null);
            }
        });

        add(title, description, itemSelector);
    }

    @Override
    public boolean validate() {
        boolean valid = itemSelector.getValue() != null;
        if (!valid) {
            itemSelector.setErrorMessage("Please select an item to take");
            itemSelector.setInvalid(true);
        } else {
            itemSelector.setInvalid(false);
        }
        return valid;
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
