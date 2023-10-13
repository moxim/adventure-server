package com.pdg.adventure.views.commands;

import com.pdg.adventure.model.CommandChainData;
import com.pdg.adventure.model.CommandProviderData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.basics.CommandDescriptionData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.views.adventure.AdventuresMainLayout;
import com.pdg.adventure.views.support.ViewSupporter;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;

@Route(value = "adventures/:adventureId/locations/:locationId/commands", layout = AdventuresMainLayout.class)
public class CommandsMenuView  extends VerticalLayout
        implements HasDynamicTitle, BeforeEnterObserver
{
    private transient final AdventureService adventureService;
    private final Binder<LocationData> binder;

    private Grid<CommandDescriptionData> grid;

    private String pageTitle;
    private LocationData locationData;

    private Button create;
    private Button edit;
    private TextField searchField;
    private Button backButton;
    private IntegerField numberOfLocations;

    @Autowired
    public CommandsMenuView(AdventureService anAdventureService) {
        adventureService = anAdventureService;
        binder = new Binder<>(LocationData.class);

        Div gridContainer = new Div();
        grid = getGrid();
        gridContainer.add(grid);
        gridContainer.setHeight("100%");
        gridContainer.setWidth("100%");

        add(gridContainer);
    }

    private Grid<CommandDescriptionData> getGrid() {
        Grid<CommandDescriptionData> grid = new Grid<>(CommandDescriptionData.class, false);
        grid.addColumn(ViewSupporter::formatId).setHeader("Id").setAutoWidth(true).setFlexGrow(0);
//        grid.addColumn(CommandChain::getTitle).setHeader("Title").setSortable(true)
//            .setAutoWidth(true);
        return grid;
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> locId = event.getRouteParameters().get("locationId");

        locationData = adventureService.findLocationById(locId.orElseThrow());

        binder.setBean(locationData);
        pageTitle = "Commands for location #" + locationData.getId();

        fillGrid(locationData.getCommandProviderData());

//        saveButton.setEnabled(false);
    }

    private void fillGrid(CommandProviderData commandProviderData) {
        final Map<CommandDescriptionData, CommandChainData> availableCommands = commandProviderData.getAvailableCommands();
        grid.setItems(availableCommands.keySet());
    }
}
