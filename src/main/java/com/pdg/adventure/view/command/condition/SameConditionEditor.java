package com.pdg.adventure.view.command.condition;

import java.util.Collection;
import java.util.function.Supplier;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.condition.SameConditionData;
import com.pdg.adventure.view.command.VariableNameSelector;
import com.pdg.adventure.view.support.VariableChoices;

@AutoRegisterConditionEditor
public class SameConditionEditor extends ConditionEditorComponent<SameConditionData> {
    private final SameConditionData sameData;
    private VariableNameSelector varOneField;
    private VariableNameSelector varTwoField;
    private final AdventureData adventureData;

    public SameConditionEditor(SameConditionData conditionData) {
        this(conditionData, null);
    }

    public SameConditionEditor(SameConditionData conditionData, AdventureData anAdventureData) {
        super(conditionData);
        this.sameData = conditionData;
        this.adventureData = anAdventureData;
    }

    @Override
    protected void buildUI() {
        Supplier<Collection<String>> defined =
                adventureData == null ? null : () -> VariableChoices.of(adventureData);
        varOneField = new VariableNameSelector("Variable 1", defined, false, sameData.getVariableNameOne());
        varTwoField = new VariableNameSelector("Variable 2", defined, false, sameData.getVariableNameTwo());

        varOneField.addValueChangeListener(e -> sameData.setVariableNameOne(e.getValue()));
        varTwoField.addValueChangeListener(e -> sameData.setVariableNameTwo(e.getValue()));

        add(varOneField, varTwoField);
    }

    @Override
    public boolean validate() {
        boolean oneValid = varOneField.validateSelection();
        boolean twoValid = varTwoField.validateSelection();
        return oneValid && twoValid;
    }

    @Override
    public String getConditionSummary() {
        String one = (varOneField != null && !varOneField.selectedName().isEmpty()) ? varOneField.selectedName() : "";
        String two = (varTwoField != null && !varTwoField.selectedName().isEmpty()) ? varTwoField.selectedName() : "";
        if (one.isEmpty()) return "(none)";
        return one + " = " + two;
    }
}
