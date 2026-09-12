package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.IntegerField;

import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.action.LightActionData;
import com.pdg.adventure.view.support.ViewSupporter;

/**
 * Editor component for LightActionData.
 * Allows selecting an item and the lumen value its light level should be set to.
 */
@AutoRegisterActionEditor
public class LightActionEditor extends ActionEditorComponent<LightActionData> {

    private static final int MIN_LUMEN = 0;
    private static final int MAX_LUMEN = 100;
    private static final int LUMEN_STEP = 1;

    private final AdventureData adventureData;

    private ComboBox<ItemData> itemSelector;
    private IntegerField lumenField;

    public LightActionEditor(LightActionData actionData, AdventureData adventureData) {
        super(actionData);
        this.adventureData = adventureData;
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Light Action");
        Span description = new Span("Set an item's light level (lumen) to the given value.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        List<ItemData> allItems = ViewSupporter.collectAllItems(adventureData);

        itemSelector = new ComboBox<>("Item");
        itemSelector.setItems(allItems);
        itemSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        itemSelector.setPlaceholder("Select the item to light");
        itemSelector.setWidthFull();
        itemSelector.setRequired(true);

        lumenField = new IntegerField("Lumen");
        lumenField.setMin(MIN_LUMEN);
        lumenField.setMax(MAX_LUMEN);
        lumenField.setStep(LUMEN_STEP);
        lumenField.setPlaceholder("Enter the new lumen value");
        lumenField.setTooltipText(
                "The item's new light level. (" + MAX_LUMEN + " = max, " + MIN_LUMEN + " = none)");
        lumenField.setWidthFull();
        lumenField.setRequired(true);

        if (actionData.getThingId() != null) {
            allItems.stream()
                    .filter(item -> item.getId().equals(actionData.getThingId()))
                    .findFirst()
                    .ifPresent(itemSelector::setValue);
        }
        if (actionData.getLumen() != null) {
            lumenField.setValue(actionData.getLumen());
        }

        itemSelector.addValueChangeListener(
                e -> actionData.setThingId(e.getValue() != null ? e.getValue().getId() : null));
        lumenField.addValueChangeListener(e -> actionData.setLumen(e.getValue()));

        add(title, description, itemSelector, lumenField);
    }

    @Override
    public boolean validate() {
        boolean itemValid = itemSelector.getValue() != null;
        boolean lumenValid = lumenField.getValue() != null;

        if (!itemValid) {
            itemSelector.setErrorMessage("Please select an item");
            itemSelector.setInvalid(true);
        } else {
            itemSelector.setInvalid(false);
        }

        if (!lumenValid) {
            lumenField.setErrorMessage("Please enter a lumen value");
            lumenField.setInvalid(true);
        } else {
            lumenField.setInvalid(false);
        }

        return itemValid && lumenValid;
    }

    @Override
    public String getActionSummary() {
        if (itemSelector == null || itemSelector.getValue() == null) return "(none)";
        String lumen = lumenField != null && lumenField.getValue() != null
                ? lumenField.getValue().toString() : "";
        return ViewSupporter.formatDescription(itemSelector.getValue()) + " -> " + lumen + " lumen";
    }
}
