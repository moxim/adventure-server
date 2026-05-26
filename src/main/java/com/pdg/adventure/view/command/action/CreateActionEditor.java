package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.action.CreateActionData;
import com.pdg.adventure.view.support.ViewSupporter;

/**
 * Editor component for CreateActionData.
 * Allows selecting an item to create and a location that will contain it.
 */
public class CreateActionEditor extends ActionEditorComponent {
    private final AdventureData adventureData;
    private final CreateActionData createActionData;
    private ComboBox<ItemData> itemSelector;
    private ComboBox<LocationData> containerSelector;

    public CreateActionEditor(CreateActionData actionData, AdventureData adventureData) {
        super(actionData);
        this.createActionData = actionData;
        this.adventureData = adventureData;
        // UI will be built when initialize() is called
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Create Action");
        Span description = new Span("Create an item at a specific location");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // Collect all items from all locations and player pocket
        List<ItemData> allItems = collectAllItems();

        itemSelector = new ComboBox<>("Item to Create");
        itemSelector.setItems(allItems);
        itemSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        itemSelector.setPlaceholder("Select item to create");
        itemSelector.setWidthFull();
        itemSelector.setRequired(true);

        // Pre-select if action already has an item
        if (createActionData.getThingId() != null) {
            ItemData item = findItemById(allItems, createActionData.getThingId());
            if (item != null) {
                itemSelector.setValue(item);
            }
        }

        // Update action data when item changes
        itemSelector.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                createActionData.setThingId(e.getValue().getId());
            } else {
                createActionData.setThingId(null);
            }
        });

        // Collect all locations
        List<LocationData> allLocations = new ArrayList<>(adventureData.getLocationData().values());

        containerSelector = new ComboBox<>("Container / Location");
        containerSelector.setItems(allLocations);
        containerSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        containerSelector.setPlaceholder("Select container / location");
        containerSelector.setWidthFull();
        containerSelector.setRequired(true);

        // Pre-select if action already has a container provider
        if (createActionData.getContainerProviderId() != null) {
            LocationData location = findLocationById(allLocations, createActionData.getContainerProviderId());
            if (location != null) {
                containerSelector.setValue(location);
            }
        }

        // Update action data when location changes
        containerSelector.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                createActionData.setContainerProviderId(e.getValue().getId());
            } else {
                createActionData.setContainerProviderId(null);
            }
        });

        add(title, description, itemSelector, containerSelector);
    }

    @Override
    public boolean validate() {
        boolean itemValid = itemSelector.getValue() != null;
        boolean containerValid = containerSelector.getValue() != null;

        if (!itemValid) {
            itemSelector.setErrorMessage("Please select an item to create");
            itemSelector.setInvalid(true);
        } else {
            itemSelector.setInvalid(false);
        }

        if (!containerValid) {
            containerSelector.setErrorMessage("Please select a container / location");
            containerSelector.setInvalid(true);
        } else {
            containerSelector.setInvalid(false);
        }

        return itemValid && containerValid;
    }

    /**
     * Collects all items from all locations in the adventure and from player's pocket.
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

    /**
     * Finds a location by its ID from a list of locations.
     */
    private LocationData findLocationById(List<LocationData> locations, String locationId) {
        return locations.stream()
                        .filter(location -> location.getId().equals(locationId))
                        .findFirst()
                        .orElse(null);
    }
}
