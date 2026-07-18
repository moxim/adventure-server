package com.pdg.adventure.view.command;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
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

        // Add context menu for deleting commands from the chain
        GridContextMenu<CommandData> contextMenu = commandChainGrid.addContextMenu();
        contextMenu.addItem("Delete", event -> {
            event.getItem().ifPresent(this::deleteCommandFromChain);
        });

        VerticalLayout vl1 = new VerticalLayout();
        vl1.add(new Span("Command Chain"));
        vl1.add(commandChainGrid);

        preconditionAndActionHolder = new Span();
        VerticalLayout details = new VerticalLayout(new NativeLabel("Preconditions & Actions"), preconditionAndActionHolder);
        HorizontalLayout hl1 = new HorizontalLayout(vl1, details);

        // The precondition/action editor is built lazily in setData(), once adventureData is
        // available (its action/condition leaf editors dereference adventureData when populated).
        add(commandLayout, hl1, resetBackSaveView);
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
        cancelButton.setEnabled(false);

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

                // Update commandId to the new specification (in case it changed)
                commandId = cvm.getData().getCommandSpecification();

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
        // Use the commandDescriptionData that was updated via the binder
        final CommandDescriptionData updatedCommandDescription = cvm.getData();
        final String newSpecification = updatedCommandDescription.getCommandSpecification();

        final Map<String, CommandChainData> availableCommandsHelper = commandProviderData.getAvailableCommands();

        // If editing an existing command and the specification has changed, remove the old entry
        if (commandId != null && !commandId.isEmpty() && !commandId.equals(newSpecification)) {
            availableCommandsHelper.remove(commandId);
        }

        // Determine if we're editing an existing command or creating a new one
        boolean isEditingExistingCommand = commandId != null && !commandId.isEmpty() &&
                                           commandId.equals(newSpecification);

        final CommandData command = getEditingCommandData(isEditingExistingCommand, updatedCommandDescription);

        // Persist the preconditions and actions from the editor
        preconditionActionEditor.saveToCommand(command);

        final CommandChainData commandChainData = availableCommandsHelper.get(newSpecification);
        if (commandChainData == null) {
            // New command - create new chain
            final CommandChainData chainData = new CommandChainData();
            chainData.getCommands().add(command);
            availableCommandsHelper.put(newSpecification, chainData);
        } else if (!isEditingExistingCommand) {
            // Command specification already exists and we're adding a new variant (not editing existing).
            // Commands with the same description are chained together; the chain executes until one
            // with met preconditions succeeds.
            commandChainData.getCommands().add(command);
        }
        // If isEditingExistingCommand is true, the command is already in the chain and updated in place.
        // The menu grid reflects all of the above on return: navigateBack() re-runs CommandsMenuView.setData().

        return command;
    }

    private @NonNull CommandData getEditingCommandData(final boolean isEditingExistingCommand,
                                                final CommandDescriptionData updatedCommandDescription) {
        CommandData command;
        if (isEditingExistingCommand && commandData != null) {
            // We're editing an existing command with the same specification - update it in place
            command = commandData;
            command.setCommandDescription(updatedCommandDescription);
        } else {
            // We're creating a new command or the specification changed
            command = new CommandData();
//            command.setId(UUID.randomUUID().toString()); // Ensure unique ID for grid
            command.setCommandDescription(updatedCommandDescription);
        }
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
        if (optionalCommandId.isPresent()) {
            // Cold-load (bookmark/refresh) navigation delivers this route parameter still
            // percent-encoded (e.g. "jump%7C%7Csea"); in-app navigate() preserves the raw
            // pipe-delimited value (e.g. "jump||sea"), which may itself contain '%' or '+'
            // characters from vocabulary text. AdventureRouteResolver.decodeRouteParam performs
            // percent-only decoding with graceful fallback for both cases.
            commandId = AdventureRouteResolver.decodeRouteParam(optionalCommandId.get());
            pageTitle = "Edit Command: " + ViewSupporter.formatDescription(new CommandDescriptionData(commandId));
        } else {
            pageTitle = "New Command";
        }
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

        // Find existing command or create new one
        CommandDescriptionData commandDescriptionData;
        if (commandId != null && !commandId.isEmpty()) {
            // Look up existing command by specification (commandId contains the spec like "go|north|")
            CommandChainData commandChain = commandProviderData.getAvailableCommands().get(commandId);
            if (commandChain != null && !commandChain.getCommands().isEmpty()) {
                // Get the command description from the first command in the chain
                commandDescriptionData = commandChain.getCommands().getFirst().getCommandDescription();
            } else {
                // Command not found, create new one with the specification
                commandDescriptionData = new CommandDescriptionData(commandId);
            }
        } else {
            // Creating a new command
            commandDescriptionData = new CommandDescriptionData();
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
            // No more commands in the chain
            commandChainGrid.setDataProvider(new ListDataProvider<>(java.util.Collections.emptyList()));
            commandData = null;
            selectedCommandIndex = -1;
            // Clear the editor by loading an empty command
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
