package com.pdg.adventure.views.components;

import com.pdg.adventure.model.Word;
import com.vaadin.flow.component.combobox.ComboBox;

import java.util.Collection;

public class VocabularyPicker extends ComboBox<Word> {

    public VocabularyPicker(String aLabel) {
        super(aLabel);
        setItemLabelGenerator(Word::getText);
        setClearButtonVisible(true);
        setHelperText("You may filter on a word's text.");
    }

    public void populate(Collection<Word> aNumberOfWords) {
        setItems(aNumberOfWords);
    }
}
