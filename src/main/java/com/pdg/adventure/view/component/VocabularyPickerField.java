package com.pdg.adventure.view.component;

import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;


public class VocabularyPickerField extends VocabularyPicker {

    public VocabularyPickerField(String aLabel) {
        super(aLabel);
    }

    public VocabularyPickerField(String label, String tooltip) {
        super(label);
        setTooltipText(tooltip);
    }

    public VocabularyPickerField(String label, String tooltip, Word.Type type, VocabularyData vocabulary) {
        super(label);
        setTooltipText(tooltip);
        populate(vocabulary.getWords(type).stream().filter(w -> w.getSynonym() == null).toList());
    }
}
