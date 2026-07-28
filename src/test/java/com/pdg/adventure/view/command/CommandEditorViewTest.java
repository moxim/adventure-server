package com.pdg.adventure.view.command;

import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.action.MessageActionData;
import com.pdg.adventure.model.action.MovePlayerActionData;
import com.pdg.adventure.model.condition.WornConditionData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.ItemService;
import com.pdg.adventure.view.support.RouteIds;

/**
 * Unit tests for CommandEditorView business logic.
 * Tests focus on validation, data population, and state management
 * without requiring full Vaadin UI context.
 */
@ExtendWith(MockitoExtension.class)
class CommandEditorViewTest {

    @Mock
    private AdventureService adventureService;
    @Mock
    private ItemService itemService;
    @Mock
    private AdventureAccessService accessService;

    private CommandEditorView view;
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
        Map<String, CommandChainData> availableCommands = new HashMap<>();

        CommandChainData goNorthChain = new CommandChainData();
        availableCommands.put("go||north", goNorthChain);

        CommandChainData takeSwordChain = new CommandChainData();
        availableCommands.put("take|golden|sword", takeSwordChain);

        commandProviderData.setAvailableCommands(availableCommands);
        locationData.setCommandProviderData(commandProviderData);

        Map<String, LocationData> locations = new HashMap<>();
        locations.put(locationData.getId(), locationData);
        adventureData.setLocationData(locations);

        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    /** Location-scoped beforeEnter: no ITEM_ID, optional COMMAND_ID. */
    private void enterWithCommandId(String aCommandId) {
        RouteParam[] params = aCommandId == null
                ? new RouteParam[] {
                        new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()),
                        new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId())}
                : new RouteParam[] {
                        new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()),
                        new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                        new RouteParam(RouteIds.COMMAND_ID.getValue(), aCommandId)};
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(params));
        when(accessService.findAdventureById(eq(adventureData.getId()), any(UserData.class)))
                .thenReturn(Optional.of(adventureData));
        view.beforeEnter(event);
    }

    @Test
    void constructor_shouldCreateViewWithAllComponents() {
        // when
        view = new CommandEditorView(adventureService, itemService, accessService);

        // then
        assertThat(view).isNotNull();
    }

    @Test
    void setData_shouldPopulateAdventureAndLocationData() {
        // given
        view = new CommandEditorView(adventureService, itemService, accessService);

        // when
        enterWithCommandId(null);

        // then
        // View should be populated with data
        // No exceptions should be thrown
        assertThat(view).isNotNull();
    }

    @Test
    void getPageTitle_shouldReturnNullBeforeRouteEnter() {
        // given
        view = new CommandEditorView(adventureService, itemService, accessService);

        // when
        String title = view.getPageTitle();

        // then
        assertThat(title).isNull();
    }

    @Test
    void loadingExistingCommandWithAction_buildsEditorWithoutThrowing() {
        // Regression: opening an existing command whose first action is a real action must build
        // the action editor without throwing. Drives setData -> (lazy) PreconditionActionEditor ->
        // setCommand -> ActionListEditor.setActions -> ActionEditorFactory.createEditor(action,
        // adventureData) -> buildUI() -- the chain that previously NPE'd on a null adventureData.
        Word go = vocabularyData.getWords().stream()
                .filter(w -> "go".equals(w.getText())).findFirst().orElseThrow();
        Word north = vocabularyData.getWords().stream()
                .filter(w -> "north".equals(w.getText())).findFirst().orElseThrow();

        CommandDescriptionData commandDescription = new CommandDescriptionData();
        commandDescription.setVerb(go);
        commandDescription.setNoun(north);

        MovePlayerActionData moveAction = new MovePlayerActionData();
        moveAction.setLocationId("location-1");

        CommandData commandWithAction = new CommandData();
        commandWithAction.setCommandDescription(commandDescription);
        commandWithAction.addAction(moveAction);
        commandProviderData.add(commandWithAction);
        String chainId = commandProviderData.findChainIdContaining(commandWithAction).orElseThrow();

        view = new CommandEditorView(adventureService, itemService, accessService);

        assertThatCode(() -> enterWithCommandId(chainId))
                .doesNotThrowAnyException();
        // and: the existing chain was actually found and loaded, not silently treated as new
        assertThat(view.getPageTitle()).startsWith("Edit Command:");
    }

    @Test
    void commandChainGridLabels_useFriendlyText_notInternalClassNames() {
        // given: a command whose first action is a Message and first precondition is Worn,
        // added to the provider so setData() loads it by its chain id and builds the formatter.
        Word go = vocabularyData.getWords().stream()
                .filter(w -> "go".equals(w.getText())).findFirst().orElseThrow();
        Word north = vocabularyData.getWords().stream()
                .filter(w -> "north".equals(w.getText())).findFirst().orElseThrow();

        CommandDescriptionData description = new CommandDescriptionData();
        description.setVerb(go);
        description.setNoun(north);

        MessageActionData message = new MessageActionData();
        message.setMessageId("welcome");
        WornConditionData worn = new WornConditionData();
        worn.setThingId("cloak-id");

        CommandData command = new CommandData();
        command.setCommandDescription(description);
        command.addAction(message);
        command.getPreConditions().add(worn);
        commandProviderData.add(command);
        String chainId = commandProviderData.findChainIdContaining(command).orElseThrow();

        view = new CommandEditorView(adventureService, itemService, accessService);
        enterWithCommandId(chainId);

        // when
        String actionLabel = view.firstActionLabel(command);
        String preconditionLabel = view.firstPreconditionLabel(command);

        // then: the grid shows human-readable text, never internal *Data class names
        assertThat(actionLabel).doesNotContain("ActionData").startsWith("MESSAGE");
        assertThat(preconditionLabel).doesNotContain("ConditionData").startsWith("WORN");
    }

    @Test
    void editingOneCommandsTriggerInAMultiCommandChain_keepsItsSiblings() throws Exception {
        // given: two commands sharing one chain (same verb/noun -> same chain on add())
        Word go = vocabularyData.getWords().stream()
                .filter(w -> "go".equals(w.getText())).findFirst().orElseThrow();
        Word north = vocabularyData.getWords().stream()
                .filter(w -> "north".equals(w.getText())).findFirst().orElseThrow();
        Word take = vocabularyData.getWords().stream()
                .filter(w -> "take".equals(w.getText())).findFirst().orElseThrow();

        CommandData first = new CommandData(new CommandDescriptionData(go, null, north));
        first.setActions(java.util.List.of(new MessageActionData()));
        CommandData second = new CommandData(new CommandDescriptionData(go, null, north));
        second.setActions(java.util.List.of(new MessageActionData()));
        commandProviderData.add(first);
        commandProviderData.add(second);
        String chainId = commandProviderData.findChainIdContaining(first).orElseThrow();

        view = new CommandEditorView(adventureService, itemService, accessService);
        enterWithCommandId(chainId); // loads the chain; selects `first` (index 0) into the editor

        // when: the selected command's verb is changed (as if the user re-picked it) and saved
        Field cvmField = CommandEditorView.class.getDeclaredField("cvm");
        cvmField.setAccessible(true);
        CommandViewModel cvm = (CommandViewModel) cvmField.get(view);
        cvm.setVerb(take);

        Method swivelTheSaveButton = CommandEditorView.class.getDeclaredMethod("swivelTheSaveButton");
        swivelTheSaveButton.setAccessible(true);
        swivelTheSaveButton.invoke(view);

        // then: both commands are still in the SAME chain - the sibling wasn't dropped
        CommandChainData chain = commandProviderData.getAvailableCommands().get(chainId);
        assertThat(chain).isNotNull();
        assertThat(chain.getCommands()).hasSize(2);
        assertThat(chain.getCommands()).contains(second);
    }
}
