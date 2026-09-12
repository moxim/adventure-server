package com.pdg.adventure.view.command.condition;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.condition.PreConditionData;
import com.pdg.adventure.view.component.VocabularyPickerField;

/**
 * Base for condition editors that pick a single {@link Word} of a fixed {@link Word.Type} from
 * the adventure's vocabulary (e.g. PrepositionCondition, AdverbCondition) via a
 * {@link VocabularyPickerField}, mirroring {@link AbstractSingleItemConditionEditor}'s shape for
 * item-picking conditions.
 */
public abstract class AbstractSingleWordConditionEditor<T extends PreConditionData>
        extends ConditionEditorComponent<T> {

    protected final T typedCondition;
    protected final AdventureData adventureData;
    private final String label;
    private final String tooltip;
    private final Word.Type wordType;
    private final String errorMessage;
    private VocabularyPickerField wordSelector;

    protected AbstractSingleWordConditionEditor(T conditionData, AdventureData adventureData,
            String label, String tooltip, Word.Type wordType, String errorMessage) {
        super(conditionData);
        this.typedCondition = conditionData;
        this.adventureData = adventureData;
        this.label = label;
        this.tooltip = tooltip;
        this.wordType = wordType;
        this.errorMessage = errorMessage;
    }

    protected abstract String currentWordText();

    protected abstract void applyWordText(String text);

    @Override
    protected final void buildUI() {
        VocabularyData vocabulary = adventureData.getVocabularyData() != null
                ? adventureData.getVocabularyData() : new VocabularyData();
        wordSelector = new VocabularyPickerField(label, tooltip, wordType, vocabulary);
        wordSelector.setWidthFull();
        wordSelector.setRequired(true);

        String currentText = currentWordText();
        if (currentText != null) {
            vocabulary.getWords(wordType).stream()
                      .filter(w -> w.getSynonym() == null)
                      .filter(w -> w.getText().equals(currentText))
                      .findFirst().ifPresent(wordSelector::setValue);
        }
        wordSelector.addValueChangeListener(e ->
                applyWordText(e.getValue() != null ? e.getValue().getText() : null));

        add(wordSelector);
    }

    @Override
    public final boolean validate() {
        boolean valid = wordSelector.getValue() != null;
        wordSelector.setInvalid(!valid);
        if (!valid) wordSelector.setErrorMessage(errorMessage);
        return valid;
    }

    @Override
    public final String getConditionSummary() {
        if (wordSelector == null || wordSelector.getValue() == null) return "(none)";
        return wordSelector.getValue().getText();
    }
}
