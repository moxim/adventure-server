package com.pdg.adventure.views.commands;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.*;

import java.util.Map;
import java.util.Optional;

import static com.pdg.adventure.model.Word.Type.*;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.basics.CommandDescriptionData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.views.adventure.AdventuresMainLayout;
import com.pdg.adventure.views.components.ResetBackSaveView;
import com.pdg.adventure.views.components.VocabularyPicker;
import com.pdg.adventure.views.components.VocabularyPickerField;
import com.pdg.adventure.views.locations.LocationsMainLayout;
import com.pdg.adventure.views.support.RouteIds;

@Route(value = "adventures/:adventureId/locations/:locationId/commands/:commandId/edit", layout = LocationsMainLayout.class)
@RouteAlias(value = "adventures/:adventureId/locations/:locationId/commands/new", layout = LocationsMainLayout.class)
public class CommandEditorView extends VerticalLayout
        implements HasDynamicTitle, BeforeLeaveObserver, BeforeEnterObserver {
    private transient final AdventureService adventureService;
    private final Binder<CommandViewModel> binder;
    private final VocabularyPicker nounSelector;
    private final VocabularyPicker adjectiveSelector;
    private final VocabularyPicker verbSelector;
    private transient String commandId;
    private String pageTitle;
    private Button saveButton;
    private Button resetButton;
    private Button cancelButton;
    private LocationData locationData;
    private GridListDataView<DescribableCommandAdapter> gridListDataView;
    private AdventureData adventureData;
    private CommandProviderData commandProviderData;
    private CommandDescriptionData commandDescriptionData;
    private transient CommandViewModel cvm;

    public CommandEditorView(AdventureService anAdventureService) {
        adventureService = anAdventureService;
        binder = new Binder<>(CommandViewModel.class);

        verbSelector = new VocabularyPickerField("Verb", "You may filter on verbs.", VERB, new VocabularyData());
        verbSelector.setHelperText("Select at least a verb.");
        adjectiveSelector = new VocabularyPickerField("Adjective", "You may filter on adjectives.", ADJECTIVE, new VocabularyData());
        nounSelector = new VocabularyPickerField("Noun", "You may filter on nouns.", NOUN, new VocabularyData());

        binder.forField(verbSelector)
              .asRequired("Verb is required")
              .withValidator(word -> word != null && !word.getText().isEmpty(), "Please select a verb with text")
              .bind(CommandViewModel::getVerb, CommandViewModel::setVerb);
        binder.forField(adjectiveSelector)
              .bind(CommandViewModel::getAdjective, CommandViewModel::setAdjective);
        binder.forField(nounSelector)
              .bind(CommandViewModel::getNoun, CommandViewModel::setNoun);

        binder.addStatusChangeListener(event -> {
            boolean isValid = event.getBinder().isValid();
            boolean hasChanges = event.getBinder().hasChanges();

            saveButton.setEnabled(hasChanges && isValid);
            resetButton.setEnabled(hasChanges);
        });

        HorizontalLayout commandLayout = new HorizontalLayout(verbSelector, adjectiveSelector, nounSelector);

        final ResetBackSaveView resetBackSaveView = setUpNavidationButtons();

        VerticalLayout vl1 = new VerticalLayout();
        vl1.add(new Span("Actions et all"));
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
        cancelButton = resetBackSaveView.getCancel();
        cancelButton.setEnabled(false);

        backButton.addClickListener(event -> UI.getCurrent().navigate(CommandsMenuView.class, new RouteParameters(
                                                       new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                                                       new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())))
                                               .ifPresent(e -> e.setData(adventureData, locationData)));
        saveButton.addClickListener(event -> validateSave(commandProviderData));
        resetButton.addClickListener(event -> binder.readBean(cvm));
        resetBackSaveView.getCancel().addClickShortcut(Key.ESCAPE);

        return resetBackSaveView;
    }

    private void validateSave(CommandProviderData aCommandProviderData) {
        try {
            if (binder.validate().isOk()) {
                binder.writeBean(cvm);
                swivelTheSaveButton(gridListDataView);
                adventureService.saveLocationData(locationData);
                saveButton.setEnabled(false);
                cancelButton.setEnabled(false);
            }
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    private void swivelTheSaveButton(GridListDataView<DescribableCommandAdapter> aGridListDataView) {
        // Use the commandDescriptionData that was updated via the binder
        final CommandDescriptionData updatedCommandDescription = cvm.getData();
        final String newSpecification = updatedCommandDescription.getCommandSpecification();

        final Map<String, CommandChainData> availableCommandsHelper = commandProviderData.getAvailableCommands();

        // If editing an existing command and the specification has changed, remove the old entry
        if (commandId != null && !commandId.isEmpty() && !commandId.equals(newSpecification)) {
            availableCommandsHelper.remove(commandId);
            // Remove old item from grid
            aGridListDataView.getItems()
                    .filter(item -> item.getShortDescription().equals(commandId))
                    .findFirst()
                    .ifPresent(aGridListDataView::removeItem);
        }

        // Now add or update the command with the new specification
        CommandData command = new CommandData();
        command.setCommandDescription(updatedCommandDescription);

        final CommandChainData commandChainData = availableCommandsHelper.get(newSpecification);
        if (commandChainData == null) {
            // New command - create new chain
            final CommandChainData chainData = new CommandChainData();
            chainData.getCommands().add(command);
            availableCommandsHelper.put(newSpecification, chainData);
            // Add to grid only if it's truly new
            aGridListDataView.addItem(new DescribableCommandAdapter(newSpecification));
        } else {
            // Command already exists - update it
            // Clear existing commands and replace with updated one
            commandChainData.getCommands().clear();
            commandChainData.getCommands().add(command);
            // Refresh grid to show updated data
            aGridListDataView.refreshAll();
        }
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
        AdventuresMainLayout.checkIfUserWantsToLeavePage(event, binder.hasChanges());
    }

    public void setData(AdventureData anAdventureData, LocationData aLocationData,
                        GridListDataView<DescribableCommandAdapter> aGridListDataView) {
        adventureData = anAdventureData;
        locationData = aLocationData;
        gridListDataView = aGridListDataView;

        commandProviderData = locationData.getCommandProviderData();

        // Find existing command or create new one
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
    }
}
