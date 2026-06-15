package com.pdg.adventure.view.direction;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.DirectionData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.location.LocationEditorView;
import com.pdg.adventure.view.support.GridProvider;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

@Route(value = "author/adventures/:adventureId/locations/:locationId/directions", layout = DirectionsMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
public class DirectionsMenuView extends VerticalLayout implements HasDynamicTitle, BeforeEnterObserver {
    private final transient AdventureService adventureService;
    private final Grid<DirectionDescriptionAdapter> grid;
    private GridListDataView<DirectionDescriptionAdapter> directionsDataView;
    private transient AdventureData adventureData;
    private transient LocationData locationData;
    private String pageTitle;

    public DirectionsMenuView(AdventureService anAdventureService) {
        adventureService = anAdventureService;
        setSizeFull();

        Button backButton = new Button("Back", _ -> UI.getCurrent().navigate(LocationEditorView.class,
                                                                                 new RouteParameters(
                                                                                         new RouteParam(
                                                                                                 RouteIds.ADVENTURE_ID.getValue(),
                                                                                                 adventureData.getId()),
                                                                                         new RouteParam(
                                                                                                 RouteIds.LOCATION_ID.getValue(),
                                                                                                 locationData.getId())))
                                                          .ifPresent(e -> e.setData(adventureData)));
        backButton.addClickShortcut(Key.ESCAPE);

        Button create = new Button("Create Exit", _ -> UI.getCurrent().navigate(DirectionEditorView.class,
                                                                                new RouteParameters(
                                                                                        new RouteParam(
                                                                                                RouteIds.ADVENTURE_ID.getValue(),
                                                                                                adventureData.getId()),
                                                                                        new RouteParam(
                                                                                                RouteIds.LOCATION_ID.getValue(),
                                                                                                locationData.getId())))
                                                         .ifPresent(editor -> editor.setData(locationData,
                                                                                             adventureData)));

        VerticalLayout leftSide = new VerticalLayout(create, backButton);

        TextField searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Find direction");
        searchField.setTooltipText("Find direction by ID, noun or any text in its short description");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

//        Div gridContainer = new Div();
//        gridContainer.setSizeFull();
//
        grid = getGrid();

//        gridContainer.add(grid);

        VerticalLayout rightSide = new VerticalLayout(searchField, grid);
        rightSide.setSizeFull();

        HorizontalLayout jumpRow = new HorizontalLayout(leftSide, rightSide);

        setMargin(true);
        setPadding(true);

        add(jumpRow);
    }

    private Grid<DirectionDescriptionAdapter> getGrid() {
        GridProvider<DirectionDescriptionAdapter> gridProvider = new GridProvider<>(
                DirectionDescriptionAdapter.class);
        gridProvider.getGrid().getColumns().get(1).setHeader("Command");
        gridProvider.addColumn(DirectionDescriptionAdapter::getDestinationDescription, "Destination");
        gridProvider.hideIdColumn();

        gridProvider.addItemDoubleClickListener(
                e -> UI.getCurrent().navigate(DirectionEditorView.class,
                                              new RouteParameters(
                                                      new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                     adventureData.getId()),
                                                      new RouteParam(RouteIds.LOCATION_ID.getValue(),
                                                                     locationData.getId()),
                                                      new RouteParam(RouteIds.DIRECTION_ID.getValue(),
                                                                     e.getItem().getId())))
                       .ifPresent(editor -> editor.setData(locationData, adventureData)));

        ViewSupporter.setSize(gridProvider.getGrid());
        gridProvider.getGrid().setEmptyStateText("Create some exits.");
        return gridProvider.getGrid();
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<String> locId = event.getRouteParameters().get(RouteIds.LOCATION_ID.getValue());
        if (locId.isPresent()) {
            final String locationId = locId.get();
            pageTitle = "Directions for location #" + locationId;
        } else {
            pageTitle = "Directions";
        }
    }

    private void fillGrid(Set<DirectionData> directionData) {
        List<DirectionDescriptionAdapter> adapters = directionData.stream()
                .map(d -> new DirectionDescriptionAdapter(d, adventureData))
                .toList();
        directionsDataView = grid.setItems(adapters);
    }

    public void setData(AdventureData anAdventureData, LocationData aLocationData) {
        adventureData = anAdventureData;
        locationData = aLocationData;

        final Set<DirectionData> directionsData = locationData.getDirectionsData();

        fillGrid(directionsData);

        // Add context menu
        new DirectionContextMenu(grid);
    }

    private void navigateToDirectionEditor(String aDirectionId) {
        UI.getCurrent().navigate(DirectionEditorView.class,
                                 new RouteParameters(
                                         new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()),
                                         new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                                         new RouteParam(RouteIds.DIRECTION_ID.getValue(), aDirectionId)))
          .ifPresent(editor -> editor.setData(locationData, adventureData));
    }

    private class DirectionContextMenu extends GridContextMenu<DirectionDescriptionAdapter> {
        public DirectionContextMenu(Grid<DirectionDescriptionAdapter> target) {
            super(target);

            addItem("Edit", e -> e.getItem().ifPresent(adapter -> {
                navigateToDirectionEditor(adapter.getId());
            }));

            addComponent(new Hr());

            addItem("Delete", e -> e.getItem().ifPresent(adapter -> {
                locationData.getDirectionsData().remove(adapter.getDirectionData());
                adventureService.saveLocationData(locationData);
                directionsDataView.removeItem(adapter);
            }));
        }
    }
}
