package com.pdg.adventure.views.directions;

import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.basics.CommandDescriptionData;
import com.pdg.adventure.model.basics.DescriptionData;
import com.pdg.adventure.server.storage.AdventureService;

/**
 * Tests for Vaadin Binder validation and binding logic in DirectionEditorView.
 * Focuses on field binding, validation rules, and data synchronization.
 */
@ExtendWith(MockitoExtension.class)
class DirectionEditorViewBinderTest {

    @Mock
    private AdventureService adventureService;

    private DirectionEditorView view;
    private AdventureData adventureData;
    private LocationData locationData;
    private VocabularyData vocabularyData;
    private DirectionData directionData;
    private Binder<DirectionViewModel> binder;

    @BeforeEach
    void setUp() throws Exception {
        // Create test data
        adventureData = new AdventureData();
        adventureData.setId("adventure-1");

        locationData = new LocationData();
        locationData.setId("location-1");
        locationData.setDescriptionData(new DescriptionData("Test Location", "A test location"));
        locationData.setDirectionsData(new HashSet<>());

        LocationData destination = new LocationData();
        destination.setId("location-2");
        destination.setDescriptionData(new DescriptionData("Destination", "A destination"));

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(locationData.getId(), locationData);
        locations.put(destination.getId(), destination);
        adventureData.setLocationData(locations);

        vocabularyData = new VocabularyData();
        Word goVerb = new Word("go", Word.Type.VERB);
        Word northNoun = new Word("north", Word.Type.NOUN);
        Word darkAdjective = new Word("dark", Word.Type.ADJECTIVE);
        vocabularyData.setWords(List.of(goVerb, northNoun, darkAdjective));
        adventureData.setVocabularyData(vocabularyData);

        directionData = new DirectionData();
        directionData.setId("direction-1");
        directionData.setDestinationId("location-2");
        directionData.setDescriptionData(new DescriptionData("Go north", "You can go north"));
        directionData.setDestinationMustBeMentioned(true);

        CommandData commandData = new CommandData();
        CommandDescriptionData commandDescription = new CommandDescriptionData();
        commandDescription.setVerb(goVerb);
        commandDescription.setNoun(northNoun);
        commandData.setCommandDescription(commandDescription);
        directionData.setCommandData(commandData);

        view = new DirectionEditorView(adventureService);
        locationData.getDirectionsData().add(directionData);

        // Access private binder field using reflection
        Field binderField = DirectionEditorView.class.getDeclaredField("binder");
        binderField.setAccessible(true);
        binder = (Binder<DirectionViewModel>) binderField.get(view);
    }

    @Test
    void binder_shouldBeCreatedInConstructor() {
        // then
        assertThat(binder).isNotNull();
    }

    @Test
    void binder_afterSetData_shouldHaveBean() {
        //given
        view.setUpLoading("direction-1");

        // when
        view.setData(locationData, adventureData);

        // then
        assertThat(binder.getBean()).isNotNull();
    }

    @Test
    void binder_withValidData_shouldPassValidation() {
        // given
        view.setUpLoading("direction-1");

        view.setData(locationData, adventureData);

        // when
        BinderValidationStatus<DirectionViewModel> status = binder.validate();

        // then
        assertThat(status.isOk()).isTrue();
        assertThat(status.getValidationErrors()).isEmpty();
    }

    @Test
    void binder_withoutVerb_shouldFailValidation() throws Exception {
        // given
        view.setUpLoading("direction-1");

        view.setData(locationData, adventureData);

        // Get the view model and remove verb
        DirectionViewModel viewModel = binder.getBean();
        viewModel.setVerb(null);
        binder.readBean(viewModel);

        // when
        BinderValidationStatus<DirectionViewModel> status = binder.validate();

        // then
        assertThat(status.isOk()).isFalse();
        assertThat(status.getValidationErrors()).isNotEmpty();
    }

    @Test
    void binder_hasChanges_shouldReturnFalseAfterReadBean() {
        // given
        view.setUpLoading("direction-1");

        view.setData(locationData, adventureData);

        // when
        DirectionViewModel viewModel = binder.getBean();
        binder.readBean(viewModel);

        // then
        assertThat(binder.hasChanges()).isFalse();
    }

    @Test
    void binder_shouldBindDirectionId() {
        // given
        view.setUpLoading("direction-1");

        view.setData(locationData, adventureData);

        // when
        DirectionViewModel viewModel = binder.getBean();

        // then
        assertThat(viewModel).isNotNull();
        assertThat(viewModel.getId()).isEqualTo("direction-1");
    }

    @Test
    void binder_shouldBindDescriptions() {
        // given
        view.setUpLoading("direction-1");

        view.setData(locationData, adventureData);

        // when
        DirectionViewModel viewModel = binder.getBean();

        // then
        assertThat(viewModel.getShortDescription()).isEqualTo("Go north");
        assertThat(viewModel.getLongDescription()).isEqualTo("You can go north");
    }
}
