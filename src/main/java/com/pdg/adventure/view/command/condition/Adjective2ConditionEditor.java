package com.pdg.adventure.view.command.condition;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.condition.Adjective2ConditionData;

@AutoRegisterConditionEditor
public class Adjective2ConditionEditor extends AbstractSingleWordConditionEditor<Adjective2ConditionData> {

    public Adjective2ConditionEditor(Adjective2ConditionData conditionData, AdventureData adventureData) {
        super(conditionData, adventureData, "Second adjective", "Select second adjective", Word.Type.ADJECTIVE,
                "Please select a second adjective");
    }

    @Override
    protected String currentWordText() { return typedCondition.getAdjective2Text(); }

    @Override
    protected void applyWordText(String text) { typedCondition.setAdjective2Text(text); }
}
