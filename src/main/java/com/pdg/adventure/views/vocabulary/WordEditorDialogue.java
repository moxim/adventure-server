package com.pdg.adventure.views.vocabulary;

import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.vocabulary.Vocabulary;
import com.vaadin.flow.component.Component;
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
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WordEditorDialogue {

    private final Vocabulary vocabulary;
    private final Binder<Word> binder;
    private final List<GuiListener> guiListeners;
    private final List<SaveListener> saveListeners;

    private RadioButtonGroup<Word.Type> typeSelector;
    private ComboBox<Word> synonyms;
    private TextField wordName;
    private Word currentWord;
    private Button saveButton;

    public enum EditType {
        EDIT("Edit"),
        NEW("New");

        private final String value;

        EditType(String aValue) {
            value = aValue;
        }
    }

    public WordEditorDialogue(Vocabulary aVocabulary) {
        vocabulary = aVocabulary;
        binder = new Binder<>(Word.class);
        guiListeners = new ArrayList<>();
        saveListeners = new ArrayList<>();

        createSaveButton();
        createWordField();
        createTypeSelector();
        createSynonymSelector();

//        binder.bind(wordName, Word::getText, Word::setText);
//        binder.bind(typeSelector, Word::getType, Word::setType);
//        binder.bind(synonyms, Word::getSynonym, Word::setSynonym);
    }

    public void addGuiListener(GuiListener aGuiListener) {
        guiListeners.add(aGuiListener);
    }

    public void addSaveListener(SaveListener aSaveListener) {
        saveListeners.add(aSaveListener);
    }

    public void open(EditType anEditType, DescribableWordAdapter aWordWrapper) {
        if (aWordWrapper != null) { // when editing
            currentWord = aWordWrapper.getWord();
            wordName.setValue(currentWord.getText());
            typeSelector.setValue(currentWord.getType());
            Word synonym = currentWord.getSynonym();
            if (synonym != null) {
                synonyms.setValue(synonym);
            }
        } else {
            currentWord = new Word("", Word.Type.NOUN);
        }

//                    binder.readBean(currentWord);

        Dialog dialog = new Dialog();
        dialog.setModal(true);
        dialog.setDraggable(true);
//        dialog.getElement().setAttribute("aria-label", anEditType.value + " word");
        dialog.getHeader().add(createDialogHeader(anEditType));
        dialog.getFooter().add(createDialogFooter(dialog)); // footer first, or saveButton is null
        dialog.add(createDialogContent(aWordWrapper));
        dialog.open();
    }


    private Component createDialogContent(DescribableWordAdapter aWordWrapper) {

        HorizontalLayout wordDefinition  = new HorizontalLayout();
        wordDefinition.add(wordName, typeSelector);

        VerticalLayout fieldLayout = new VerticalLayout(wordDefinition, synonyms);
        fieldLayout.setSpacing(false);
        fieldLayout.setPadding(false);
        fieldLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        fieldLayout.getStyle().set("width", "300px").set("max-width", "100%");

        return fieldLayout;
    }

    private void createSynonymSelector() {
        ComboBox.ItemFilter<Word> wordItemFilter = (word, filterString) -> {
            String typeName = word.getType().name();
            return typeName.startsWith(filterString.toUpperCase());
        };

        synonyms = new ComboBox<>("Synonyms");
        synonyms.setItems(wordItemFilter, vocabulary.getWords());
        synonyms.setItemLabelGenerator(Word::getText);
        synonyms.addValueChangeListener(e -> validateIfSave());
        synonyms.setHelperText("A synonym has precedence over a type.");
        synonyms.setTooltipText("You may filter on a word's type.");
    }

    private void createWordField() {
        wordName = new TextField("Word");
        wordName.setRequired(true);
        wordName.addValueChangeListener(e -> validateIfSave());
        wordName.setValueChangeMode(ValueChangeMode.EAGER);
        wordName.focus();
        wordName.setHelperText("Combine with a synonym or a type.");

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
        try {
            binder.writeBean(currentWord);
            if (!(typeSelector.getValue() == null && synonyms.getValue() == null)) {
                saveButton.setEnabled(!wordName.isInvalid());
                // saveButton.setEnabled(binder.isValid());
                // saveButton.setEnabled(false);
            }
        } catch (ValidationException e) {
            throw new RuntimeException(e);
        }
    }

    private Component createDialogFooter(Dialog dialog) {
        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
            notifyListeners(false);
        });
        saveButton.addClickListener(e -> {
            if (synonyms.getValue() == null) {
                vocabulary.addNewWord(wordName.getValue(), typeSelector.getValue());
            } else {
                vocabulary.addSynonymForWord(wordName.getValue(), synonyms.getValue());
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
