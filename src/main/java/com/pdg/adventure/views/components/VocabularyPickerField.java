package com.pdg.adventure.views.components;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import lombok.Getter;

import java.util.Collection;
import java.util.function.Function;

import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.views.commands.ValidationMessage;


public class VocabularyPickerField extends VerticalLayout {
    @Getter
    private final VocabularyPicker picker;
    private final ValidationMessage validationMessage;

    public VocabularyPickerField(String label, String tooltip, Word.Type type, VocabularyData vocabulary) {
        picker = new VocabularyPicker(label);
        picker.setTooltipText(tooltip);
        picker.populate(vocabulary.getWords(type).stream().filter(w -> w.getSynonym() == null).toList());
        validationMessage = new ValidationMessage();
        add(picker, validationMessage);
    }

    public void bind(Binder<?> binder, Function<Word, String> errorMessage, String property) {
        binder.forField(picker)
              .withValidator(word -> word != null && !word.getText().isEmpty(), errorMessage.apply(null))
              .bind(property);
    }

    public void setPlaceholder(String aPlaceholder) {
        picker.setPlaceholder(aPlaceholder);
    }

    public void populate(Collection<Word> words) {
        picker.populate(words);
    }
}
