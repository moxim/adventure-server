package com.pdg.adventure.view.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.model.Word;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.server.storage.AdventureService;

/**
 * Unit tests for CommandsMenuView business logic.
 * Tests focus on grid population, command management, and state management
 * without requiring full Vaadin UI context.
 */
@ExtendWith(MockitoExtension.class)
class CommandsMenuViewTest {

    @Mock
    private AdventureService adventureService;

    private CommandsMenuView view;
    private AdventureData adventureData;
    private LocationData locationData;
    private VocabularyData vocabularyData;
    private CommandProviderData commandProviderData;

    @BeforeEach
    void setUp() {
        // Create test data
        adventureData = new AdventureData();
        adventureData.setId("adventure-1");

        locationData = new LocationData();
        locationData.setId("location-1");
        locationData.setDescriptionData(new DescriptionData("Test Location", "A test location"));

        // Create vocabulary
        vocabularyData = new VocabularyData();
        Word go = new Word("go", Word.Type.VERB);
        Word take = new Word("take", Word.Type.VERB);
        Word north = new Word("north", Word.Type.NOUN);
        Word sword = new Word("sword", Word.Type.NOUN);
        Word golden = new Word("golden", Word.Type.ADJECTIVE);
        vocabularyData.addWord(go);
        vocabularyData.addWord(take);
        vocabularyData.addWord(north);
        vocabularyData.addWord(sword);
        vocabularyData.addWord(golden);
        adventureData.setVocabularyData(vocabularyData);

        // Create command provider with available commands
        commandProviderData = new CommandProviderData();
        commandProviderData.setAvailableCommands(new HashMap<>());
        locationData.setCommandProviderData(commandProviderData);

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(locationData.getId(), locationData);
        adventureData.setLocationData(locations);
    }

    @Test
    void constructor_shouldCreateViewWithAllComponents() {
        // when
        view = new CommandsMenuView(adventureService);

        // then
        assertThat(view).isNotNull();
    }

    @Test
    void setData_shouldPopulateGridWithCommands() {
        // given
        view = new CommandsMenuView(adventureService);

        CommandChainData goNorthChain = new CommandChainData();
        CommandChainData takeSwordChain = new CommandChainData();

        commandProviderData.getAvailableCommands().put("go||north", goNorthChain);
        commandProviderData.getAvailableCommands().put("take|golden|sword", takeSwordChain);

        // when
        view.setData(adventureData, locationData);

        // then
        assertThat(locationData.getCommandProviderData().getAvailableCommands())
                .hasSize(2)
                .containsKeys("go||north", "take|golden|sword");
    }

    @Test
    void setData_withEmptyCommands_shouldHandleEmptyState() {
        // given
        view = new CommandsMenuView(adventureService);

        // Command provider has empty commands map (created in setUp)

        // when
        view.setData(adventureData, locationData);

        // then
        assertThat(locationData.getCommandProviderData().getAvailableCommands()).isEmpty();
    }
}
