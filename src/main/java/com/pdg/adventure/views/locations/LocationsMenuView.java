package com.pdg.adventure.views.locations;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.views.adventure.AdventureEditorView;
import com.pdg.adventure.views.support.GridProvider;
import com.pdg.adventure.views.support.ViewSupporter;

@Route(value = "adventures/:adventureId/locations", layout = LocationsMainLayout.class)
@RouteAlias(value = "adventures/locations", layout = LocationsMainLayout.class)
@PageTitle("Locations")
public class LocationsMenuView extends VerticalLayout implements BeforeLeaveObserver, BeforeEnterObserver {

    private static final String LOCATION_ID = "locationId";
    private static final String ADVENTURE_ID = "adventureId";

    private final transient AdventureService adventureService;
    private final Binder<AdventureData> binder;

    private final Div gridContainer;

    private String targetLocationId;
    private transient AdventureData adventureData;
    private String pageTitle = "";

    private final TextField startLocationTF;
    private final Button create;
    private final Button edit;
    private final TextField searchField;
    private final Button backButton;
    private final IntegerField numberOfLocations;

    @Autowired
    public LocationsMenuView(AdventureService anAdventureService) {

        setSizeFull();

        adventureService = anAdventureService;

        binder = new Binder<>(AdventureData.class);

        startLocationTF = getEntryLocation();
        startLocationTF.setTooltipText("This is the location where a new adventures start.");
        startLocationTF.setReadOnly(true);
        startLocationTF.setWidth(300, Unit.PIXELS);

        numberOfLocations = new IntegerField("Locations:");
        numberOfLocations.setTooltipText("This is the number of locations you have defined.");

        edit = new Button("Edit Location", e -> {
            if (binder.writeBeanIfValid(adventureData)) {
                UI.getCurrent().navigate(LocationEditorView.class,
                                         new RouteParameters(new RouteParam(LOCATION_ID, targetLocationId),
                                                             new RouteParam(ADVENTURE_ID, adventureData.getId())))
                  .ifPresent(editor -> editor.setAdventureData(adventureData));
            }
        });
        edit.setEnabled(false);

        create = new Button("Create Location", e -> {
            UI.getCurrent().navigate(LocationEditorView.class, new RouteParameters(
//                            new RouteParam(LOCATION_ID, "new"),
                      new RouteParam(ADVENTURE_ID, adventureData.getId())))
              .ifPresent(editor -> editor.setAdventureData(adventureData));
        });

        backButton = new Button("Back", event -> {
            UI.getCurrent().navigate(AdventureEditorView.class,
                                     new RouteParameters(new RouteParam(ADVENTURE_ID, adventureData.getId())));
        });
        backButton.addClickShortcut(Key.ESCAPE);

        VerticalLayout leftSide = new VerticalLayout(startLocationTF, numberOfLocations, edit, create, backButton);

        searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Find location");
        searchField.setTooltipText("Find locations by ID, noun or any text in its short description");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        gridContainer = new Div();
        gridContainer.setSizeFull();

        VerticalLayout rightSide = new VerticalLayout(searchField, gridContainer);

        HorizontalLayout jumpRow = new HorizontalLayout(leftSide, rightSide);

        setMargin(true);
        setPadding(true);

        add(jumpRow);
    }

    private TextField getEntryLocation() {
        TextField field = new TextField("Entry location");
        return field;
    }


    private Grid<LocationDescriptionAdapter> getLocationsGrid(List<LocationData> locations) {
        GridProvider<LocationDescriptionAdapter> gridProvider = new GridProvider<>(LocationDescriptionAdapter.class);
        gridProvider.addColumn(LocationDescriptionAdapter::getLumen, "Lumen");

        Grid<LocationDescriptionAdapter> grid = gridProvider.getGrid();
        grid.setWidth("500px");
        grid.setHeight("500px");
        grid.setEmptyStateText("Create some locations.");


        List<LocationDescriptionAdapter> locationDescriptions = new ArrayList<>(locations.size());
        for (LocationData location : locations) {
            locationDescriptions.add(new LocationDescriptionAdapter(location));
        }
        grid.setItems(locationDescriptions);

        gridProvider.addSelectionListener(selection -> {
            Optional<LocationDescriptionAdapter> optionalLocation = selection.getFirstSelectedItem();
            if (optionalLocation.isPresent()) {
                targetLocationId = optionalLocation.get().getId();
                edit.setEnabled(true);
            } else {
                edit.setEnabled(false);
            }
        });

        gridProvider.addItemDoubleClickListener(e -> {
            targetLocationId = e.getItem().getId();
            navigateToLocationEditor(targetLocationId);
        });

        gridProvider.setFilter(aLocationDescription -> {
            String searchTerm = searchField.getValue().trim();

            if (searchTerm.isEmpty()) {
                return true;
            }

            boolean matchesNoun = LocationsMenuView.this.matchesTerm(aLocationDescription.getNoun(), searchTerm);
            boolean matchesShortDescription = LocationsMenuView.this.matchesTerm(
                    aLocationDescription.getShortDescription(), searchTerm);
            boolean matchesId = LocationsMenuView.this.matchesTerm(aLocationDescription.getId(), searchTerm);

            return matchesNoun || matchesShortDescription || matchesId;
        }, locationDescriptions, searchField);

        LocationDataContextMenu contextMenu = new LocationDataContextMenu(grid);
        // TODO remove this again, it's only here to test if the contextmenu works
        if (contextMenu.getItems().size() < 13) {
            grid.setHeight("300px");
        }

        return grid;
    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value.toLowerCase().contains(searchTerm.toLowerCase());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
//        Optional<String> adventureId = event.getRouteParameters().get("adventureId");
//        Objects.requireNonNull(adventureId);
//
//        if (adventureId.isPresent()) {
//            setUpLoading(adventureId.get());
//        } else {
//            setUpNewEdit();
//        }
//        fillGUI();
    }

    public void setAdventureData(AdventureData anAdventureData) {
        adventureData = anAdventureData;
        fillGUI();
    }

    private void fillGUI() {
        List<LocationData> locations = new ArrayList<>(adventureData.getLocationData().values());
        numberOfLocations.setValue(locations.size());
        ViewSupporter.populateStartLocation(adventureData, startLocationTF);
        gridContainer.add(getLocationsGrid(locations));
    }

    private void setUpNewEdit() {
        adventureData = new AdventureData();
        adventureData.setId(UUID.randomUUID().toString());
        binder.setBean(adventureData);
        pageTitle = "A new adventure awaits";
    }

    private void setUpLoading(String anAdventureId) {
        loadAdventure(anAdventureId);
        pageTitle = "Locations of adventure " + adventureData.getTitle();
    }

    public void loadAdventure(String anAdventureId) {
        adventureData = adventureService.findAdventureById(anAdventureId);
        binder.setBean(adventureData);
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        // TODO: this triggers even if nothing has changed....
//        AdventuresMainLayout.checkIfUserWantsToLeavePage(event, binder.hasChanges());
    }

    private void navigateToLocationEditor(String aLocationId) {
        // TODO: do I need to check if the bean is valid?
//        if (binder.writeBeanIfValid(adventureData)) {
        UI.getCurrent().navigate(LocationEditorView.class, new RouteParameters(new RouteParam(LOCATION_ID, aLocationId),
                                                                               new RouteParam(ADVENTURE_ID,
                                                                                              adventureData.getId())))
          .ifPresent(e -> e.setAdventureData(adventureData));
//        }
    }

    private class LocationDataContextMenu extends GridContextMenu<LocationDescriptionAdapter> {
        public LocationDataContextMenu(Grid<LocationDescriptionAdapter> target) {
            super(target);

            addItem("Edit", e -> e.getItem().ifPresent(location -> navigateToLocationEditor(location.getId())));

            addItem("Select as start", e -> e.getItem().ifPresent(location -> {
                adventureData.setCurrentLocationId(location.getId());
                ViewSupporter.populateStartLocation(adventureData, startLocationTF);
                adventureService.saveAdventureData(adventureData);
            }));

            add(new Hr());

            GridMenuItem<LocationDescriptionAdapter> locationDetailItem = addItem("LocationId", e -> e.getItem()
                                                                                                      .ifPresent(
                                                                                                              location -> {
                                                                                                                  // System.out.printf("Email: %s%n", location.getXYZ());
                                                                                                              }));

            setDynamicContentHandler(location -> {
                // Do not show context menu when header is clicked
                if (location == null) {
                    return false;
                }
                locationDetailItem.scrollIntoView();
                locationDetailItem.setText(location.getLongDescription());
                return true;
            });

            add(new Hr());

            addItem("Delete", e -> e.getItem().ifPresent(location -> {
                // TODO: check that no directions exist, pointing to this location
                final GridListDataView<LocationDescriptionAdapter> view = target.getListDataView();
                view.removeItem(location);
                view.refreshAll();
                String locationId = location.getId();
                adventureData.getLocationData().remove(locationId);
                if (locationId.equals(adventureData.getCurrentLocationId())) {
                    adventureData.setCurrentLocationId("");
                    startLocationTF.clear();
                }
                adventureService.deleteLocation(locationId);
                adventureService.saveAdventureData(adventureData);
            }));
        }
    }

}
