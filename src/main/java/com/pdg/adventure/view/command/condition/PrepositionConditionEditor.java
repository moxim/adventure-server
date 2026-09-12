package com.pdg.adventure.view.command.condition;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.condition.PrepositionConditionData;

@AutoRegisterConditionEditor
public class PrepositionConditionEditor extends AbstractSingleWordConditionEditor<PrepositionConditionData> {

    public PrepositionConditionEditor(PrepositionConditionData conditionData, AdventureData adventureData) {
        super(conditionData, adventureData, "Preposition", "Select preposition", Word.Type.PREPOSITION,
                "Please select a preposition");
    }

    @Override
    protected String currentWordText() { return typedCondition.getPrepositionText(); }

    @Override
    protected void applyWordText(String text) { typedCondition.setPrepositionText(text); }
}
