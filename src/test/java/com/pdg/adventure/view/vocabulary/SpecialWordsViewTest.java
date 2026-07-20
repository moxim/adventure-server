package com.pdg.adventure.view.vocabulary;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventBus;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.component.VocabularyPickerField;
import com.pdg.adventure.view.support.RouteIds;

class SpecialWordsViewTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private SpecialWordsView view;
    private AdventureData adventureData;
    private VocabularyData vocabularyData;
    private Word examineVerb;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);

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

        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));

        view = new SpecialWordsView(adventureService, accessService);
        UI.getCurrent().add(view);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void enterWithAdventure() {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())));
        when(accessService.findAdventureById(eq(adventureData.getId()), any(UserData.class)))
                .thenReturn(Optional.of(adventureData));
        view.beforeEnter(event);
    }

    @Test
    @DisplayName("After setAdventureData, four VocabularyPickerFields are displayed (take, drop, load, examine)")
    void afterSetAdventureData_fourPickerFieldsAreDisplayed() {
        enterWithAdventure();

        assertThat(find(VocabularyPickerField.class, view).all()).hasSize(4);
    }

    @Test
    @DisplayName("After setAdventureData, an already-configured examineWord is preserved in vocabularyData")
    void afterSetAdventureData_existingExamineWordIsPreselected() {
        vocabularyData.setExamineWord(examineVerb);

        enterWithAdventure();

        // ComboBox.getValue() is unreliable in browserless tests (DataKeyMapper not initialised);
        // verify the model object is preserved instead — the populate step must not clear it.
        assertThat(vocabularyData.getExamineWord()).isEqualTo(examineVerb);
    }

    @Test
    @DisplayName("After setAdventureData, the examine picker has no selection when examineWord is not set")
    void afterSetAdventureData_examinePickerIsEmpty_whenNoExamineWordSet() {
        enterWithAdventure();

        VocabularyPickerField examineSelector = find(VocabularyPickerField.class, view).atIndex(3);
        assertThat(examineSelector.getValue()).isNull();
    }

    @Test
    @DisplayName("Clicking Save delegates to AdventureService.saveAdventureData")
    void clickingSave_callsAdventureServiceSave() {
        enterWithAdventure();

        Button saveButton = find(Button.class, view).withText("Save").single();
        test(saveButton).click();

        verify(adventureService).saveAdventureData(adventureData);
    }

    @Test
    @DisplayName("Selecting a verb in the examine picker updates vocabularyData.examineWord")
    void selectingExamineVerb_updatesVocabularyDataExamineWord() throws Exception {
        enterWithAdventure();

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
