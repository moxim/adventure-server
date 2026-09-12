package com.pdg.adventure.view.command.condition;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.condition.AdverbConditionData;

@AutoRegisterConditionEditor
public class AdverbConditionEditor extends AbstractSingleWordConditionEditor<AdverbConditionData> {

    public AdverbConditionEditor(AdverbConditionData conditionData, AdventureData adventureData) {
        super(conditionData, adventureData, "Adverb", "Select adverb", Word.Type.ADVERB,
                "Please select an adverb");
    }

    @Override
    protected String currentWordText() { return typedCondition.getAdverbText(); }

    @Override
    protected void applyWordText(String text) { typedCondition.setAdverbText(text); }
}
