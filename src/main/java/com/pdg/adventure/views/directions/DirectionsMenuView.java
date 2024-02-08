package com.pdg.adventure.views.directions;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.views.adventure.AdventuresMainLayout;
import com.pdg.adventure.views.locations.LocationEditorView;
import com.pdg.adventure.views.support.ViewSupporter;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;

import java.util.Optional;
import java.util.Set;

@Route(value = "adventures/:adventureId/locations/:locationId/directions", layout = AdventuresMainLayout.class)
public class DirectionsMenuView extends VerticalLayout
        implements HasDynamicTitle, BeforeEnterObserver {
    private static final String LOCATION_ID_TEXT = "locationId";
    private static final String ADVENTURE_ID = "adventureId";
    private transient AdventureData adventureData;
    private transient LocationData locationData;

    private String pageTitle;
    private Grid<DirectionData> grid;
    private Button create;
    private TextField searchField;
    private Button backButton;

    public DirectionsMenuView() {
        setSizeFull();

        backButton = new Button("Back", event -> UI.getCurrent().navigate(LocationEditorView.class,
                new RouteParameters(
                        new RouteParam(ADVENTURE_ID, adventureData.getId()),
                        new RouteParam(LOCATION_ID_TEXT, locationData.getId()))
        ).ifPresent(e -> e.setAdventureData(adventureData))
        );
        backButton.addClickShortcut(Key.ESCAPE);

        create = new Button("Create Exit", e -> UI.getCurrent().navigate(DirectionEditorView.class,
                new RouteParameters(
                        new RouteParam(ADVENTURE_ID, adventureData.getId()),
                        new RouteParam(LOCATION_ID_TEXT, locationData.getId()))
        ).ifPresent(editor -> editor.setData(locationData, adventureData)));

        VerticalLayout leftSide = new VerticalLayout(create, backButton);

        searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Find direction");
        searchField.setTooltipText("Find direction by ID, noun or any text in its short description");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        Div gridContainer = new Div();
        gridContainer.setSizeFull();

        grid = getGrid();
        grid.setWidth("500px");
        grid.setHeight("500px");

        gridContainer.add(grid);

        VerticalLayout rightSide = new VerticalLayout(searchField, gridContainer);

        HorizontalLayout jumpRow = new HorizontalLayout(leftSide, rightSide);

        setMargin(true);
        setPadding(true);

        add(jumpRow);
    }

    private Grid<DirectionData> getGrid() {
        Grid<DirectionData> directionGrid = new Grid<>(DirectionData.class, false);
//        directionGrid.setSizeFull();
        directionGrid.addColumn(ViewSupporter::formatId).setHeader("Id").setAutoWidth(true).setFlexGrow(0);
        directionGrid.addColumn(directionData ->
                ViewSupporter.formatDescription(directionData.getCommandData().getCommandDescription())
        ).setHeader("Command");
        directionGrid.addColumn(directionData ->
                ViewSupporter.formatDescription(directionData.getDestinationData().getDescriptionData())
        ).setHeader("Location");
        directionGrid.addColumn(directionData -> ViewSupporter.formatId(directionData.getDestinationData())).setHeader("LocationId)");

        directionGrid.addItemDoubleClickListener(e ->
                UI.getCurrent().navigate(DirectionEditorView.class,
                        new RouteParameters(
                                new RouteParam(ADVENTURE_ID, adventureData.getId()),
                                new RouteParam(LOCATION_ID_TEXT, locationData.getId()),
                                new RouteParam("directionId", e.getItem().getId()))
                ).ifPresent(editor -> editor.setData(locationData, adventureData)));

        return directionGrid;
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> locId = event.getRouteParameters().get(LOCATION_ID_TEXT);
        if (locId.isPresent()) {
            final String locationId = locId.get();
            pageTitle = "Directions for location #" + locationId;
        } else {
            pageTitle = "Directions";
        }
    }

    private void fillGrid(Set<DirectionData> directionData) {
        grid.setItems(directionData);
    }

    public void setData(AdventureData anAdventureData, LocationData aLocationData) {
        adventureData = anAdventureData;
        locationData = aLocationData;

        final Set<DirectionData> directionsData = locationData.getDirectionsData();

        fillGrid(directionsData);
    }
}
