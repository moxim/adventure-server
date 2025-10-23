package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.action.MoveItemActionData;
import com.pdg.adventure.view.support.ViewSupporter;

/**
 * Editor component for MoveItemActionData.
 * Allows selecting an item to move and a destination container (location or player's pocket) where it will be moved to.
 */
public class MoveItemActionEditor extends ActionEditorComponent {
    private final AdventureData adventureData;
    private final MoveItemActionData moveActionData;
    private ComboBox<ItemData> itemSelector;
    private ComboBox<ItemContainerData> destinationSelector;

    public MoveItemActionEditor(MoveItemActionData actionData, AdventureData adventureData) {
        super(actionData);
        this.moveActionData = actionData;
        this.adventureData = adventureData;
        // UI will be built when initialize() is called
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Move Item Action");
        Span description = new Span("Move an item to a different container");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        // Collect all items from all locations
        List<ItemData> allItems = collectAllItems();

        itemSelector = new ComboBox<>("Item to Move");
        itemSelector.setItems(allItems);
        itemSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        itemSelector.setPlaceholder("Select item to move");
        itemSelector.setWidthFull();
        itemSelector.setRequired(true);

        // Pre-select if action already has an item
        if (moveActionData.getThingId() != null) {
            ItemData item = findItemById(allItems, moveActionData.getThingId());
            if (item != null) {
                itemSelector.setValue(item);
            }
        }

        // Update action data when item changes
        itemSelector.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                moveActionData.setThingId(e.getValue().getId());
            } else {
                moveActionData.setThingId(null);
            }
        });

        // Collect all item containers (locations + player pocket)
        List<ItemContainerData> allContainers = collectAllContainers();

        destinationSelector = new ComboBox<>("Destination Container");
        destinationSelector.setItems(allContainers);
        destinationSelector.setItemLabelGenerator(this::formatContainerLabel);
        destinationSelector.setPlaceholder("Select destination container");
        destinationSelector.setWidthFull();
        destinationSelector.setRequired(true);

        // Pre-select if action already has a destination
        if (moveActionData.getDestinationId() != null) {
            ItemContainerData container = findContainerById(allContainers, moveActionData.getDestinationId());
            if (container != null) {
                destinationSelector.setValue(container);
            }
        }

        // Update action data when destination changes
        destinationSelector.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                moveActionData.setDestinationId(e.getValue().getId());
            } else {
                moveActionData.setDestinationId(null);
            }
        });

        add(title, description, itemSelector, destinationSelector);
    }

    @Override
    public boolean validate() {
        boolean itemValid = itemSelector.getValue() != null;
        boolean destinationValid = destinationSelector.getValue() != null;

        if (!itemValid) {
            itemSelector.setErrorMessage("Please select an item to move");
            itemSelector.setInvalid(true);
        } else {
            itemSelector.setInvalid(false);
        }

        if (!destinationValid) {
            destinationSelector.setErrorMessage("Please select a destination container");
            destinationSelector.setInvalid(true);
        } else {
            destinationSelector.setInvalid(false);
        }

        return itemValid && destinationValid;
    }

    /**
     * Collects all items from all locations in the adventure.
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
     * Collects all item containers from locations and player pocket.
     */
    private List<ItemContainerData> collectAllContainers() {
        List<ItemContainerData> allContainers = new ArrayList<>();

        // Add player's pocket
        if (adventureData.getPlayerPocket() != null) {
            allContainers.add(adventureData.getPlayerPocket());
        }

        // Add item containers from all locations
        for (LocationData location : adventureData.getLocationData().values()) {
            if (location.getItemContainerData() != null) {
                allContainers.add(location.getItemContainerData());
            }
        }

        return allContainers;
    }

    /**
     * Formats a label for an item container, showing either "Player's Pocket" or the location name.
     */
    private String formatContainerLabel(ItemContainerData container) {
        if (container == null) {
            return "";
        }

        // Check if this is the player's pocket
        if (adventureData.getPlayerPocket() != null &&
            container.getId().equals(adventureData.getPlayerPocket().getId())) {
            return "Player's Pocket";
        }

        // Find the location that contains this container
        for (LocationData location : adventureData.getLocationData().values()) {
            if (location.getItemContainerData() != null &&
                location.getItemContainerData().getId().equals(container.getId())) {
                return ViewSupporter.formatDescription(location);
            }
        }

        // Fallback to container's own description
        return ViewSupporter.formatDescription(container);
    }

    /**
     * Finds a container by its ID from a list of containers.
     */
    private ItemContainerData findContainerById(List<ItemContainerData> containers, String containerId) {
        return containers.stream()
                .filter(container -> container.getId().equals(containerId))
                .findFirst()
                .orElse(null);
    }
}
