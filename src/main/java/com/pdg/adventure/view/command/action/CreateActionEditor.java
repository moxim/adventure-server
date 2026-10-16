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

@AutoRegisterActionEditor
public class CreateActionEditor extends ActionEditorComponent<CreateActionData> {
    private final AdventureData adventureData;
    private final CreateActionData createActionData;
    private ComboBox<ItemData> itemSelector;
    private ComboBox<LocationData> containerSelector;

    public CreateActionEditor(CreateActionData actionData, AdventureData adventureData) {
        super(actionData);
        this.createActionData = actionData;
        this.adventureData = adventureData;
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Create Action");
        Span description = new Span("Create an item at a specific location");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        List<ItemData> allItems = ViewSupporter.collectAllItems(adventureData);

        itemSelector = new ComboBox<>("Item to Create");
        itemSelector.setItems(allItems);
        itemSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        itemSelector.setPlaceholder("Select item to create");
        itemSelector.setWidthFull();
        itemSelector.setRequired(true);

        if (createActionData.getThingId() != null) {
            allItems.stream()
                    .filter(item -> item.getId().equals(createActionData.getThingId()))
                    .findFirst()
                    .ifPresent(itemSelector::setValue);
        }

        itemSelector.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                createActionData.setThingId(e.getValue().getId());
            } else {
                createActionData.setThingId(null);
            }
        });

        List<LocationData> allLocations = new ArrayList<>(adventureData.getLocationData().values());

        containerSelector = new ComboBox<>("Container / Location");
        containerSelector.setItems(allLocations);
        containerSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        containerSelector.setPlaceholder("Select container / location");
        containerSelector.setWidthFull();
        containerSelector.setRequired(true);

        if (createActionData.getContainerProviderId() != null) {
            allLocations.stream()
                        .filter(loc -> loc.getId().equals(createActionData.getContainerProviderId()))
                        .findFirst()
                        .ifPresent(containerSelector::setValue);
        }

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

    @Override
    public String getActionSummary() {
        String item = (itemSelector != null && itemSelector.getValue() != null)
                ? ViewSupporter.formatDescription(itemSelector.getValue()) : "?";
        String container = (containerSelector != null && containerSelector.getValue() != null)
                ? ViewSupporter.formatDescription(containerSelector.getValue()) : "?";
        return item + " @ " + container;
    }
}
