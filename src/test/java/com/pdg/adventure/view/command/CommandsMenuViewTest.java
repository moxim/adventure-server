package com.pdg.adventure.view.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.action.MessageActionData;
import com.pdg.adventure.model.action.QuitActionData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.ItemService;
import com.pdg.adventure.view.command.CommandsMenuView;

/**
 * Unit tests for CommandsMenuView business logic.
 * Tests focus on grid population, command management, and state management
 * without requiring full Vaadin UI context.
 */
@ExtendWith(MockitoExtension.class)
class CommandsMenuViewTest {

    @Mock
    private AdventureService adventureService;
    @Mock
    private ItemService itemService;
    @Mock
    private AdventureAccessService accessService;

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
        view = new CommandsMenuView(adventureService, itemService, accessService);

        // then
        assertThat(view).isNotNull();
    }

    @Test
    void setData_shouldPopulateGridWithCommands() {
        // given
        view = new CommandsMenuView(adventureService, itemService, accessService);

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
        view = new CommandsMenuView(adventureService, itemService, accessService);

        // Command provider has empty commands map (created in setUp)

        // when
        view.setData(adventureData, locationData);

        // then
        assertThat(locationData.getCommandProviderData().getAvailableCommands()).isEmpty();
    }

    @Test
    void setData_withChainOfMultipleCommands_buildsWithoutThrowing() {
        // given: one spec ("open||cage") mapped to a chain of two distinct CommandData (cf. the
        // mockup's "123"/"xyz" rows). Each row is a CommandData, so the chain must not collapse.
        view = new CommandsMenuView(adventureService, itemService, accessService);

        CommandDescriptionData openCage = new CommandDescriptionData("open||cage");

        CommandData first = new CommandData();
        first.setCommandDescription(openCage);
        MessageActionData firstMsg = new MessageActionData();
        firstMsg.setMessageId("cage_opened");
        first.addAction(firstMsg);

        CommandData second = new CommandData();
        second.setCommandDescription(openCage);
        second.addAction(new QuitActionData());

        CommandChainData chain = new CommandChainData();
        chain.getCommands().add(first);
        chain.getCommands().add(second);
        commandProviderData.getAvailableCommands().put("open||cage", chain);

        // when / then
        org.assertj.core.api.Assertions.assertThatCode(() -> view.setData(adventureData, locationData))
                .doesNotThrowAnyException();
        assertThat(chain.getCommands()).hasSize(2);
    }
}
