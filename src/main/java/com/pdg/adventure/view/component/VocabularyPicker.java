package com.pdg.adventure.view.component;

import com.vaadin.flow.component.combobox.ComboBox;

import java.util.Collection;

import com.pdg.adventure.model.Word;

public class VocabularyPicker extends ComboBox<Word> {

    public VocabularyPicker(String aLabel) {
        super(aLabel);
        setItemLabelGenerator(Word::getText);
        setClearButtonVisible(true);
        setHelperText("You can filter on text.");
    }

    public void populate(Collection<Word> aNumberOfWords) {
        setItems(aNumberOfWords.stream().sorted((w1, w2) -> w1.getText().compareToIgnoreCase(w2.getText())).toList());
    }
}
