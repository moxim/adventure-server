package com.pdg.adventure.views.commands;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static com.pdg.adventure.views.support.RouteIds.ADVENTURE_ID;
import static com.pdg.adventure.views.support.RouteIds.LOCATION_ID;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.basics.CommandDescriptionData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.views.adventure.AdventuresMainLayout;
import com.pdg.adventure.views.locations.LocationEditorView;
import com.pdg.adventure.views.support.RouteIds;
import com.pdg.adventure.views.support.ViewSupporter;

@Route(value = "adventures/:adventureId/locations/:locationId/commands", layout = AdventuresMainLayout.class)
public class CommandsMenuView extends VerticalLayout
        implements HasDynamicTitle, BeforeEnterObserver {
    private transient final AdventureService adventureService;
    private final Binder<CommandProviderData> binder;
    private final Div gridContainer;
    private final Button saveButton;
    private final Button resetButton;
    private final Button backButton;
    private final Button createButton;
    private Grid<DescribableCommandAdapter> grid;
//    private GridUnbufferedInlineEditor grid;
    private String pageTitle;
    private LocationData locationData;
    private AdventureData adventureData;
    private TextField searchField;
    private CommandProviderData commandProviderData;
    private Set<CommandDescriptionData> availableCommands;
    private GridListDataView<DescribableCommandAdapter> gridListDataView;

    @Autowired
    public CommandsMenuView(AdventureService anAdventureService) {
        adventureService = anAdventureService;
        binder = new BeanValidationBinder<>(CommandProviderData.class);

        gridContainer = new Div("Commands");
        gridContainer.setSizeFull();

        createButton = new Button("Create", e -> {
            UI.getCurrent().navigate(CommandEditorView.class, new RouteParameters(
                                    new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                                    new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())))
              .ifPresent(editor -> editor.setData(adventureData, locationData, gridListDataView));

//            showCreateDialog(availableCommands);
        });

        saveButton = new Button("Save");
        saveButton.addClickListener(e -> {
                                    adventureService.saveLocationData(locationData);
                                    saveButton.setEnabled(false);
                                });

        backButton = new Button("Back", event -> UI.getCurrent().navigate(LocationEditorView.class,
                                                                          new RouteParameters(
                                                                                  new RouteParam(LOCATION_ID.getValue(),
                                                                                                 locationData.getId()),
                                                                                  new RouteParam(
                                                                                          ADVENTURE_ID.getValue(),
                                                                                          adventureData.getId()))
        ).ifPresent(e -> e.setData(adventureData)));
        backButton.addClickShortcut(Key.ESCAPE);

        resetButton = new Button("Reset", e -> {
            binder.readBean(commandProviderData);
        });

        VerticalLayout vll = new VerticalLayout(createButton, backButton, resetButton, saveButton);
        VerticalLayout vlr = new VerticalLayout(gridContainer);

        HorizontalLayout hl = new HorizontalLayout(vll, vlr);
        add(hl);
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

    private Grid<CommandDescriptionData> getGrid() {
        Grid<CommandDescriptionData> grid = new Grid<>(CommandDescriptionData.class, false);
        grid.setWidth(300, Unit.PIXELS);
        grid.addColumn(ViewSupporter::formatId).setHeader("Id").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(CommandDescriptionData::getVerb).setHeader("Verb").setAutoWidth(true);
        grid.addColumn(CommandDescriptionData::getAdjective).setHeader("Adjective").setAutoWidth(true);
        grid.addColumn(CommandDescriptionData::getNoun).setHeader("Noun").setAutoWidth(true);
        return grid;
    }

    private Grid<DescribableCommandAdapter> getSimpleGrid() {
        Grid<DescribableCommandAdapter> grid = new Grid<>(DescribableCommandAdapter.class, false);
        grid.setWidth(300, Unit.PIXELS);
        grid.addColumn(DescribableCommandAdapter::getVerb).setHeader("Verb").setAutoWidth(true).setFlexGrow(0).setSortable(true);
        grid.addColumn(DescribableCommandAdapter::getAdjective).setHeader("Adjective").setAutoWidth(true);
        grid.addColumn(DescribableCommandAdapter::getNoun).setHeader("Noun").setAutoWidth(true);
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

    private GridListDataView<DescribableCommandAdapter> fillGrid(CommandProviderData commandProviderData) {
        final Map<String, CommandChainData> availableCommands = commandProviderData.getAvailableCommands();
        final Set<DescribableCommandAdapter> commandDescriptionDataSet = new HashSet<>();

        for (Map.Entry<String, CommandChainData> entry : availableCommands.entrySet()) {
            String command = entry.getKey();
            DescribableCommandAdapter commandDescription = new DescribableCommandAdapter(command);
            commandDescriptionDataSet.add(commandDescription);
//            CommandChainData commandChainData = entry.getValue();
//            for (CommandDescriptionData commandDescriptionData : commandChainData.getCommands()) {
//                DescribableCommandAdapter commandDescription = new DescribableCommandAdapter(
//                  commandDescriptionData.getCommandSpecification()
//                );
//                commandDescriptionDataSet.add(commandDescription);
//            }
        }
//        CommandDescriptionDataFilter gridFilter = new CommandDescriptionDataFilter(gridListDataView);
        return grid.setItems(commandDescriptionDataSet);
    }

    public void setData(AdventureData anAdventureData, LocationData aLocationData) {
        adventureData = anAdventureData;
        locationData = aLocationData;

        commandProviderData = locationData.getCommandProviderData();
        binder.setBean(commandProviderData);

        HashSet<String> availableCommandsHelper = new HashSet<>(commandProviderData.getAvailableCommands().keySet());
        availableCommands = new HashSet<>(commandProviderData.getAvailableCommands().size());
        for (String command : availableCommandsHelper) {
            CommandDescriptionData commandDescriptionData = new CommandDescriptionData(command);
            availableCommands.add(commandDescriptionData);
        }
        grid = getSimpleGrid();
//        grid = new GridUnbufferedInlineEditor(availableCommands, vocabularyData, saveButton);
        grid.setEmptyStateText("Create some commands.");

        // Add double-click listener to edit commands
        grid.addItemDoubleClickListener(e -> {
            String commandSpec = e.getItem().getShortDescription(); // This returns the command specification
            navigateToCommandEditor(commandSpec);
        });

        gridListDataView = fillGrid(locationData.getCommandProviderData());
//        grid.setWidth(50, Unit.PERCENTAGE);
//        grid.setSizeFull();
        gridContainer.add(grid);
        saveButton.setEnabled(false);
        resetButton.setEnabled(false);
    }

    private void navigateToCommandEditor(String aCommandId) {
        UI.getCurrent().navigate(CommandEditorView.class, new RouteParameters(
                new RouteParam(RouteIds.COMMAND_ID.getValue(), aCommandId),
                new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())))
          .ifPresent(editor -> editor.setData(adventureData, locationData, gridListDataView));
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

