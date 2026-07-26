package com.pdg.adventure.view.command.condition;

import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.value.ValueChangeMode;

import com.pdg.adventure.model.condition.ChanceConditionData;

@AutoRegisterConditionEditor
public class ChanceConditionEditor extends ConditionEditorComponent<ChanceConditionData> {

    private static final int MIN_CHANCE = 1;
    private static final int MAX_CHANCE = 100;

    private final ChanceConditionData typedCondition;
    private IntegerField chanceField;

    public ChanceConditionEditor(ChanceConditionData conditionData) {
        super(conditionData);
        this.typedCondition = conditionData;
    }

    @Override
    protected void buildUI() {
        chanceField = new IntegerField("Chance (%)");
        chanceField.setMin(MIN_CHANCE);
        chanceField.setMax(MAX_CHANCE);
        chanceField.setWidthFull();
        chanceField.setRequired(true);
        chanceField.setTooltipText("Succeeds when a random roll from " + MIN_CHANCE + "-" + MAX_CHANCE + " is at most this value.");
        chanceField.setValueChangeMode(ValueChangeMode.EAGER);

        if (typedCondition.getValue() != null) {
            chanceField.setValue(typedCondition.getValue());
        }

        chanceField.addValueChangeListener(e -> typedCondition.setValue(e.getValue()));

        add(chanceField);
    }

    @Override
    public boolean validate() {
        Integer value = chanceField.getValue();
        boolean valid = value != null && value >= MIN_CHANCE && value <= MAX_CHANCE;
        chanceField.setInvalid(!valid);
        if (!valid) {
            chanceField.setErrorMessage("Please enter a chance between " + MIN_CHANCE + " and " + MAX_CHANCE);
        }
        return valid;
    }

    @Override
    public String getConditionSummary() {
        if (chanceField == null || chanceField.getValue() == null) {
            return "(none)";
        }
        return chanceField.getValue() + "% chance";
    }
}
