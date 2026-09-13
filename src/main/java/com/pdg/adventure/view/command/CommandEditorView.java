package com.pdg.adventure.view.command;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static com.pdg.adventure.model.Word.Type.*;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.ItemService;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;
import com.pdg.adventure.view.component.ResetBackSaveView;
import com.pdg.adventure.view.component.VocabularyPicker;
import com.pdg.adventure.view.component.VocabularyPickerField;
import com.pdg.adventure.view.location.LocationsMainLayout;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

@Route(value = "author/adventures/:adventureId/locations/:locationId/commands/:commandId/edit", layout = LocationsMainLayout.class)
@RouteAlias(value = "author/adventures/:adventureId/locations/:locationId/commands/new", layout = LocationsMainLayout.class)
@RouteAlias(value = "author/adventures/:adventureId/locations/:locationId/items/:itemId/commands/:commandId/edit", layout = LocationsMainLayout.class)
@RouteAlias(value = "author/adventures/:adventureId/locations/:locationId/items/:itemId/commands/new", layout = LocationsMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class CommandEditorView extends VerticalLayout
        implements HasDynamicTitle, BeforeLeaveObserver, BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(CommandEditorView.class);

    private final transient AdventureService adventureService;
    private final transient ItemService itemService;
    private final transient AdventureAccessService accessService;
    private final Binder<CommandViewModel> binder;
    private final VocabularyPicker nounSelector;
    private final VocabularyPicker adjectiveSelector;
    private final VocabularyPicker verbSelector;
    private final Span preconditionAndActionHolder;
    private PreconditionActionEditor preconditionActionEditor;
    private transient String commandId;
    private String pageTitle;
    private Button saveButton;
    private Button resetButton;
    private LocationData locationData;
    private AdventureData adventureData;
    private ItemData itemData;
    private CommandProviderData commandProviderData;
    private transient CommandViewModel cvm;
    private transient CommandData commandData;
    private boolean editorHasChanges = false; // Track if the precondition/action editor has been modified
    private final Grid<CommandData> commandChainGrid; // Grid to display all commands in the chain
    private transient PreconditionActionFormatter chainFormatter; // Renders chain rows as friendly text
    private transient CommandChainData currentCommandChain; // The command chain being edited
    private int selectedCommandIndex = 0; // Which command in the chain we're currently editing

    public CommandEditorView(AdventureService anAdventureService, ItemService anItemService,
                             AdventureAccessService anAccessService) {
        adventureService = anAdventureService;
        itemService = anItemService;
        accessService = anAccessService;
        binder = new Binder<>(CommandViewModel.class);

        verbSelector = new VocabularyPickerField("Verb", "You may filter on verbs.");
        verbSelector.setHelperText("Select at least a verb.");
        adjectiveSelector = new VocabularyPickerField("Adjective", "You may filter on adjectives.");
        nounSelector = new VocabularyPickerField("Noun", "You may filter on nouns.");

        setUpBinding();

        HorizontalLayout commandLayout = new HorizontalLayout(verbSelector, adjectiveSelector, nounSelector);

        final ResetBackSaveView resetBackSaveView = setUpNavidationButtons();

        // Create command chain grid
        commandChainGrid = createCommandChainGrid();

        // Add context menu for reordering and deleting commands from the chain
        GridContextMenu<CommandData> contextMenu = commandChainGrid.addContextMenu();
        GridMenuItem<CommandData> moveUpItem = contextMenu.addItem("Move Up", event -> {
            event.getItem().ifPresent(item -> moveCommandInChain(item, -1));
        });
        GridMenuItem<CommandData> moveDownItem = contextMenu.addItem("Move Down", event -> {
            event.getItem().ifPresent(item -> moveCommandInChain(item, 1));
        });
        contextMenu.addItem("Delete", event -> {
            event.getItem().ifPresent(this::deleteCommandFromChain);
        });
        // Grey out Move Up / Move Down at the chain's respective ends
        contextMenu.setDynamicContentHandler(item -> {
            if (item == null || currentCommandChain == null) {
                return false;
            }
            int index = currentCommandChain.getCommands().indexOf(item);
            moveUpItem.setEnabled(index > 0);
            moveDownItem.setEnabled(index < currentCommandChain.getCommands().size() - 1);
            return true;
        });

        VerticalLayout vl1 = new VerticalLayout();
        vl1.add(new Span("Command Chain"));
        vl1.add(commandChainGrid, resetBackSaveView);

        preconditionAndActionHolder = new Span();
        VerticalLayout details = new VerticalLayout(new NativeLabel("Preconditions & Actions"), preconditionAndActionHolder);
        HorizontalLayout hl1 = new HorizontalLayout(vl1, details);

        add(commandLayout, hl1);
    }

    private Grid<CommandData> createCommandChainGrid() {
        final Grid<CommandData> newCommandChainGrid = new Grid<>(CommandData.class, false);

        newCommandChainGrid.addColumn(this::firstPreconditionLabel).setHeader("First Precondition").setAutoWidth(true);
        newCommandChainGrid.addColumn(this::firstActionLabel).setHeader("First Action").setAutoWidth(true);

        newCommandChainGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        newCommandChainGrid.setMaxHeight("300px"); // Limit height so it doesn't dominate the UI
        newCommandChainGrid.setMinWidth("630px");
        newCommandChainGrid.addSelectionListener(selection -> {
            selection.getFirstSelectedItem().ifPresent(selectedCommand -> {
                // Find the index of the selected command
                if (currentCommandChain != null) {
                    selectedCommandIndex = currentCommandChain.getCommands().indexOf(selectedCommand);
                    if (selectedCommandIndex >= 0) {
                        commandData = selectedCommand;
                        // Show the precondition/action editor for this command
                        preconditionActionEditor.setCommand(commandData);
                    }
                }
            });
        });
        return newCommandChainGrid;
    }

    /** Label for the command-chain grid's "First Action" column: friendly text, never a class name. */
    String firstActionLabel(CommandData cmd) {
        if (cmd.getActions().isEmpty()) {
            return "none";
        }
        return chainFormatter.formatAction(cmd.getActions().getFirst());
    }

    /** Label for the command-chain grid's "First Precondition" column: friendly text, never a class name. */
    String firstPreconditionLabel(CommandData cmd) {
        if (cmd.getPreConditions() != null && !cmd.getPreConditions().isEmpty()) {
            try {
                return chainFormatter.formatCondition(cmd.getPreConditions().getFirst());
            } catch (UnsupportedOperationException _) {
                return "none";
            }
        }
        return "none";
    }

    private void setUpBinding() {
        binder.forField(verbSelector).asRequired("Verb is required")
              .withValidator(word -> word != null && !word.getText().isEmpty(), "Please select a verb with text")
              .bind(CommandViewModel::getVerb, CommandViewModel::setVerb);
        binder.forField(adjectiveSelector).bind(CommandViewModel::getAdjective, CommandViewModel::setAdjective);
        binder.forField(nounSelector).bind(CommandViewModel::getNoun, CommandViewModel::setNoun);

        binder.addStatusChangeListener(event -> {
            updateSaveButtonState();
            resetButton.setEnabled(event.getBinder().hasChanges() || editorHasChanges);
        });
    }

    private ResetBackSaveView setUpNavidationButtons() {
        final ResetBackSaveView resetBackSaveView = new ResetBackSaveView();

        Button backButton = resetBackSaveView.getBack();
        backButton.addClickShortcut(Key.ESCAPE);
        saveButton = resetBackSaveView.getSave();
        saveButton.setEnabled(false);
        resetButton = resetBackSaveView.getReset();
        resetButton.setEnabled(false);
        Button cancelButton = resetBackSaveView.getCancel();
        cancelButton.setEnabled(true);

        backButton.addClickListener(_ -> navigateBack());
        saveButton.addClickListener(_ -> validateSave(commandProviderData));
        resetButton.addClickListener(_ -> {
            binder.readBean(cvm);
            preconditionActionEditor.setCommand(commandData != null ? commandData : new CommandData());
            editorHasChanges = false;
            resetButton.setEnabled(false);
        });
        resetBackSaveView.getCancel().addClickShortcut(Key.ESCAPE);

        return resetBackSaveView;
    }

    private void navigateBack() {
        if (itemData != null) {
            UI.getCurrent().navigate(CommandsMenuView.class, new RouteParameters(
                      new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()),
                      new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                      new RouteParam(RouteIds.ITEM_ID.getValue(), itemData.getId())));
        } else {
            UI.getCurrent().navigate(CommandsMenuView.class, new RouteParameters(
                      new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                      new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())));
        }
    }

    /**
     * Update the save button state based on binder and action editor validity.
     */
    private void updateSaveButtonState() {
        boolean binderValid = binder.isValid();
        boolean binderHasChanges = binder.hasChanges();

        // Save button should be enabled only if:
        // 1. (Binder has changes OR the precondition/action editor has changes) AND binder is valid
        // 2. The precondition/action editor is valid
        saveButton.setEnabled((binderHasChanges || editorHasChanges) && binderValid && preconditionActionEditor.validate());
    }

    private void validateSave(CommandProviderData aCommandProviderData) {
        try {
            // Validate the precondition/action editor
            if (!preconditionActionEditor.validate()) {
                // Validation failed, don't save
                return;
            }

            if (binder.validate().isOk()) {
                binder.writeBean(cvm);
                commandData = swivelTheSaveButton();
                if (itemData != null) {
                    itemService.saveItem(itemData);
                } else {
                    adventureService.saveLocationData(locationData);
                }

                // Reload the command chain from the saved data
                currentCommandChain = commandProviderData.getAvailableCommands().get(commandId);

                // Refresh the command chain grid to show updated data
                if (currentCommandChain != null && !currentCommandChain.getCommands().isEmpty()) {
                    ListDataProvider<CommandData> dataProvider = new ListDataProvider<>(
                            currentCommandChain.getCommands());
                    commandChainGrid.setDataProvider(dataProvider);
                    // Re-select the current command
                    if (selectedCommandIndex >= 0 && selectedCommandIndex < currentCommandChain.getCommands().size()) {
                        commandChainGrid.select(currentCommandChain.getCommands().get(selectedCommandIndex));
                    }
                } else {
                    commandChainGrid.setDataProvider(new ListDataProvider<>(java.util.Collections.emptyList()));
                }

                // Reset change tracking flags after successful save
                editorHasChanges = false;

                navigateBack();
            }
        } catch (ValidationException e) {
            LOG.error(e.getMessage());
        }
    }

    private CommandData swivelTheSaveButton() {
        final CommandDescriptionData updatedCommandDescription = cvm.getData();

        CommandData command = (commandData != null) ? commandData : new CommandData();
        command.setCommandDescription(updatedCommandDescription);

        // Persist the preconditions and actions from the editor
        preconditionActionEditor.saveToCommand(command);

        CommandChainData chainData = (commandId != null && !commandId.isEmpty())
                ? commandProviderData.getAvailableCommands().get(commandId)
                : null;

        if (chainData != null) {
            // Editing a command whose chain we already know: it stays there, in place - the
            // chain's id never changes just because the trigger description did, so siblings
            // sharing this chain are never dropped by a save that changes verb/adjective/noun.
            if (!chainData.getCommands().contains(command)) {
                chainData.getCommands().add(command);
            }
        } else {
            // Brand-new command: joins an existing chain with a matching trigger, or starts one.
            commandProviderData.add(command);
        }

        commandId = commandProviderData.findChainIdContaining(command).orElse(commandId);
        return command;
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    /**
     * Test/loader seam: set the id (command specification) of the command chain to edit.
     * Mirrors {@code DirectionEditorView.setUpLoading(String)}; normally {@link #beforeEnter}
     * derives this from the route parameters.
     */
    protected void setUpLoading(String aCommandId) {
        commandId = aCommandId;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventureOrForward(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            return;
        }
        Optional<LocationData> resolvedLocation = AdventureRouteResolver.resolveLocationOrForward(resolvedAdventure.get(), event);
        if (resolvedLocation.isEmpty()) {
            return;
        }
        final Optional<String> optionalItemId = event.getRouteParameters().get(RouteIds.ITEM_ID.getValue());
        Optional<ItemData> resolvedItem = Optional.empty();
        if (optionalItemId.isPresent()) {
            resolvedItem = AdventureRouteResolver.resolveItemOrForward(
                    resolvedAdventure.get(), resolvedLocation.get(), event);
            if (resolvedItem.isEmpty()) {
                return;
            }
        }
        final Optional<String> optionalCommandId = event.getRouteParameters().get(RouteIds.COMMAND_ID.getValue());
        // Cold-load (bookmark/refresh) navigation can deliver this route parameter percent-
        // encoded; in-app navigate() preserves the raw value. AdventureRouteResolver
        // .decodeRouteParam performs percent-only decoding with graceful fallback for both.
        // commandId is a chain's own stable id (opaque), not anything derived from its
        // trigger - pageTitle is set from the actual resolved chain, in populate() below.
        commandId = optionalCommandId.map(AdventureRouteResolver::decodeRouteParam).orElse(null);
        if (resolvedItem.isPresent()) {
            setData(resolvedAdventure.get(), resolvedLocation.get(), resolvedItem.get());
        } else {
            setData(resolvedAdventure.get(), resolvedLocation.get());
        }
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        AdventuresMainLayout.checkIfUserWantsToLeavePage(event, binder.hasChanges() || editorHasChanges);
    }

    private void setData(AdventureData anAdventureData, LocationData aLocationData, ItemData anItemData) {
        itemData = anItemData;
        populate(anAdventureData, aLocationData);
    }

    private void setData(AdventureData anAdventureData, LocationData aLocationData) {
        itemData = null;
        populate(anAdventureData, aLocationData);
    }

    private void populate(AdventureData anAdventureData, LocationData aLocationData) {
        adventureData = anAdventureData;
        locationData = aLocationData;
        chainFormatter = new PreconditionActionFormatter(adventureData);

        commandProviderData = itemData != null
                ? itemData.getCommandProviderData()
                : locationData.getCommandProviderData();

        // Build the precondition/action editor now that adventureData is available (its
        // action/condition leaf editors dereference adventureData when a command is loaded).
        if (preconditionActionEditor == null) {
            preconditionActionEditor = new PreconditionActionEditor(adventureData);
            preconditionActionEditor.setOnChange(() -> {
                editorHasChanges = true;
                updateSaveButtonState();
                resetButton.setEnabled(true);
            });
            preconditionAndActionHolder.add(preconditionActionEditor);
        }

        // Find existing command or create new one. commandId is a chain's own stable id, not
        // anything derived from its trigger, so there's no fallback parse-as-spec path anymore.
        CommandDescriptionData commandDescriptionData;
        if (commandId != null && !commandId.isEmpty()) {
            CommandChainData commandChain = commandProviderData.getAvailableCommands().get(commandId);
            if (commandChain != null && !commandChain.getCommands().isEmpty()) {
                commandDescriptionData = commandChain.getCommands().getFirst().getCommandDescription();
                pageTitle = "Edit Command: " + ViewSupporter.getDescriptionText(commandDescriptionData);
            } else {
                commandDescriptionData = new CommandDescriptionData();
                pageTitle = "New Command";
            }
        } else {
            commandDescriptionData = new CommandDescriptionData();
            pageTitle = "New Command";
        }

        VocabularyData vocabularyData = adventureData.getVocabularyData();
        nounSelector.populate(
                vocabularyData.getWords(NOUN).stream().filter(word -> word.getSynonym() == null).toList());
        adjectiveSelector.populate(
                vocabularyData.getWords(ADJECTIVE).stream().filter(word -> word.getSynonym() == null).toList());
        verbSelector.populate(
                vocabularyData.getWords(VERB).stream().filter(word -> word.getSynonym() == null).toList());

        saveButton.setEnabled(false);
        cvm = new CommandViewModel(commandDescriptionData);
        binder.readBean(cvm);

        // Reset editor change tracking
        editorHasChanges = false;

        // Populate the command chain grid and load the selected command into the editor
        populateCommandChain();
    }

    /**
     * Populate the command chain grid and load the selected command into the precondition/action editor.
     */
    private void populateCommandChain() {
        // Get the command chain and populate the grid
        currentCommandChain = null;

        if (commandId != null && !commandId.isEmpty()) {
            currentCommandChain = commandProviderData.getAvailableCommands().get(commandId);
            if (currentCommandChain != null && !currentCommandChain.getCommands().isEmpty()) {
                // Populate the grid with all commands in the chain
                // Create a data provider with explicit identity based on Command ID
                ListDataProvider<CommandData> dataProvider = new ListDataProvider<>(currentCommandChain.getCommands());
                commandChainGrid.setDataProvider(dataProvider);

                // Select the command at the current index (or first if index is out of bounds)
                if (selectedCommandIndex < 0 || selectedCommandIndex >= currentCommandChain.getCommands().size()) {
                    selectedCommandIndex = 0;
                }
                commandData = currentCommandChain.getCommands().get(selectedCommandIndex);
                commandChainGrid.select(commandData);
            } else {
                // No commands in the chain yet
                commandChainGrid.setDataProvider(new ListDataProvider<>(java.util.Collections.emptyList()));
            }
        } else {
            // Creating a new command - no chain yet
            commandChainGrid.setDataProvider(new ListDataProvider<>(java.util.Collections.emptyList()));
        }

        // Show the precondition/action editor for the selected command (or an empty command for the new-command path)
        preconditionActionEditor.setCommand(commandData != null ? commandData : new CommandData());
    }

    /**
     * Move a command within the chain by the given offset (-1 for up, +1 for down),
     * refreshing the grid and keeping the moved command selected.
     */
    private void moveCommandInChain(CommandData commandToMove, int delta) {
        if (currentCommandChain == null) {
            return;
        }

        List<CommandData> commands = currentCommandChain.getCommands();
        int index = commands.indexOf(commandToMove);
        int newIndex = index + delta;
        if (index < 0 || newIndex < 0 || newIndex >= commands.size()) {
            return;
        }

        commands.remove(index);
        commands.add(newIndex, commandToMove);

        // Refresh the grid to reflect the new order, keeping the moved command selected
        commandChainGrid.setDataProvider(new ListDataProvider<>(commands));
        selectedCommandIndex = newIndex;
        commandChainGrid.select(commandToMove);

        editorHasChanges = true;
        updateSaveButtonState();
        resetButton.setEnabled(true);
    }

    /**
     * Delete a command from the command chain.
     * Handles edge cases like deleting the last command or currently selected command.
     */
    private void deleteCommandFromChain(CommandData commandToDelete) {
        if (currentCommandChain == null || commandToDelete == null) {
            return;
        }

        // Remove the command from the chain
        currentCommandChain.getCommands().remove(commandToDelete);

        // Update the grid to reflect the deletion
        if (currentCommandChain.getCommands().isEmpty()) {
            // No more commands in the chain: drop the chain entry itself (mirrors
            // CommandsMenuView.deleteCommand) so a later Save can't silently resurrect it by
            // reusing the now-empty chain still found via commandId. Also reset the trigger
            // fields to blank, so the required-verb validation keeps Save disabled until the
            // author deliberately picks a new trigger - otherwise Save stays clickable with the
            // deleted command's old verb/adjective/noun still selected and would create a
            // fresh, content-free command under that same trigger instead of doing nothing.
            if (commandId != null) {
                commandProviderData.getAvailableCommands().remove(commandId);
            }
            commandChainGrid.setDataProvider(new ListDataProvider<>(java.util.Collections.emptyList()));
            commandData = null;
            commandId = null;
            currentCommandChain = null;
            selectedCommandIndex = -1;
            cvm = new CommandViewModel(new CommandDescriptionData());
            binder.readBean(cvm);
            preconditionActionEditor.setCommand(new CommandData());
        } else {
            // Refresh the grid with remaining commands
            ListDataProvider<CommandData> dataProvider = new ListDataProvider<>(currentCommandChain.getCommands());
            commandChainGrid.setDataProvider(dataProvider);

            // Adjust selectedCommandIndex if needed
            if (selectedCommandIndex >= currentCommandChain.getCommands().size()) {
                selectedCommandIndex = currentCommandChain.getCommands().size() - 1;
            }
            if (selectedCommandIndex < 0) {
                selectedCommandIndex = 0;
            }

            // Select the new command at the adjusted index
            commandData = currentCommandChain.getCommands().get(selectedCommandIndex);
            commandChainGrid.select(commandData);

            // Show the precondition/action editor for the newly selected command
            preconditionActionEditor.setCommand(commandData);
        }

        // Mark as having changes so save button enables
        editorHasChanges = true;
        updateSaveButtonState();
        resetButton.setEnabled(true);
    }
}
