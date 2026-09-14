package com.pdg.adventure.view.vocabulary;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;

/**
 * Browserless (UI-unit) tests for WordEditorDialogue, covering the save/cancel flows and
 * dialog wiring that the plain-JUnit WordEditorDialogueTest can't exercise without a UI
 * (Dialog.open() needs a current UI). Synonym-selection paths (circular reference rejection,
 * the synonym cascade dialog) are deliberately left to WordEditorDialogueTest's pure
 * hasCircularReference() tests instead - ComboBox.setValue() is unreliable in browserless
 * (DataKeyMapper not initialised), so driving a real synonym selection here would be flaky.
 */
class WordEditorDialogueBrowserlessTest extends BrowserlessTest {

    private VocabularyData vocabularyData;
    private WordEditorDialogue dialogue;
    private GuiListener guiListener;
    private SaveListener saveListener;

    @BeforeEach
    void setUp() {
        vocabularyData = new VocabularyData();
        dialogue = new WordEditorDialogue(vocabularyData);
        guiListener = mock(GuiListener.class);
        saveListener = mock(SaveListener.class);
        dialogue.addGuiListener(guiListener);
        dialogue.addSaveListener(saveListener);
    }

    @Test
    @DisplayName("open(NEW) shows an empty word field, defaults to Noun, and titles the dialog 'New word'")
    void open_newMode_opensDialogWithEmptyWordFieldAndNounDefault() {
        dialogue.open(WordEditorDialogue.EditType.NEW, null);

        Dialog dialog = find(Dialog.class).single();
        assertThat(find(TextField.class, dialog).single().getValue()).isEmpty();
        assertThat(find(RadioButtonGroup.class, dialog).single().getValue()).isEqualTo(Word.Type.NOUN);
        assertThat(find(H2.class, dialog).single().getText()).isEqualTo("New word");
    }

    @Test
    @DisplayName("open(EDIT) pre-fills the word field with the existing word's text and titles the dialog 'Edit word'")
    void open_editMode_populatesWordTextWithExistingWord() {
        Word sword = vocabularyData.createWord("sword", Word.Type.NOUN);

        dialogue.open(WordEditorDialogue.EditType.EDIT, new DescribableWordAdapter(sword));

        Dialog dialog = find(Dialog.class).single();
        assertThat(find(TextField.class, dialog).single().getValue()).isEqualTo("sword");
        assertThat(find(H2.class, dialog).single().getText()).isEqualTo("Edit word");
    }

    @Test
    @DisplayName("Saving a valid new word creates it, closes the dialog, and notifies both listeners")
    void savingValidNewWord_createsWordClosesDialogAndNotifiesListeners() {
        dialogue.open(WordEditorDialogue.EditType.NEW, null);
        Dialog dialog = find(Dialog.class).single();
        find(TextField.class, dialog).single().setValue("torch");

        test(find(Button.class, dialog).withText("Save").single()).click();

        assertThat(vocabularyData.findWord("torch")).isPresent();
        assertThat(vocabularyData.findWord("torch").get().getType()).isEqualTo(Word.Type.NOUN);
        assertThat(find(Dialog.class).all()).as("dialog closes after a successful save").isEmpty();
        verify(guiListener).updateGui();
        verify(saveListener).persistData();
    }

    @Test
    @DisplayName("Saving a word whose text already exists shows an error and keeps the dialog open")
    void savingDuplicateWordText_showsErrorAndKeepsDialogOpen() {
        vocabularyData.createWord("torch", Word.Type.NOUN);

        dialogue.open(WordEditorDialogue.EditType.NEW, null);
        Dialog dialog = find(Dialog.class).single();
        TextField wordText = find(TextField.class, dialog).single();
        wordText.setValue("torch");

        test(find(Button.class, dialog).withText("Save").single()).click();

        assertThat(find(Dialog.class).all()).as("dialog stays open after a rejected save").hasSize(1);
        assertThat(wordText.isInvalid()).isTrue();
        assertThat(vocabularyData.getWords()).hasSize(1);
        verify(guiListener, never()).updateGui();
        verify(saveListener, never()).persistData();
    }

    @Test
    @DisplayName("Cancel closes the dialog without creating the word or notifying listeners")
    void clickingCancel_closesDialogWithoutSavingOrNotifying() {
        dialogue.open(WordEditorDialogue.EditType.NEW, null);
        Dialog dialog = find(Dialog.class).single();
        find(TextField.class, dialog).single().setValue("torch");

        test(find(Button.class, dialog).withText("Cancel").single()).click();

        assertThat(find(Dialog.class).all()).isEmpty();
        assertThat(vocabularyData.findWord("torch")).isEmpty();
        verify(guiListener, never()).updateGui();
        verify(saveListener, never()).persistData();
    }

    @Test
    @DisplayName("Editing a word's text renames it in place, preserving its type")
    void editingWord_changingTextOnly_updatesWordInPlace() {
        Word typo = vocabularyData.createWord("atack", Word.Type.VERB);

        dialogue.open(WordEditorDialogue.EditType.EDIT, new DescribableWordAdapter(typo));
        Dialog dialog = find(Dialog.class).single();
        find(TextField.class, dialog).single().setValue("attack");

        test(find(Button.class, dialog).withText("Save").single()).click();

        assertThat(vocabularyData.findWord("atack")).isEmpty();
        assertThat(vocabularyData.findWord("attack")).isPresent();
        assertThat(vocabularyData.findWord("attack").get().getType()).isEqualTo(Word.Type.VERB);
        assertThat(find(Dialog.class).all()).isEmpty();
        verify(guiListener).updateGui();
        verify(saveListener).persistData();
    }

    @Test
    @DisplayName("Renaming a word to another existing word's text is rejected, leaving both words untouched")
    void editingWord_renamingToAnotherExistingWordsText_showsErrorAndDoesNotOverwrite() {
        vocabularyData.createWord("sword", Word.Type.NOUN);
        Word blade = vocabularyData.createWord("blade", Word.Type.NOUN);

        dialogue.open(WordEditorDialogue.EditType.EDIT, new DescribableWordAdapter(blade));
        Dialog dialog = find(Dialog.class).single();
        find(TextField.class, dialog).single().setValue("sword");

        test(find(Button.class, dialog).withText("Save").single()).click();

        assertThat(find(Dialog.class).all()).as("dialog stays open after a rejected save").hasSize(1);
        assertThat(vocabularyData.findWord("sword")).isPresent();
        assertThat(vocabularyData.findWord("blade")).isPresent();
        verify(guiListener, never()).updateGui();
        verify(saveListener, never()).persistData();
    }
}
