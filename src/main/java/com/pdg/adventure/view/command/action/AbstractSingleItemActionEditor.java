package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.view.support.ViewSupporter;

public abstract class AbstractSingleItemActionEditor<T extends ActionData>
        extends ActionEditorComponent<T> {

    protected final T typedAction;
    protected final AdventureData adventureData;

    private final String title;
    private final String descriptionText;
    private final String itemLabel;
    private final String itemPlaceholder;
    private final String itemErrorMessage;

    private ComboBox<ItemData> itemSelector;

    protected AbstractSingleItemActionEditor(
            T actionData, AdventureData adventureData,
            String title, String descriptionText,
            String itemLabel, String itemPlaceholder, String itemErrorMessage) {
        super(actionData);
        this.typedAction = actionData;
        this.adventureData = adventureData;
        this.title = title;
        this.descriptionText = descriptionText;
        this.itemLabel = itemLabel;
        this.itemPlaceholder = itemPlaceholder;
        this.itemErrorMessage = itemErrorMessage;
    }

    protected abstract String currentThingId();

    protected abstract void applyThingId(String id);

    @Override
    protected final void buildUI() {
        H4 heading = new H4(title);
        Span description = new Span(descriptionText);
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        List<ItemData> allItems = ViewSupporter.collectAllItems(adventureData);

        itemSelector = new ComboBox<>(itemLabel);
        itemSelector.setItems(allItems);
        itemSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        itemSelector.setPlaceholder(itemPlaceholder);
        itemSelector.setWidthFull();
        itemSelector.setRequired(true);

        if (currentThingId() != null) {
            allItems.stream()
                    .filter(item -> item.getId().equals(currentThingId()))
                    .findFirst()
                    .ifPresent(itemSelector::setValue);
        }

        itemSelector.addValueChangeListener(
                e -> applyThingId(e.getValue() != null ? e.getValue().getId() : null));

        add(heading, description, itemSelector);
    }

    @Override
    public final boolean validate() {
        boolean valid = itemSelector.getValue() != null;
        if (!valid) {
            itemSelector.setErrorMessage(itemErrorMessage);
            itemSelector.setInvalid(true);
        } else {
            itemSelector.setInvalid(false);
        }
        return valid;
    }

    @Override
    public final String getActionSummary() {
        if (itemSelector == null || itemSelector.getValue() == null) return "(none)";
        return ViewSupporter.formatDescription(itemSelector.getValue());
    }
}
