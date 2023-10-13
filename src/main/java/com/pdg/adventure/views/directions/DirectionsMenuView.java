package com.pdg.adventure.views.directions;

import com.pdg.adventure.model.*;
import com.pdg.adventure.model.basics.CommandDescriptionData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.views.adventure.AdventuresMainLayout;
import com.pdg.adventure.views.locations.LocationEditorView;
import com.pdg.adventure.views.support.ViewSupporter;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;

@Route(value = "adventures/:adventureId/locations/:locationId/directions", layout = AdventuresMainLayout.class)
public class DirectionsMenuView extends VerticalLayout
        implements HasDynamicTitle, BeforeEnterObserver
{
    private transient final AdventureService adventureService;
    private final Binder<DirectionData> binder;

    private Grid<CommandDescriptionData> grid;

    private String pageTitle;
    private AdventureData adventureData;
    private LocationData locationData;
    private DirectionData directionData;

    private Button create;
    private Button edit;
    private TextField searchField;
    private Button backButton;

    @Autowired
    public DirectionsMenuView(AdventureService anAdventureService) {
        adventureService = anAdventureService;
        binder = new Binder<>(DirectionData.class);

        backButton = new Button("Back", event -> {
            UI.getCurrent().navigate(LocationEditorView.class,
                                     new RouteParameters(
                            new RouteParam("adventureId", adventureData.getId()),
                            new RouteParam("locationId", locationData.getId())
                            )
            );
        });

        create = new Button("Create Exit", e -> {
            UI.getCurrent().navigate(DirectionEditorView.class
            ).ifPresent(editor -> editor.setData(locationData, adventureData));
        });

        VerticalLayout leftSide = new VerticalLayout(create, backButton);

        searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Find direction");
        searchField.setTooltipText("Find direction by ID, noun or any text in its short description");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        Div gridContainer = new Div();
        grid = getGrid();
        gridContainer.add(grid);
        gridContainer.setHeight("100%");
        gridContainer.setWidth("100%");

        VerticalLayout rightSide = new VerticalLayout(searchField, gridContainer);

        HorizontalLayout jumpRow = new HorizontalLayout(leftSide, rightSide);

        setMargin(true);
        setPadding(true);

        add(jumpRow);
    }

    private Grid<CommandDescriptionData> getGrid() {
        Grid<CommandDescriptionData> grid = new Grid<>(CommandDescriptionData.class, false);
        grid.addColumn(ViewSupporter::formatId).setHeader("Id").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(CommandDescriptionData::getVerb).setHeader("Verb").setAutoWidth(true);
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
        pageTitle = "Directions for location #" + locId;


//        saveButton.setEnabled(false);
    }

    private void fillGrid(CommandProviderData commandProviderData) {
        final Map<CommandDescriptionData, CommandChainData> availableCommands = commandProviderData.getAvailableCommands();
        grid.setItems(availableCommands.keySet());
    }

    public void setData(AdventureData anAdventureData, LocationData aLocationData) {
        adventureData = anAdventureData;
        locationData = aLocationData;

//        final ItemContainerData directionsData = locationData.getDirectionsData();

        binder.setBean(directionData);

        fillGrid(locationData.getCommandProviderData());
    }
}
