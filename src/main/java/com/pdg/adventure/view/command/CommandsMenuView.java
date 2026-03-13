package com.pdg.adventure.view.command;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.pdg.adventure.view.support.RouteIds.ADVENTURE_ID;
import static com.pdg.adventure.view.support.RouteIds.LOCATION_ID;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventuresMainLayout;
import com.pdg.adventure.view.location.LocationEditorView;
import com.pdg.adventure.view.support.GridProvider;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

@Route(value = "author/adventures/:adventureId/locations/:locationId/commands", layout = AdventuresMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class CommandsMenuView extends VerticalLayout
        implements HasDynamicTitle, BeforeEnterObserver {
    private transient final AdventureService adventureService;
    private final Binder<CommandProviderData> binder;
    private final Div gridContainer;
    private final Button saveButton;
    private final Button resetButton;
    private final Button backButton;
    private final Button createButton;
    private Grid<CommandDescriptionAdapter> grid;
    private String pageTitle;
    private LocationData locationData;
    private AdventureData adventureData;
    private CommandProviderData commandProviderData;
    private GridListDataView<CommandDescriptionAdapter> gridListDataView;

    public CommandsMenuView(AdventureService anAdventureService) {
        adventureService = anAdventureService;
        binder = new BeanValidationBinder<>(CommandProviderData.class);

        gridContainer = new Div("Commands");
        gridContainer.setSizeFull();

        createButton = new Button("Create", _ -> {
            UI.getCurrent().navigate(CommandEditorView.class, new RouteParameters(
                      new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                      new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())))
              .ifPresent(editor -> editor.setData(adventureData, locationData, gridListDataView));

//            showCreateDialog(availableCommands);
        });

        saveButton = new Button("Save");
        saveButton.addClickListener(_ -> {
            adventureService.saveLocationData(locationData);
            saveButton.setEnabled(false);
        });

        backButton = new Button("Back", _ -> UI.getCurrent().navigate(LocationEditorView.class,
                                                                          new RouteParameters(
                                                                                  new RouteParam(LOCATION_ID.getValue(),
                                                                                                 locationData.getId()),
                                                                                  new RouteParam(
                                                                                          ADVENTURE_ID.getValue(),
                                                                                          adventureData.getId()))
        ).ifPresent(e -> e.setData(adventureData)));
        backButton.addClickShortcut(Key.ESCAPE);

        resetButton = new Button("Reset", _ -> {
            binder.readBean(commandProviderData);
        });

        VerticalLayout vll = new VerticalLayout(createButton, backButton, resetButton, saveButton);
        VerticalLayout vlr = new VerticalLayout(gridContainer);
        vlr.setSizeFull();

        HorizontalLayout hl = new HorizontalLayout(vll, vlr);
        add(hl);
    }

    private Grid<CommandDescriptionAdapter> getSimpleGrid() {
        GridProvider<CommandDescriptionAdapter> gridProvider = new GridProvider<>(CommandDescriptionAdapter.class);
        gridProvider.getGrid().getColumns().get(1).setHeader("Command");
        gridProvider.addColumn(CommandDescriptionAdapter::getVerb, "Verb");
        gridProvider.addColumn(CommandDescriptionAdapter::getAdjective, "Adjective");
        gridProvider.addColumn(CommandDescriptionAdapter::getNoun, "Noun");
        ViewSupporter.setSize(gridProvider.getGrid());
        return gridProvider.getGrid();
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

    private GridListDataView<CommandDescriptionAdapter> fillGrid(CommandProviderData commandProviderData) {
        final Map<String, CommandChainData> availableCommands = commandProviderData.getAvailableCommands();
        final List<CommandDescriptionAdapter> commandDescriptionAdapters = new ArrayList<>(availableCommands.size());
        for (String command : availableCommands.keySet()) {
            commandDescriptionAdapters.add(new CommandDescriptionAdapter(command));
        }
        return grid.setItems(commandDescriptionAdapters);
    }

    public void setData(AdventureData anAdventureData, LocationData aLocationData) {
        adventureData = anAdventureData;
        locationData = aLocationData;

        commandProviderData = locationData.getCommandProviderData();
        binder.setBean(commandProviderData);

        grid = getSimpleGrid();
//        grid = new GridUnbufferedInlineEditor(availableCommands, vocabularyData, saveButton);
        grid.setEmptyStateText("Create some commands.");

        // Add double-click listener to edit commands
        grid.addItemDoubleClickListener(e -> {
            String commandSpec = e.getItem().getShortDescription(); // This returns the command specification
            navigateToCommandEditor(commandSpec);
        });

        gridListDataView = fillGrid(locationData.getCommandProviderData());

        // Add context menu
        new CommandContextMenu(grid);

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

    private class CommandContextMenu extends GridContextMenu<CommandDescriptionAdapter> {
        public CommandContextMenu(Grid<CommandDescriptionAdapter> target) {
            super(target);

            addItem("Edit", e -> e.getItem().ifPresent(command -> {
                String commandSpec = command.getShortDescription();
                navigateToCommandEditor(commandSpec);
            }));

            addComponent(new Hr());

            addItem("Delete", e -> e.getItem().ifPresent(command -> {
                String commandSpec = command.getShortDescription();
                // Remove from the data view
                gridListDataView.removeItem(command);
                // Remove from the command provider data
                commandProviderData.getAvailableCommands().remove(commandSpec);
                // Save changes
                adventureService.saveLocationData(locationData);
                // Refresh grid
                gridListDataView.refreshAll();
            }));
        }
    }

}

