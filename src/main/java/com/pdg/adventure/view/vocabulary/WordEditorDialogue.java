package com.pdg.adventure.view.vocabulary;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.*;

import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;

public class WordEditorDialogue {

    private RadioButtonGroup<Word.Type> typeSelector;
    private ComboBox<Word> synonyms;
    private TextField wordText;
    private Button saveButton;

    private final Binder<Word> binder;

    private final VocabularyData vocabularyData;
    private transient Word currentWord;

    private transient final List<GuiListener> guiListeners;
    private transient final List<SaveListener> saveListeners;

    public enum EditType {
        EDIT("Edit"),
        NEW("New");

        private final String value;

        EditType(String aValue) {
            value = aValue;
        }
    }

    private EditType editType = EditType.NEW;

    public WordEditorDialogue(VocabularyData aVocabulary) {
        vocabularyData = aVocabulary;
        binder = new Binder<>(Word.class);
        guiListeners = new ArrayList<>();
        saveListeners = new ArrayList<>();

        createSaveButton();
        createWordField();
        createTypeSelector();
        createSynonymSelector();
    }

    public void addGuiListener(GuiListener aGuiListener) {
        guiListeners.add(aGuiListener);
    }

    public void addSaveListener(SaveListener aSaveListener) {
        saveListeners.add(aSaveListener);
    }

    public void open(EditType anEditType, DescribableWordAdapter aWordWrapper) {
        editType = anEditType;
        switch (anEditType) {
            case EDIT: {
                currentWord = aWordWrapper.getWord();
                wordText.setValue(currentWord.getText());
                typeSelector.setValue(currentWord.getType());
                Word synonym = currentWord.getSynonym();
                if (synonym != null) {
                    synonyms.setValue(synonym);
                }
                break;
            }
            case NEW: {
                currentWord = new Word("", Word.Type.NOUN);
                break;
            }
        }

        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setDraggable(true);
        dialog.getHeader().add(createDialogHeader(anEditType));
        dialog.getFooter().add(createDialogFooter(dialog)); // footer first, or saveButton is null
        dialog.add(createDialogContent());
        dialog.open();
    }


    private Component createDialogContent() {

        HorizontalLayout wordDefinition  = new HorizontalLayout();
        wordDefinition.add(wordText, typeSelector);

        VerticalLayout fieldLayout = new VerticalLayout(wordDefinition, synonyms);
        fieldLayout.setSpacing(false);
        fieldLayout.setPadding(false);
        fieldLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        fieldLayout.getStyle().set("width", "300px").set("max-width", "100%");

        return fieldLayout;
    }

    private void createSynonymSelector() {
        ComboBox.ItemFilter<Word> wordItemFilter = WordFilter.filterByTypeOrText();
        synonyms = new ComboBox<>("Synonyms");
        synonyms.setItems(wordItemFilter, vocabularyData.getWords());
        synonyms.setItemLabelGenerator(Word::getText);
        synonyms.addValueChangeListener(e -> validateIfSave());
        synonyms.setHelperText("A synonym has precedence over a type.");
        synonyms.setTooltipText("You may filter on a word's type or text.");
    }

    private void createWordField() {
        wordText = new TextField("Word");
        wordText.setRequired(true);
        wordText.addValueChangeListener(e -> validateIfSave());
        wordText.setValueChangeMode(ValueChangeMode.EAGER);
        wordText.focus();
        wordText.setHelperText("Combine with a synonym or a type.");

    }

    private void createTypeSelector() {
        typeSelector = new RadioButtonGroup<>();
        typeSelector.addThemeVariants(RadioGroupVariant.LUMO_HELPER_ABOVE_FIELD);
        typeSelector.getStyle().set("--vaadin-input-field-border-width", "1px");
        typeSelector.setLabel("Type");
        List<Word.Type> typeList = new ArrayList<>();
        Collections.addAll(typeList, Word.Type.values());
        typeSelector.addValueChangeListener(e -> validateIfSave());
        typeSelector.setItems(typeList);
        typeSelector.setRenderer(new ComponentRenderer<Component, Word.Type>(wordType -> new Text(wordType.name())));
    }

    private void validateIfSave() {
        if (!(typeSelector.getValue() == null && synonyms.getValue() == null)) {
            saveButton.setEnabled(!wordText.isInvalid());
        }
    }

    private Component createDialogFooter(Dialog dialog) {
        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
            notifyListeners(false);
        });
        cancelButton.addClickShortcut(Key.ESCAPE);

        saveButton.addClickListener(e -> {
            if (editType == EditType.EDIT) {
                final Optional<Word> existingWord = vocabularyData.findWord(wordText.getValue());
                if (existingWord.isPresent() && !existingWord.get().equals(currentWord)) {
                    wordText.setErrorMessage(String.format(VocabularyData.DUPLICATE_WORD_TEXT, wordText.getValue()));
                    wordText.setInvalid(true);
                    return;
                };
                final Optional<Word> word = vocabularyData.removeWord(currentWord.getText());
                if (word.isPresent()) {
                    final Word editedWord = word.get();
                    final Word desiredSynonym = synonyms.getValue();
                    if (desiredSynonym == null) {
                        editedWord.setSynonym(null);
                        editedWord.setType(typeSelector.getValue());
                    } else {
                        final Word rootSynonym = desiredSynonym.getSynonym();
                        if (rootSynonym != null && rootSynonym.equals(editedWord)) {
                            throw new RuntimeException("Synonym loop detected: " + editedWord.getText());
                        }
                        editedWord.setSynonym(Objects.requireNonNullElse(rootSynonym, desiredSynonym));
                        editedWord.setType(desiredSynonym.getType());
                    }
                    editedWord.setText(wordText.getValue());
                    vocabularyData.addWord(editedWord);
                } else {
                    throw new RuntimeException("Word not found: " + currentWord.getText());
                }
            } else { // EditType.NEW
                if (synonyms.getValue() == null) {
                    vocabularyData.createWord(wordText.getValue(), typeSelector.getValue());
                } else {
                    vocabularyData.createSynonym(wordText.getValue(), synonyms.getValue());
                }
            }
            dialog.close();
            notifyListeners(true);
        });

        HorizontalLayout hl = new HorizontalLayout();
        hl.add(cancelButton, saveButton);
        return hl;
    }

    private void createSaveButton() {
        saveButton = new Button("Save");
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setEnabled(false);
        saveButton.addClickShortcut(Key.ENTER);
    }

    private void notifyListeners(boolean aFlagWhetherTheyShouldSaveAndUpdadeTheirGUI) {
        if (aFlagWhetherTheyShouldSaveAndUpdadeTheirGUI) {
            for (GuiListener guiListener : guiListeners) {
                guiListener.updateGui();
            }
            for (SaveListener saveListener : saveListeners) {
                saveListener.persistData();
            }
        }
    }

    private Component createDialogHeader(EditType anEditType) {
        H2 header = new H2(anEditType.value + " word");
        header.addClassName("draggable");
        header.getStyle().set("margin", "0").set("font-size", "1.5em")
                .set("font-weight", "bold").set("cursor", "move")
                .set("padding", "var(--lumo-space-m) 0").set("flex", "1");

        return header;
    }

}
