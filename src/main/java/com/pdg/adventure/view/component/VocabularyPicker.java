package com.pdg.adventure.view.component;

import com.vaadin.flow.component.combobox.ComboBox;

import java.util.Collection;

import com.pdg.adventure.model.Word;

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
