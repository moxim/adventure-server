package com.pdg.adventure.view.command;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

import static com.pdg.adventure.model.Word.Type.*;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.action.ActionData;
import com.pdg.adventure.model.basic.CommandDescriptionData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;
import com.pdg.adventure.view.command.action.ActionEditorComponent;
import com.pdg.adventure.view.command.action.ActionEditorFactory;
import com.pdg.adventure.view.command.action.ActionSelector;
import com.pdg.adventure.view.component.ResetBackSaveView;
import com.pdg.adventure.view.component.VocabularyPicker;
import com.pdg.adventure.view.component.VocabularyPickerField;
import com.pdg.adventure.view.location.LocationsMainLayout;
import com.pdg.adventure.view.support.RouteIds;

@Route(value = "adventures/:adventureId/locations/:locationId/commands/:commandId/edit", layout = LocationsMainLayout.class)
@RouteAlias(value = "adventures/:adventureId/locations/:locationId/commands/new", layout = LocationsMainLayout.class)
public class CommandEditorView extends VerticalLayout
        implements HasDynamicTitle, BeforeLeaveObserver, BeforeEnterObserver {

    private static final Logger LOG = LoggerFactory.getLogger(CommandEditorView.class);

    private transient final AdventureService adventureService;
    private final Binder<CommandViewModel> binder;
    private final VocabularyPicker nounSelector;
    private final VocabularyPicker adjectiveSelector;
    private final VocabularyPicker verbSelector;
    private final VerticalLayout actionEditorContainer;
    private transient String commandId;
    private String pageTitle;
    private Button saveButton;
    private Button resetButton;
    private LocationData locationData;
    private GridListDataView<CommandDescriptionAdapter> gridListDataView;
    private AdventureData adventureData;
    private CommandProviderData commandProviderData;
    private transient CommandViewModel cvm;
    private transient ActionEditorComponent actionEditor;
    private transient CommandData commandData;
    private transient ActionData originalActionData; // Store original action for reset
    private boolean actionEditorHasChanges = false; // Track if action editor has been modified
    private final Grid<CommandData> commandChainGrid; // Grid to display all commands in the chain
    private transient CommandChainData currentCommandChain; // The command chain being edited
    private int selectedCommandIndex = 0; // Which command in the chain we're currently editing

    public CommandEditorView(AdventureService anAdventureService) {
        adventureService = anAdventureService;
        binder = new Binder<>(CommandViewModel.class);

        verbSelector = new VocabularyPickerField("Verb", "You may filter on verbs.");
        verbSelector.setHelperText("Select at least a verb.");
        adjectiveSelector = new VocabularyPickerField("Adjective", "You may filter on adjectives.");
        nounSelector = new VocabularyPickerField("Noun", "You may filter on nouns.");

        binder.forField(verbSelector).asRequired("Verb is required")
              .withValidator(word -> word != null && !word.getText().isEmpty(), "Please select a verb with text")
              .bind(CommandViewModel::getVerb, CommandViewModel::setVerb);
        binder.forField(adjectiveSelector).bind(CommandViewModel::getAdjective, CommandViewModel::setAdjective);
        binder.forField(nounSelector).bind(CommandViewModel::getNoun, CommandViewModel::setNoun);

        binder.addStatusChangeListener(event -> {
            updateSaveButtonState();
            resetButton.setEnabled(event.getBinder().hasChanges() || actionEditorHasChanges);
        });

        HorizontalLayout commandLayout = new HorizontalLayout(verbSelector, adjectiveSelector, nounSelector);

        final ResetBackSaveView resetBackSaveView = setUpNavidationButtons();

        // Create command chain grid
        commandChainGrid = new Grid<>(CommandData.class, false);

        commandChainGrid.addColumn(cmd -> {
            if (cmd.getAction() != null) {
                return cmd.getAction().getActionName();
            }
            return "none";
        }).setHeader("Primary Action").setAutoWidth(true);

        commandChainGrid.addColumn(cmd -> {
            if (cmd.getPreConditions() != null && !cmd.getPreConditions().isEmpty()) {
                try {
                    return cmd.getPreConditions().get(0).getName();
                } catch (UnsupportedOperationException e) {
                    return "none";
                }
            }
            return "none";
        }).setHeader("First Precondition").setAutoWidth(true);

        commandChainGrid.addColumn(cmd -> {
            if (cmd.getFollowUpActions() != null && !cmd.getFollowUpActions().isEmpty()) {
                return cmd.getFollowUpActions().get(0).getActionName();
            }
            return "none";
        }).setHeader("First Followup Action").setAutoWidth(true);

        commandChainGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        commandChainGrid.setMaxHeight("300px"); // Limit height so it doesn't dominate the UI
        commandChainGrid.setMinWidth("630px");
        commandChainGrid.addSelectionListener(selection -> {
            selection.getFirstSelectedItem().ifPresent(selectedCommand -> {
                // Find the index of the selected command
                if (currentCommandChain != null) {
                    selectedCommandIndex = currentCommandChain.getCommands().indexOf(selectedCommand);
                    if (selectedCommandIndex >= 0) {
                        commandData = selectedCommand;
                        // Show the action editor for this command
                        showActionEditorForCommand(commandData.getAction());
                    }
                }
            });
        });

        // Add context menu for deleting commands from the chain
        GridContextMenu<CommandData> contextMenu = commandChainGrid.addContextMenu();
        contextMenu.addItem("Delete", event -> {
            event.getItem().ifPresent(this::deleteCommandFromChain);
        });

        // Create action editor container
        actionEditorContainer = new VerticalLayout();
        actionEditorContainer.setPadding(false);
        actionEditorContainer.setSpacing(true);

        VerticalLayout vl1 = new VerticalLayout();
        vl1.add(new Span("Command Chain"));
        vl1.add(commandChainGrid);
        vl1.add(new Span("Selected Command Action"));
        vl1.add(actionEditorContainer);
        Details details = new Details("Preconditions & Actions", vl1);

        add(commandLayout, details, resetBackSaveView);
    }

    private ResetBackSaveView setUpNavidationButtons() {
        final ResetBackSaveView resetBackSaveView = new ResetBackSaveView();

        Button backButton = resetBackSaveView.getBack();
        saveButton = resetBackSaveView.getSave();
        saveButton.setEnabled(false);
        resetButton = resetBackSaveView.getReset();
        resetButton.setEnabled(false);
        Button cancelButton = resetBackSaveView.getCancel();
        cancelButton.setEnabled(false);

        backButton.addClickListener(event -> navigateBack());
        saveButton.addClickListener(event -> validateSave(commandProviderData));
        resetButton.addClickListener(event -> {
            binder.readBean(cvm);
            resetActionEditor();
            actionEditorHasChanges = false;
            resetButton.setEnabled(false);
        });
        resetBackSaveView.getCancel().addClickShortcut(Key.ESCAPE);

        return resetBackSaveView;
    }

    private void navigateBack() {
        UI.getCurrent().navigate(CommandsMenuView.class, new RouteParameters(
                  new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                  new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())))
          .ifPresent(e -> e.setData(adventureData, locationData));
    }

    /**
     * Update the save button state based on binder and action editor validity.
     */
    private void updateSaveButtonState() {
        boolean binderValid = binder.isValid();
        boolean binderHasChanges = binder.hasChanges();
        boolean actionEditorValid = actionEditor == null || actionEditor.validate();

        // Save button should be enabled only if:
        // 1. (Binder has changes OR action editor has changes) AND binder is valid
        // 2. Action editor is valid (if present)
        saveButton.setEnabled((binderHasChanges || actionEditorHasChanges) && binderValid && actionEditorValid);
    }

    /**
     * Attach validation listeners to the action editor's input components.
     * This ensures the save button state updates when action editor fields change.
     */
    private void attachActionEditorListeners(ActionEditorComponent editor) {
        if (editor == null) {
            return;
        }

        // Add a value change listener to all input components in the editor
        editor.getChildren().forEach(component -> {
            if (component instanceof com.vaadin.flow.component.HasValue) {
                ((com.vaadin.flow.component.HasValue<?, ?>) component).addValueChangeListener(e -> {
                    // Mark that the action editor has changes
                    if (!e.isFromClient()) {
                        // This is a programmatic change (initial value setting), don't mark as changed
                        return;
                    }
                    actionEditorHasChanges = true;
                    updateSaveButtonState();
                    resetButton.setEnabled(true);
                });
            }
        });
    }

    private void validateSave(CommandProviderData aCommandProviderData) {
        try {
            // Validate action editor if present
            if (actionEditor != null && !actionEditor.validate()) {
                // Validation failed, don't save
                return;
            }

            if (binder.validate().isOk()) {
                binder.writeBean(cvm);
                commandData = swivelTheSaveButton(gridListDataView);
                adventureService.saveLocationData(locationData);

                // Update commandId to the new specification (in case it changed)
                commandId = cvm.getData().getCommandSpecification();

                // Update the original action data to reflect what was just saved
                if (actionEditor != null) {
                    originalActionData = actionEditor.getActionData();
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
                actionEditorHasChanges = false;

                navigateBack();
            }
        } catch (ValidationException e) {
            LOG.error(e.getMessage());
        }
    }

    private CommandData swivelTheSaveButton(GridListDataView<CommandDescriptionAdapter> aGridListDataView) {
        // Use the commandDescriptionData that was updated via the binder
        final CommandDescriptionData updatedCommandDescription = cvm.getData();
        final String newSpecification = updatedCommandDescription.getCommandSpecification();

        final Map<String, CommandChainData> availableCommandsHelper = commandProviderData.getAvailableCommands();

        // If editing an existing command and the specification has changed, remove the old entry
        if (commandId != null && !commandId.isEmpty() && !commandId.equals(newSpecification)) {
            availableCommandsHelper.remove(commandId);
            // Remove old item from grid
            aGridListDataView.getItems().filter(item -> item.getShortDescription().equals(commandId)).findFirst()
                             .ifPresent(aGridListDataView::removeItem);
        }

        // Determine if we're editing an existing command or creating a new one
        boolean isEditingExistingCommand = commandId != null && !commandId.isEmpty() &&
                                           commandId.equals(newSpecification);

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

        // Use the action from the action editor if present
        if (actionEditor != null) {
            ActionData action = actionEditor.getActionData();
            command.setAction(action);
        }

        final CommandChainData commandChainData = availableCommandsHelper.get(newSpecification);
        if (commandChainData == null) {
            // New command - create new chain
            final CommandChainData chainData = new CommandChainData();
            chainData.getCommands().add(command);
            availableCommandsHelper.put(newSpecification, chainData);
            // Add to grid only if it's truly new
            aGridListDataView.addItem(new CommandDescriptionAdapter(newSpecification));
        } else if (!isEditingExistingCommand) {
            // Command specification already exists and we're adding a new variant (not editing existing)
            // Commands with the same description are chained together
            // The chain will execute commands until one with met preconditions succeeds
            commandChainData.getCommands().add(command);
            // Refresh grid to show updated data
            aGridListDataView.refreshAll();
        }
        // If isEditingExistingCommand is true, the command is already in the chain and has been updated in place

        return command;
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        final Optional<String> optionalCommandId = event.getRouteParameters().get(RouteIds.COMMAND_ID.getValue());
        if (optionalCommandId.isPresent()) {
            commandId = optionalCommandId.get();
            pageTitle = "Edit Command #" + commandId;
        } else {
            pageTitle = "New Command";
        }

    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        AdventuresMainLayout.checkIfUserWantsToLeavePage(event, binder.hasChanges() || actionEditorHasChanges);
    }

    public void setData(AdventureData anAdventureData, LocationData aLocationData,
                        GridListDataView<CommandDescriptionAdapter> aGridListDataView) {
        adventureData = anAdventureData;
        locationData = aLocationData;
        gridListDataView = aGridListDataView;

        commandProviderData = locationData.getCommandProviderData();

        // Find existing command or create new one
        CommandDescriptionData commandDescriptionData;
        if (commandId != null && !commandId.isEmpty()) {
            // Look up existing command by specification (commandId contains the spec like "go|north|")
            CommandChainData commandChain = commandProviderData.getAvailableCommands().get(commandId);
            if (commandChain != null && !commandChain.getCommands().isEmpty()) {
                // Get the command description from the first command in the chain
                commandDescriptionData = commandChain.getCommands().get(0).getCommandDescription();
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

        // Reset action editor change tracking
        actionEditorHasChanges = false;

        // Set up action editor
        setupActionEditor();
    }

    private void setupActionEditor() {
        actionEditorContainer.removeAll();

        // Get the command chain and populate the grid
        currentCommandChain = null;
        ActionData action = null;

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

                action = commandData.getAction();
            } else {
                // No commands in the chain yet
                commandChainGrid.setDataProvider(new ListDataProvider<>(java.util.Collections.emptyList()));
            }
        } else {
            // Creating a new command - no chain yet
            commandChainGrid.setDataProvider(new ListDataProvider<>(java.util.Collections.emptyList()));
        }

        // Store the original action data for reset functionality
        originalActionData = action;

        // Show the action editor for the selected command
        showActionEditorForCommand(action);
    }

    /**
     * Show the action editor for a specific action.
     * Used when selecting a command from the grid.
     */
    private void showActionEditorForCommand(ActionData action) {
        actionEditorContainer.removeAll();

        // If there's an action, show its editor
        if (action != null) {
            createActionEditor(action);
        } else {
            // No action yet, show the action selector
            showActionSelector();
        }
    }

    /**
     * Reset the action editor to its original state.
     * This is called when the reset button is clicked.
     */
    private void resetActionEditor() {
        actionEditorContainer.removeAll();

        // If there was an original action, recreate its editor
        if (originalActionData != null) {
            createActionEditor(originalActionData);
        } else {
            // No original action, show the action selector
            actionEditor = null;
            showActionSelector();
        }
    }

    private void createActionEditor(final ActionData anOriginalActionData) {
        try {
            actionEditor = ActionEditorFactory.createEditor(anOriginalActionData, adventureData);

            // Add a "Change Action" button above the editor
            Button changeActionButton = new Button("Change Action");
            changeActionButton.addClickListener(e -> showActionSelector());

            actionEditorContainer.add(changeActionButton, actionEditor);

            // Attach listeners to update save button state when action editor fields change
            attachActionEditorListeners(actionEditor);
        } catch (UnsupportedOperationException e) {
            // Action type not supported yet, show a message and the selector
            Div message = new Div();
            message.setText("Action editor not available for: " + anOriginalActionData.getActionName());
            message.getStyle().set("color", "var(--lumo-error-text-color)");
            actionEditorContainer.add(message);
            showActionSelector();
        }
    }

    private void showActionSelector() {
        actionEditorContainer.removeAll();

        ActionSelector actionSelector = new ActionSelector(adventureData);
        actionSelector.setEditorSelectedListener(editor -> {
            // Replace the selector with the selected editor
            actionEditorContainer.removeAll();
            actionEditor = editor;

            // Add a "Change Action" button above the new editor
            Button changeActionButton = new Button("Change Action");
            changeActionButton.addClickListener(e -> showActionSelector());

            actionEditorContainer.add(changeActionButton, actionEditor);

            // Attach listeners to update save button state when action editor fields change
            attachActionEditorListeners(actionEditor);

            // Mark that the action has changed since a new action type was selected
            actionEditorHasChanges = true;

            // Update save and reset button states
            updateSaveButtonState();
            resetButton.setEnabled(true);
        });
        actionEditorContainer.add(actionSelector);
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
            // Clear the action editor
            actionEditorContainer.removeAll();
            showActionSelector();
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

            // Show the action editor for the newly selected command
            showActionEditorForCommand(commandData.getAction());
        }

        // Mark as having changes so save button enables
        actionEditorHasChanges = true;
        updateSaveButtonState();
        resetButton.setEnabled(true);
    }
}
