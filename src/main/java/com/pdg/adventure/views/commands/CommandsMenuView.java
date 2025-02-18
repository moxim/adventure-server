package com.pdg.adventure.views.commands;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.basics.CommandDescriptionData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.views.adventure.AdventuresMainLayout;
import com.pdg.adventure.views.components.VocabularyPicker;
import com.pdg.adventure.views.locations.LocationEditorView;
import com.pdg.adventure.views.support.ViewSupporter;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static com.pdg.adventure.model.Word.Type.*;
import static com.pdg.adventure.views.support.RouteSupporter.ADVENTURE_ID;
import static com.pdg.adventure.views.support.RouteSupporter.LOCATION_ID;

@Route(value = "adventures/:adventureId/locations/:locationId/commands", layout = AdventuresMainLayout.class)
public class CommandsMenuView  extends VerticalLayout
        implements HasDynamicTitle, BeforeEnterObserver
{
    private transient final AdventureService adventureService;
    private final Binder<LocationData> binder;
    private final Div gridContainer;

    //    private Grid<CommandDescriptionData> grid;
    private GridUnbufferedInlineEditor grid;

    private String pageTitle;
    private LocationData locationData;
    private AdventureData adventureData;

    private final Button createButton;
    private final Button saveButton;
    private final Button resetButton;

    private final VocabularyPicker nounSelection;
    private final VocabularyPicker adjectiveSelection;
    private final VocabularyPicker verbSelection;

    private TextField searchField;
    private IntegerField numberOfLocations;
    private final Button backButton;
    private CommandProviderData commandProviderData;
    private Set<CommandDescriptionData> availableCommands;

    @Autowired
    public CommandsMenuView(AdventureService anAdventureService) {
        adventureService = anAdventureService;
        binder = new Binder<>(LocationData.class);

        verbSelection = getWordBox("Verb", "You may filter on verbs.");
        adjectiveSelection = getWordBox( "Adjective", "You may filter on adjectives.");
        nounSelection = getWordBox("Noun", "You may filter on nouns.");

        gridContainer = new Div("");
//        grid = getGrid();
        gridContainer.setSizeFull();
//        gridContainer.add(grid);

        createButton = new Button("Create", e -> {
            showCreateDialog();
        });

        saveButton = new Button("Save", e -> {
        });

        backButton = new Button("Back", event -> UI.getCurrent().navigate(LocationEditorView.class,
                        new RouteParameters(
                                new RouteParam(LOCATION_ID.name(), locationData.getId()),
                                new RouteParam(ADVENTURE_ID.name(), adventureData.getId()))
                ).ifPresent(e -> e.setAdventureData(adventureData)));

        resetButton = new Button("Reset", e -> {
            binder.readBean(locationData);
        });

        VerticalLayout vll = new VerticalLayout(createButton, backButton, resetButton, saveButton);
        VerticalLayout vlr = new VerticalLayout(gridContainer);

        HorizontalLayout hl = new HorizontalLayout(vll, vlr);
        add(hl);
    }

    private void showCreateDialog() {
        HorizontalLayout commandLayout = new HorizontalLayout(verbSelection, adjectiveSelection, nounSelection);
        Button saveButton = new Button("Save");
        Button cancelButton = new Button("Cancel");
        HorizontalLayout buttonsLayout = new HorizontalLayout(saveButton, cancelButton);
        VerticalLayout vl = new VerticalLayout(commandLayout, buttonsLayout);
        Dialog dialog = new Dialog("New command", vl);
        cancelButton.addClickListener(e -> {
            dialog.close();
        });
        saveButton.addClickListener(e -> {
            CommandDescriptionData commandDescriptionData = new CommandDescriptionData();
            commandDescriptionData.setVerb(verbSelection.getValue());
            commandDescriptionData.setAdjective(adjectiveSelection.getValue());
            commandDescriptionData.setNoun(nounSelection.getValue());
            CommandData command = new CommandData();
            command.setCommandDescription(commandDescriptionData);
            final Map<CommandDescriptionData, CommandChainData> commandChainDataMap = commandProviderData.getAvailableCommands();
            final CommandChainData commandChainData = commandChainDataMap.get(commandDescriptionData);
            if (commandChainData == null) {
                final CommandChainData chainData = new CommandChainData();
                chainData.getCommands().add(command);
                commandChainDataMap.put(commandDescriptionData, chainData);
            } else {
                commandChainData.getCommands().add(command);
            }
            dialog.close();
        });
        dialog.open();
    }

    private VocabularyPicker getWordBox(String label, String tooltipText) {
        VocabularyPicker wordBox = new VocabularyPicker(label);
        wordBox.setHelperText("");
        wordBox.setTooltipText(tooltipText);
        wordBox.addValueChangeListener(e -> checkIfSaveAvailable());
        return wordBox;
    }

    private void checkIfSaveAvailable() {
        if (binder.validate().isOk()) {
            final boolean isNounEmpty = nounSelection.isEmpty();
            // TODO: see if we can use the binder instead
            //  binder.getBean().getNoun().getText().isEmpty();
            saveButton.setEnabled(!isNounEmpty);
        }
        resetButton.setEnabled(false);
    }

    private Grid<CommandDescriptionData> getGrid() {
        Grid<CommandDescriptionData> grid = new Grid<>(CommandDescriptionData.class, false);
        grid.setWidth(300, Unit.PIXELS);
        grid.addColumn(ViewSupporter::formatId).setHeader("Id").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(CommandDescriptionData::getVerb).setHeader("Verb").setAutoWidth(true);
        grid.addColumn(CommandDescriptionData::getAdjective).setHeader("Adjective").setAutoWidth(true);
        grid.addColumn(CommandDescriptionData::getNoun).setHeader("Noun").setAutoWidth(true);
        return grid;
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        String locationId = "666";
        final Optional<String> optionalLocationId = event.getRouteParameters().get("locationId");
        if (optionalLocationId.isPresent()) {
            locationId = optionalLocationId.get();
        }
        //  must be set in here
        pageTitle = "Commands for location #" + locationId;
    }

    public void setData(AdventureData anAdventureData, LocationData aLocationData) {
        adventureData = anAdventureData;
        VocabularyData vocabularyData = adventureData.getVocabularyData();
        nounSelection.populate(vocabularyData.getWords(NOUN));
        adjectiveSelection.populate(vocabularyData.getWords(ADJECTIVE));
        verbSelection.populate(vocabularyData.getWords(Word.Type.VERB));
        locationData = aLocationData;
        binder.setBean(locationData);

        commandProviderData = locationData.getCommandProviderData();
        availableCommands = new HashSet<>(commandProviderData.getAvailableCommands().keySet());

        var cdd  = new CommandDescriptionData();
        cdd.setVerb(new Word("leave", VERB));
        availableCommands.add(cdd);
        cdd  = new CommandDescriptionData();
        cdd.setVerb(new Word("climb", VERB));
        availableCommands.add(cdd);

        grid = new GridUnbufferedInlineEditor(availableCommands);

//        fillGrid(locationData.getCommandProviderData());
        grid.setWidth(300, Unit.PIXELS);
        gridContainer.add(grid);
        saveButton.setEnabled(false);
   }

    private void fillGrid(CommandProviderData commandProviderData) {
        final Map<CommandDescriptionData, CommandChainData> availableCommands = commandProviderData.getAvailableCommands();
        final Set<CommandDescriptionData> commandDescriptionDataSet = availableCommands.keySet();

//        GridListDataView<CommandDescriptionData> gridListDataView = grid.setItems(commandDescriptionDataSet);
//        CommandDescriptionDataFilter gridFilter = new CommandDescriptionDataFilter(gridListDataView);
    }

    private static Component createFilterHeader(String labelText, Consumer<String> filterChangeConsumer) {
        NativeLabel label = new NativeLabel(labelText);
        label.getStyle().set("padding-top", "var(--lumo-space-m)")
                .set("font-size", "var(--lumo-font-size-xs)");
        TextField textField = new TextField();
        textField.setValueChangeMode(ValueChangeMode.EAGER);
        textField.setClearButtonVisible(true);
        textField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        textField.setWidthFull();
        textField.getStyle().set("max-width", "100%");
        textField.addValueChangeListener(
                e -> filterChangeConsumer.accept(e.getValue()));
        VerticalLayout layout = new VerticalLayout(label, textField);
        layout.getThemeList().clear();
        layout.getThemeList().add("spacing-xs");

        return layout;
    }

    private static class CommandDescriptionDataFilter {
            private final GridListDataView<CommandDescriptionData> dataView;

            private String verb;
            private String adjective;
            private String noun;

            public CommandDescriptionDataFilter(GridListDataView<CommandDescriptionData> dataView) {
                this.dataView = dataView;
                this.dataView.addFilter(this::test);
            }

            public void setVerb(String verb) {
                this.verb = verb;
                this.dataView.refreshAll();
            }

            public void setAdjective(String adjective) {
                this.adjective = adjective;
                this.dataView.refreshAll();
            }

            public void setNoun(String noun) {
                this.noun = noun;
                this.dataView.refreshAll();
            }

            public boolean test(CommandDescriptionData CommandDescriptionData) {
                boolean matchesVerb = matches(CommandDescriptionData.getVerb().getText(), verb);
                boolean matchesAdjective = matches(CommandDescriptionData.getAdjective().getText(), adjective);
                boolean matchesNoun = matches(CommandDescriptionData.getNoun().getText(), noun);

                return matchesVerb && matchesAdjective && matchesNoun;
            }

            private boolean matches(String value, String searchTerm) {
                return searchTerm == null || searchTerm.isEmpty()
                        || value.toLowerCase().contains(searchTerm.toLowerCase());
            }
        }

}
