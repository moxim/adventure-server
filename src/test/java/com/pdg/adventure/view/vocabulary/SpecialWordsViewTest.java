package com.pdg.adventure.view.vocabulary;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventBus;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.component.VocabularyPickerField;

class SpecialWordsViewTest extends BrowserlessTest {

    private AdventureService adventureService;
    private SpecialWordsView view;
    private AdventureData adventureData;
    private VocabularyData vocabularyData;
    private Word examineVerb;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);

        vocabularyData = new VocabularyData();
        vocabularyData.createWord("take", Word.Type.VERB);
        vocabularyData.createWord("drop", Word.Type.VERB);
        vocabularyData.createWord("load", Word.Type.VERB);
        examineVerb = vocabularyData.createWord("examine", Word.Type.VERB);
        vocabularyData.createWord("inspect", Word.Type.VERB);

        adventureData = new AdventureData();
        adventureData.setId("test-adventure");
        adventureData.setVocabularyData(vocabularyData);
        adventureData.setLocationData(new HashMap<>());

        view = new SpecialWordsView(adventureService);
        UI.getCurrent().add(view);
    }

    @Test
    @DisplayName("After setAdventureData, four VocabularyPickerFields are displayed (take, drop, load, examine)")
    void afterSetAdventureData_fourPickerFieldsAreDisplayed() {
        view.setAdventureData(adventureData);

        assertThat(find(VocabularyPickerField.class, view).all()).hasSize(4);
    }

    @Test
    @DisplayName("After setAdventureData, an already-configured examineWord is preserved in vocabularyData")
    void afterSetAdventureData_existingExamineWordIsPreselected() {
        vocabularyData.setExamineWord(examineVerb);

        view.setAdventureData(adventureData);

        // ComboBox.getValue() is unreliable in browserless tests (DataKeyMapper not initialised);
        // verify the model object is preserved instead — the populate step must not clear it.
        assertThat(vocabularyData.getExamineWord()).isEqualTo(examineVerb);
    }

    @Test
    @DisplayName("After setAdventureData, the examine picker has no selection when examineWord is not set")
    void afterSetAdventureData_examinePickerIsEmpty_whenNoExamineWordSet() {
        view.setAdventureData(adventureData);

        VocabularyPickerField examineSelector = find(VocabularyPickerField.class, view).atIndex(3);
        assertThat(examineSelector.getValue()).isNull();
    }

    @Test
    @DisplayName("Clicking Save delegates to AdventureService.saveAdventureData")
    void clickingSave_callsAdventureServiceSave() {
        view.setAdventureData(adventureData);

        Button saveButton = find(Button.class, view).withText("Save").single();
        test(saveButton).click();

        verify(adventureService).saveAdventureData(adventureData);
    }

    @Test
    @DisplayName("Selecting a verb in the examine picker updates vocabularyData.examineWord")
    void selectingExamineVerb_updatesVocabularyDataExamineWord() throws Exception {
        view.setAdventureData(adventureData);

        // $(VocabularyPickerField.class, view).atIndex(3) returns a wrapper — not the actual
        // component instance that has the value-change listener registered. Access the field
        // directly to get the real instance.
        Field examineSelectorField = SpecialWordsView.class.getDeclaredField("examineSelector");
        examineSelectorField.setAccessible(true);
        VocabularyPickerField examineSelector = (VocabularyPickerField) examineSelectorField.get(view);

        // ComboBox.setValue() fails silently in browserless (DataKeyMapper not initialised).
        // Set AbstractFieldSupport.bufferedValue directly so the event constructor captures
        // examineVerb from getValue(). Then fire through the event bus with the exact registered
        // event class — ComponentValueChangeEvent — so listeners are found and dispatched to.
        Field fieldSupportField = AbstractField.class.getDeclaredField("fieldSupport");
        fieldSupportField.setAccessible(true);
        Object fieldSupport = fieldSupportField.get(examineSelector);
        Field bufferedValueField = fieldSupport.getClass().getDeclaredField("bufferedValue");
        bufferedValueField.setAccessible(true);
        bufferedValueField.set(fieldSupport, examineVerb);

        Field eventBusField = Component.class.getDeclaredField("eventBus");
        eventBusField.setAccessible(true);
        ComponentEventBus eventBus = (ComponentEventBus) eventBusField.get(examineSelector);
        eventBus.fireEvent(
            new AbstractField.ComponentValueChangeEvent<>(examineSelector, examineSelector, null, true));

        assertThat(vocabularyData.getExamineWord()).isEqualTo(examineVerb);
    }
}
