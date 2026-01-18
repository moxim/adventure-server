package com.pdg.adventure.view.vocabulary;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.ModalityMode;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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
                currentWord = aWordWrapper.word();
                wordText.setValue(currentWord.getText());
                typeSelector.setValue(currentWord.getType());
                Word synonym = currentWord.getSynonym();
                if (synonym != null) {
                    synonyms.setValue(synonym);
                } else {
                    synonyms.clear();
                }
                // Update synonym dropdown to exclude current word
                updateSynonymItems();
                break;
            }
            case NEW: {
                currentWord = new Word("", Word.Type.NOUN);
                wordText.clear();
                typeSelector.setValue(Word.Type.NOUN); // Default to NOUN
                synonyms.clear();
                // Reset synonym dropdown to show all words
                updateSynonymItems();
                break;
            }
        }

        Dialog dialog = new Dialog();
        dialog.setModality(ModalityMode.VISUAL);
        dialog.setDraggable(true);
        dialog.getHeader().add(createDialogHeader(anEditType));
        dialog.getFooter().add(createDialogFooter(dialog)); // footer first, or saveButton is null
        dialog.add(createDialogContent());
        dialog.setMinWidth("40%");

        // Handle synonym change to set proper type selector state
        handleSynonymChange();

        // Trigger initial validation
        validateAndUpdateSaveButton();

        dialog.open();
    }


    private Component createDialogContent() {

        HorizontalLayout wordDefinition = new HorizontalLayout();
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
        synonyms.addValueChangeListener(e -> {
            handleSynonymChange();
            validateAndUpdateSaveButton();
        });
        synonyms.setHelperText("A synonym has precedence over a type.");
        synonyms.setTooltipText("You may filter on a word's type or text.");
    }

    /**
     * Handles changes to the synonym selector.
     * When a synonym is selected, the type selector is disabled since type is inherited.
     */
    private void handleSynonymChange() {
        Word selectedSynonym = synonyms.getValue();
        if (selectedSynonym != null) {
            // Disable type selector and set it to the synonym's type
            typeSelector.setEnabled(false);
            typeSelector.setValue(selectedSynonym.getType());
            typeSelector.setHelperText("Type inherited from synonym: " + selectedSynonym.getText());
        } else {
            // Re-enable type selector
            typeSelector.setEnabled(true);
            typeSelector.setHelperText(null);
            // Set default type if none selected
            if (typeSelector.getValue() == null) {
                typeSelector.setValue(Word.Type.NOUN);
            }
        }
    }

    private void createWordField() {
        wordText = new TextField("Word");
        wordText.setRequired(true);
        wordText.addValueChangeListener(e -> validateAndUpdateSaveButton());
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
        typeSelector.addValueChangeListener(e -> validateAndUpdateSaveButton());
        typeSelector.setItems(typeList);
        typeSelector.setRenderer(new ComponentRenderer<Component, Word.Type>(wordType -> new Text(wordType.name())));
    }

    /**
     * Validates the form and updates the save button state.
     * Requirements:
     * - Word text must not be empty or whitespace
     * - Either type OR synonym must be selected (mutually exclusive if synonym is selected)
     * - If synonym is selected, it cannot be the current word (in edit mode)
     */
    private void validateAndUpdateSaveButton() {
        boolean isValid = isFormValid();
        saveButton.setEnabled(isValid);
    }

    /**
     * Checks if the form is valid for saving.
     * Package-private for testing.
     */
    boolean isFormValid() {
        // Word text must not be empty
        String text = wordText.getValue();
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        // When synonym is selected, type can be ignored (it will be inherited)
        Word selectedSynonym = synonyms.getValue();
        Word.Type selectedType = typeSelector.getValue();

        // Must have either a synonym or a type
        return selectedSynonym != null || selectedType != null;
    }

    /**
     * Updates the synonym dropdown items, filtering out the current word when in edit mode.
     * Package-private for testing.
     */
    void updateSynonymItems() {
        ComboBox.ItemFilter<Word> wordItemFilter = WordFilter.filterByTypeOrText();

        if (editType == EditType.EDIT && currentWord != null) {
            // Filter out the current word to prevent self-reference
            Collection<Word> availableWords = vocabularyData.getWords().stream()
                                                            .filter(word -> !word.equals(currentWord))
                                                            .toList();
            synonyms.setItems(wordItemFilter, availableWords);
        } else {
            synonyms.setItems(wordItemFilter, vocabularyData.getWords());
        }
    }

    private Component createDialogFooter(Dialog dialog) {
        Button cancelButton = new Button("Cancel", e -> {
            dialog.close();
            notifyListeners(false);
        });
        cancelButton.addClickShortcut(Key.ESCAPE);

        saveButton.addClickListener(e -> {
            try {
                if (editType == EditType.EDIT) {
                    saveEditedWord(dialog);
                } else {
                    saveNewWord(dialog);
                }
            } catch (IllegalArgumentException ex) {
                showErrorNotification(ex.getMessage());
            } catch (Exception ex) {
                showErrorNotification("An unexpected error occurred: " + ex.getMessage());
            }
        });

        HorizontalLayout hl = new HorizontalLayout();
        hl.add(cancelButton, saveButton);
        return hl;
    }

    /**
     * Saves a new word to the vocabulary.
     */
    private void saveNewWord(Dialog dialog) {
        String newWordText = wordText.getValue().toLowerCase().trim();

        // Check for duplicate
        if (vocabularyData.findWord(newWordText).isPresent()) {
            wordText.setErrorMessage(String.format(VocabularyData.DUPLICATE_WORD_TEXT, newWordText));
            wordText.setInvalid(true);
            return;
        }

        Word selectedSynonym = synonyms.getValue();
        if (selectedSynonym == null) {
            // Create word with type
            vocabularyData.createWord(newWordText, typeSelector.getValue());
        } else {
            // Create synonym
            vocabularyData.createSynonym(newWordText, selectedSynonym);
        }

        dialog.close();
        notifyListeners(true);
        showSuccessNotification("Word '" + newWordText + "' created successfully");
    }

    /**
     * Saves an edited word to the vocabulary.
     */
    private void saveEditedWord(Dialog dialog) {
        String newWordText = wordText.getValue().toLowerCase().trim();

        // Check if word text changed and if new text already exists
        if (!currentWord.getText().equals(newWordText)) {
            Optional<Word> existingWord = vocabularyData.findWord(newWordText);
            if (existingWord.isPresent()) {
                wordText.setErrorMessage(String.format(VocabularyData.DUPLICATE_WORD_TEXT, newWordText));
                wordText.setInvalid(true);
                return;
            }
        }

        // Remove the old word
        Optional<Word> wordToEdit = vocabularyData.removeWord(currentWord.getText());
        if (wordToEdit.isEmpty()) {
            throw new IllegalArgumentException("Word not found: " + currentWord.getText());
        }

        Word editedWord = wordToEdit.get();
        Word desiredSynonym = synonyms.getValue();

        if (desiredSynonym == null) {
            // No synonym, use type
            editedWord.setSynonym(null);
            editedWord.setType(typeSelector.getValue());
        } else {
            // Check for circular reference before setting synonym
            if (hasCircularReference(editedWord, desiredSynonym)) {
                // Re-add the word back since we're not saving
                vocabularyData.addWord(editedWord);
                showErrorNotification("Cannot create circular synonym reference");
                return;
            }

            // Set synonym (and get root if needed)
            Word rootSynonym = desiredSynonym.getSynonym();
            editedWord.setSynonym(Objects.requireNonNullElse(rootSynonym, desiredSynonym));
            editedWord.setType(desiredSynonym.getType());
        }

        editedWord.setText(newWordText);
        vocabularyData.addWord(editedWord);

        dialog.close();
        notifyListeners(true);
        showSuccessNotification("Word updated successfully");
    }

    /**
     * Checks if setting the synonym would create a circular reference.
     * This checks the entire chain to detect cycles at any depth.
     * Package-private for testing.
     *
     * @param word            The word being edited
     * @param proposedSynonym The synonym we want to set
     * @return true if a circular reference would be created
     */
    boolean hasCircularReference(Word word, Word proposedSynonym) {
        Set<String> visited = new HashSet<>();
        visited.add(word.getText());

        Word current = proposedSynonym;
        while (current != null) {
            if (visited.contains(current.getText())) {
                return true; // Found a cycle
            }
            visited.add(current.getText());
            current = current.getSynonym();
        }

        return false;
    }

    /**
     * Shows a success notification to the user.
     */
    private void showSuccessNotification(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    /**
     * Shows an error notification to the user.
     */
    private void showErrorNotification(String message) {
        Notification notification = Notification.show(message, 5000, Notification.Position.BOTTOM_START);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
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
