package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.action.WearActionData;
import com.pdg.adventure.view.support.ViewSupporter;

/**
 * Editor component for WearActionData.
 * Allows selecting a wearable item for the player to wear.
 * Since there is no Wearable marker on ItemData, all items are shown.
 */
public class WearActionEditor extends ActionEditorComponent {
    private final AdventureData adventureData;
    private final WearActionData wearActionData;
    private ComboBox<ItemData> itemSelector;

    public WearActionEditor(WearActionData actionData, AdventureData adventureData) {
        super(actionData);
        this.wearActionData = actionData;
        this.adventureData = adventureData;
        // UI will be built when initialize() is called
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Wear Action");
        Span description = new Span(
                "Select the wearable item the player will wear. Shows all items; ensure you select a wearable one.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        List<ItemData> allItems = collectAllItems();

        itemSelector = new ComboBox<>("Item to Wear");
        itemSelector.setItems(allItems);
        itemSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        itemSelector.setPlaceholder("Select item to wear");
        itemSelector.setWidthFull();
        itemSelector.setRequired(true);

        // Pre-select if action already has an item
        if (wearActionData.getThingId() != null) {
            ItemData item = findItemById(allItems, wearActionData.getThingId());
            if (item != null) {
                itemSelector.setValue(item);
            }
        }

        // Update action data when item changes
        itemSelector.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                wearActionData.setThingId(e.getValue().getId());
            } else {
                wearActionData.setThingId(null);
            }
        });

        add(title, description, itemSelector);
    }

    @Override
    public boolean validate() {
        boolean itemValid = itemSelector.getValue() != null;

        if (!itemValid) {
            itemSelector.setErrorMessage("Please select an item to wear");
            itemSelector.setInvalid(true);
        } else {
            itemSelector.setInvalid(false);
        }

        return itemValid;
    }

    /**
     * Collects all items from all locations and the player's pocket.
     */
    private List<ItemData> collectAllItems() {
        List<ItemData> allItems = new ArrayList<>();

        // Collect items from all locations
        for (LocationData location : adventureData.getLocationData().values()) {
            if (location.getItemContainerData() != null) {
                List<ItemData> items = location.getItemContainerData().getItems();
                if (items != null) {
                    allItems.addAll(items);
                }
            }
        }

        // Collect items from player's pocket
        if (adventureData.getPlayerPocket() != null) {
            List<ItemData> pocketItems = adventureData.getPlayerPocket().getItems();
            if (pocketItems != null) {
                allItems.addAll(pocketItems);
            }
        }

        return allItems;
    }

    /**
     * Finds an item by its ID from a list of items.
     */
    private ItemData findItemById(List<ItemData> items, String itemId) {
        return items.stream()
                    .filter(item -> item.getId().equals(itemId))
                    .findFirst()
                    .orElse(null);
    }
}
