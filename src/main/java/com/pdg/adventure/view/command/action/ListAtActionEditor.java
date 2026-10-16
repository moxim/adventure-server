package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.action.ListAtActionData;
import com.pdg.adventure.view.support.ViewSupporter;

/**
 * Editor component for ListAtActionData: the author picks a location or the player's pocket, and the game prints
 * the items in it.
 */
@AutoRegisterActionEditor
public class ListAtActionEditor extends ActionEditorComponent<ListAtActionData> {
    private static final String POCKET_LABEL = "your pocket";

    private final AdventureData adventureData;
    private final ListAtActionData listAtActionData;
    private ComboBox<ItemContainerData> containerSelector;

    public ListAtActionEditor(ListAtActionData actionData, AdventureData adventureData) {
        super(actionData);
        this.listAtActionData = actionData;
        this.adventureData = adventureData;
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("ListAt Action");
        Span description = new Span("Print the items in a location or in your pocket. A heading such as "
                                    + "\"I have with me:\" is not added; put a Message in front for that.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        List<ItemContainerData> choices = choices();

        containerSelector = new ComboBox<>("Location or Pocket");
        containerSelector.setItems(choices);
        containerSelector.setItemLabelGenerator(this::label);
        containerSelector.setPlaceholder("Select a location or your pocket");
        containerSelector.setWidthFull();
        containerSelector.setRequired(true);

        if (listAtActionData.getContainerId() != null) {
            choices.stream()
                   .filter(c -> c.getId().equals(listAtActionData.getContainerId()))
                   .findFirst()
                   .ifPresent(containerSelector::setValue);
        }

        containerSelector.addValueChangeListener(e ->
                listAtActionData.setContainerId(e.getValue() == null ? null : e.getValue().getId()));

        add(title, description, containerSelector);
    }

    /** The pocket first, then the item container of every location. */
    private List<ItemContainerData> choices() {
        List<ItemContainerData> choices = new ArrayList<>();
        if (adventureData.getPlayerPocket() != null) {
            choices.add(adventureData.getPlayerPocket());
        }
        for (LocationData location : adventureData.getLocationData().values()) {
            if (location.getItemContainerData() != null) {
                choices.add(location.getItemContainerData());
            }
        }
        return choices;
    }

    @Override
    public boolean validate() {
        boolean valid = containerSelector.getValue() != null;
        if (!valid) {
            containerSelector.setErrorMessage("Please select a location or your pocket");
        }
        containerSelector.setInvalid(!valid);
        return valid;
    }

    @Override
    public String getActionSummary() {
        ItemContainerData selected = containerSelector == null ? null : containerSelector.getValue();
        return selected == null ? "(none)" : label(selected);
    }

    private String label(ItemContainerData aContainer) {
        if (adventureData.getPlayerPocket() != null
            && aContainer.getId().equals(adventureData.getPlayerPocket().getId())) {
            return POCKET_LABEL;
        }
        for (LocationData location : adventureData.getLocationData().values()) {
            if (location.getItemContainerData() != null
                && location.getItemContainerData().getId().equals(aContainer.getId())) {
                return ViewSupporter.formatDescription(location);
            }
        }
        return ViewSupporter.formatDescription(aContainer);
    }
}
