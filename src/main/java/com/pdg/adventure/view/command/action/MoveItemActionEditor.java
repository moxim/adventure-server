package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.action.MoveItemActionData;
import com.pdg.adventure.view.support.ViewSupporter;

@AutoRegisterActionEditor
public class MoveItemActionEditor extends ActionEditorComponent<MoveItemActionData> {
    private final AdventureData adventureData;
    private final MoveItemActionData moveActionData;
    private ComboBox<ItemData> itemSelector;
    private ComboBox<ItemContainerData> destinationSelector;

    public MoveItemActionEditor(MoveItemActionData actionData, AdventureData adventureData) {
        super(actionData);
        this.moveActionData = actionData;
        this.adventureData = adventureData;
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Move Item Action");
        Span description = new Span("Move an item to a different container");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        List<ItemData> allItems = ViewSupporter.collectAllItems(adventureData);

        itemSelector = new ComboBox<>("Item to Move");
        itemSelector.setItems(allItems);
        itemSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        itemSelector.setPlaceholder("Select item to move");
        itemSelector.setWidthFull();
        itemSelector.setRequired(true);

        if (moveActionData.getThingId() != null) {
            allItems.stream()
                    .filter(item -> item.getId().equals(moveActionData.getThingId()))
                    .findFirst()
                    .ifPresent(itemSelector::setValue);
        }

        itemSelector.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                moveActionData.setThingId(e.getValue().getId());
            } else {
                moveActionData.setThingId(null);
            }
        });

        List<ItemContainerData> allContainers = ViewSupporter.collectAllContainers(adventureData);

        destinationSelector = new ComboBox<>("Destination Container");
        destinationSelector.setItems(allContainers);
        destinationSelector.setItemLabelGenerator(this::formatContainerLabel);
        destinationSelector.setPlaceholder("Select destination container");
        destinationSelector.setWidthFull();
        destinationSelector.setRequired(true);

        if (moveActionData.getDestinationId() != null) {
            allContainers.stream()
                         .filter(c -> c.getId().equals(moveActionData.getDestinationId()))
                         .findFirst()
                         .ifPresent(destinationSelector::setValue);
        }

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

    @Override
    public String getActionSummary() {
        String item = (itemSelector != null && itemSelector.getValue() != null)
                ? ViewSupporter.formatDescription(itemSelector.getValue()) : "?";
        String dest = (destinationSelector != null && destinationSelector.getValue() != null)
                ? formatContainerLabel(destinationSelector.getValue()) : "?";
        return item + " @ " + dest;
    }

    private String formatContainerLabel(ItemContainerData container) {
        if (container == null) return "";
        if (adventureData.getPlayerPocket() != null &&
            container.getId().equals(adventureData.getPlayerPocket().getId())) {
            return "your pocket";
        }
        for (LocationData location : adventureData.getLocationData().values()) {
            if (location.getItemContainerData() != null &&
                location.getItemContainerData().getId().equals(container.getId())) {
                return ViewSupporter.formatDescription(location);
            }
        }
        return ViewSupporter.formatDescription(container);
    }
}
