package com.pdg.adventure.view.command.condition;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.condition.Noun2ConditionData;

@AutoRegisterConditionEditor
public class Noun2ConditionEditor extends AbstractSingleWordConditionEditor<Noun2ConditionData> {

    public Noun2ConditionEditor(Noun2ConditionData conditionData, AdventureData adventureData) {
        super(conditionData, adventureData, "Second noun", "Select second noun", Word.Type.NOUN,
                "Please select a second noun");
    }

    @Override
    protected String currentWordText() { return typedCondition.getNoun2Text(); }

    @Override
    protected void applyWordText(String text) { typedCondition.setNoun2Text(text); }
}
