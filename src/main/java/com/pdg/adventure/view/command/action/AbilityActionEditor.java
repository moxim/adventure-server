package com.pdg.adventure.view.command.action;

import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.IntegerField;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.action.AbilityActionData;

/** Sets what the player can carry: the most weight and the most objects, both whole numbers from 0. */
@AutoRegisterActionEditor
public class AbilityActionEditor extends ActionEditorComponent<AbilityActionData> {
    static final int MIN_VALUE = 0;

    private IntegerField maxWeightField;
    private IntegerField maxItemsField;

    public AbilityActionEditor(AbilityActionData actionData, AdventureData adventureData) {
        super(actionData);
    }

    @Override
    protected void buildUI() {
        H4 title = new H4("Ability Action");
        Span description = new Span("Set what the player can carry. Taking an item is refused when the player "
                                    + "already carries that many objects or would carry more than that weight.");
        description.getStyle().set("color", "var(--lumo-secondary-text-color)");

        maxWeightField = createField("Max weight", "The most weight the player can carry in total");
        maxWeightField.setValue(actionData.getMaxWeight());
        maxWeightField.addValueChangeListener(e -> actionData.setMaxWeight(e.getValue()));

        maxItemsField = createField("Max number of objects", "The most objects the player can carry at once");
        maxItemsField.setValue(actionData.getMaxItems());
        maxItemsField.addValueChangeListener(e -> actionData.setMaxItems(e.getValue()));

        add(title, description, maxWeightField, maxItemsField);
    }

    private static IntegerField createField(String aLabel, String aHelperText) {
        IntegerField field = new IntegerField(aLabel);
        field.setMin(MIN_VALUE);
        field.setStep(1);
        field.setStepButtonsVisible(true);
        field.setRequired(true);
        field.setHelperText(aHelperText + " (a whole number from " + MIN_VALUE + ")");
        field.setWidthFull();
        return field;
    }

    @Override
    public boolean validate() {
        boolean weightValid = isValid(maxWeightField);
        boolean itemsValid = isValid(maxItemsField);
        return weightValid && itemsValid;
    }

    private static boolean isValid(IntegerField aField) {
        Integer value = aField.getValue();
        boolean valid = value != null && value >= MIN_VALUE;
        aField.setInvalid(!valid);
        if (!valid) {
            aField.setErrorMessage("Please enter a whole number from " + MIN_VALUE);
        }
        return valid;
    }

    @Override
    public String getActionSummary() {
        if (maxWeightField == null || maxItemsField == null) {
            return "(none)";
        }
        return "weight " + show(maxWeightField.getValue()) + ", objects " + show(maxItemsField.getValue());
    }

    private static String show(Integer aValue) {
        return aValue == null ? "?" : aValue.toString();
    }
}
