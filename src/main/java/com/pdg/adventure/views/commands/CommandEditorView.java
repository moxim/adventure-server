package com.pdg.adventure.views.commands;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.*;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.pdg.adventure.model.Word.Type.ADJECTIVE;
import static com.pdg.adventure.model.Word.Type.NOUN;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.basics.CommandDescriptionData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.views.components.ResetBackSaveView;
import com.pdg.adventure.views.components.VocabularyPicker;
import com.pdg.adventure.views.locations.LocationsMainLayout;
import com.pdg.adventure.views.support.RouteSupporter;

@Route(value = "adventures/:adventureId/locations/:locationId/commands/:commandId/edit", layout = LocationsMainLayout.class)
@RouteAlias(value = "adventures/:adventureId/locations/:locationId/commands/new", layout = LocationsMainLayout.class)
public class CommandEditorView extends VerticalLayout
        implements HasDynamicTitle, BeforeLeaveObserver, BeforeEnterObserver {
    private transient final AdventureService adventureService;
    private final Binder<CommandProviderData> binder;
    private final VocabularyPicker nounSelection;
    private final VocabularyPicker adjectiveSelection;
    private final VocabularyPicker verbSelection;
    private transient String commandId;
    private String pageTitle;
    private Button saveButton;
    private Button resetButton;
    private Button backButton;
    private LocationData locationData;
    private GridListDataView<SimpleCommandDescription> gridListDataView;
    private AdventureData adventureData;
    private CommandProviderData commandProviderData;
    private Set<CommandDescriptionData> availableCommands;

    public CommandEditorView(AdventureService anAdventureService) {
        adventureService = anAdventureService;
        binder = new BeanValidationBinder<>(CommandProviderData.class);

        verbSelection = getWordBox("Verb", "You may filter on verbs.");
        verbSelection.setHelperText("Select at least a verb.");
        adjectiveSelection = getWordBox("Adjective", "You may filter on adjectives.");
        nounSelection = getWordBox("Noun", "You may filter on nouns.");

        HorizontalLayout commandLayout = new HorizontalLayout(verbSelection, adjectiveSelection, nounSelection);

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

        backButton.addClickListener(event -> UI.getCurrent().navigate(CommandsMenuView.class, new RouteParameters(
                                                       new RouteParam(RouteSupporter.LOCATION_ID.getValue(), locationData.getId()),
                                                       new RouteParam(RouteSupporter.ADVENTURE_ID.getValue(), adventureData.getId())))
                                               .ifPresent(e -> e.setData(adventureData, locationData)));
        saveButton.addClickListener(event -> validateSave(commandProviderData));
        resetButton.addClickListener(event -> {
            verbSelection.clear();
            adjectiveSelection.clear();
            nounSelection.clear();
            binder.readBean(commandProviderData);
            resetButton.setEnabled(false);
        });
        resetBackSaveView.getCancel().addClickShortcut(Key.ESCAPE);

        return resetBackSaveView;
    }

    private void validateSave(CommandProviderData aCommandProviderData) {
        try {
            binder.writeBean(aCommandProviderData);
            if (binder.validate().isOk()) {
                swivelTheSaveButton(gridListDataView);
                adventureService.saveLocationData(locationData);
                saveButton.setEnabled(false);
            }
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    private void swivelTheSaveButton(GridListDataView<SimpleCommandDescription> aGridListDataView) {
        CommandDescriptionData commandDescriptionData = new CommandDescriptionData();
        commandDescriptionData.setVerb(verbSelection.getValue());
        commandDescriptionData.setAdjective(adjectiveSelection.getValue());
        commandDescriptionData.setNoun(nounSelection.getValue());

        CommandData command = new CommandData();
        command.setCommandDescription(commandDescriptionData);

        final Map<String, CommandChainData> availableCommandsHelper = commandProviderData.getAvailableCommands();
        final CommandChainData commandChainData = availableCommandsHelper.get(commandDescriptionData.getCommandSpecification());
        if (commandChainData == null) {
            final CommandChainData chainData = new CommandChainData();
            chainData.getCommands().add(command);
            availableCommandsHelper.put(commandDescriptionData.getCommandSpecification(), chainData);
        } else {
            commandChainData.getCommands().add(command);
        }
        aGridListDataView.addItem(new SimpleCommandDescription(commandDescriptionData.getCommandSpecification()));
        adventureService.saveLocationData(locationData);
    }

    /*
    private void showCreateDialog(Set<CommandDescriptionData> aListOfAvailableCommands) {

        verbSelection.clear();
        final Registration valueChangeListener = verbSelection.addValueChangeListener(
                comboBoxWordComponentValueChangeEvent -> {
                    createSaveButton.setEnabled(!verbSelection.isEmpty());
                });

        adjectiveSelection.clear();
        nounSelection.clear();

        HorizontalLayout commandLayout = new HorizontalLayout(verbSelection, adjectiveSelection, nounSelection);

        Button cancelButton = new Button("Cancel");

        HorizontalLayout buttonsLayout = new HorizontalLayout(createSaveButton, cancelButton);
        VerticalLayout vl = new VerticalLayout(commandLayout, buttonsLayout);


        VerticalLayout vl1 = new VerticalLayout();
        vl1.add(new Span("Actions et all"));
        Details details = new Details("Preconditions & Actions", vl1);

        Dialog dialog = new Dialog("New command", vl, details);

        createSaveButton.setEnabled(false);
        createSaveButton.addClickListener(e -> {
            CommandDescriptionData commandDescriptionData = new CommandDescriptionData();
            commandDescriptionData.setVerb(verbSelection.getValue());
            commandDescriptionData.setAdjective(adjectiveSelection.getValue());
            commandDescriptionData.setNoun(nounSelection.getValue());

            CommandData command = new CommandData();
            command.setCommandDescription(commandDescriptionData);

            final Map<String, CommandChainData> availableCommandsHelper = commandProviderData.getAvailableCommands();
            final CommandChainData commandChainData = availableCommandsHelper.get(commandDescriptionData.getCommandSpecification());
            if (commandChainData == null) {
                final CommandChainData chainData = new CommandChainData();
                chainData.getCommands().add(command);
                availableCommandsHelper.put(commandDescriptionData.getCommandSpecification(), chainData);
            } else {
                commandChainData.getCommands().add(command);
            }
            grid.addItem(commandDescriptionData);
            aListOfAvailableCommands.add(commandDescriptionData);
            adventureService.saveLocationData(locationData);
            closeDialog(dialog, valueChangeListener);
        });

        cancelButton.addClickListener(e -> closeDialog(dialog, valueChangeListener));

        dialog.open();
    }
*/
    private void checkIfSaveAvailable() {
        final boolean isVerbEmpty = verbSelection.isEmpty();
        saveButton.setEnabled(!isVerbEmpty);
        resetButton.setEnabled(true);
    }

    private VocabularyPicker getWordBox(String label, String tooltipText) {
        VocabularyPicker wordBox = new VocabularyPicker(label);
        wordBox.setHelperText("");
        wordBox.setTooltipText(tooltipText);
        wordBox.addValueChangeListener(e -> checkIfSaveAvailable());
        return wordBox;
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        final Optional<String> optionalCommandId = event.getRouteParameters().get(RouteSupporter.COMMAND_ID.getValue());
        if (optionalCommandId.isPresent()) {
            commandId = optionalCommandId.get();
            pageTitle = "Edit Command #" + commandId;
        } else {
            pageTitle = "New Command";
        }

    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
//        AdventuresMainLayout.checkIfUserWantsToLeavePage(event, binder.hasChanges());
    }

    public void setData(AdventureData anAdventureData, LocationData aLocationData,
                        GridListDataView<SimpleCommandDescription> aGridListDataView) {
        adventureData = anAdventureData;
        locationData = aLocationData;
        this.gridListDataView = aGridListDataView;

        commandProviderData = locationData.getCommandProviderData();
        VocabularyData vocabularyData = adventureData.getVocabularyData();
        nounSelection.populate(
                vocabularyData.getWords(NOUN).stream().filter(word -> word.getSynonym() == null).toList());
        adjectiveSelection.populate(
                vocabularyData.getWords(ADJECTIVE).stream().filter(word -> word.getSynonym() == null).toList());
        verbSelection.populate(
                vocabularyData.getWords(Word.Type.VERB).stream().filter(word -> word.getSynonym() == null).toList());

    }
}
