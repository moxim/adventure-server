package com.pdg.adventure.view.location;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.grid.contextmenu.GridMenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
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

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.view.adventure.AdventureEditorView;
import com.pdg.adventure.view.support.GridProvider;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

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
                                            new RouteParameters(new RouteParam(RouteIds.LOCATION_ID.getValue(), targetLocationId),
                                                             new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())))
                                .ifPresent(editor -> editor.setData(adventureData));
            }
        });
        edit.setEnabled(false);

        create = new Button("Create Location", e -> {
            UI.getCurrent().navigate(LocationEditorView.class, new RouteParameters(
//                            new RouteParam(LOCATION_ID, "new"),
                      new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())))
              .ifPresent(editor -> editor.setData(adventureData));
        });

        backButton = new Button("Back", event -> {
            UI.getCurrent().navigate(AdventureEditorView.class,
                                     new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())));
        });
        backButton.addClickShortcut(Key.ESCAPE);

        VerticalLayout leftSide = new VerticalLayout(startLocationTF, numberOfLocations, edit, create, backButton);
        leftSide.setMaxWidth("30%");

        searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Find location");
        searchField.setTooltipText("Find locations by ID, noun or any text in its short description");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        gridContainer = new Div();
        gridContainer.setSizeFull();

        VerticalLayout rightSide = new VerticalLayout(searchField, gridContainer);
        rightSide.setSizeFull();

        HorizontalLayout jumpRow = new HorizontalLayout(leftSide, rightSide);
        jumpRow.setSizeFull();

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
        gridProvider.addColumn(LocationDescriptionAdapter::getUsageCount, "Used");

        Grid<LocationDescriptionAdapter> grid = gridProvider.getGrid();

        List<LocationDescriptionAdapter> locationDescriptions = new ArrayList<>(locations.size());
        for (LocationData location : locations) {
            int usageCount = LocationUsageTracker.countLocationUsages(adventureData, location.getId());
            locationDescriptions.add(new LocationDescriptionAdapter(location, usageCount));
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

        grid.setWidthFull();
        grid.setMaxWidth("900px");
        grid.setHeight("500px");
        grid.setEmptyStateText("No locations found. Create some to get the story going.");

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
          .ifPresent(e -> e.setData(adventureData));
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

            addItem("Find Usage", e -> e.getItem().ifPresent(LocationsMenuView.this::showLocationUsage));

            addComponent(new Hr());

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

            addComponent(new Hr());

            addItem("Delete", e -> e.getItem().ifPresent(LocationsMenuView.this::confirmDeleteLocation));
        }
    }

    private void showLocationUsage(LocationDescriptionAdapter aLlocation) {
        List<LocationUsageTracker.LocationUsage> usages = LocationUsageTracker.findLocationUsages(adventureData, aLlocation.getId());
        ViewSupporter.showUsages("Location Usage", "location", aLlocation.getId(), usages);
    }

    private void confirmDeleteLocation(LocationDescriptionAdapter aLlocation) {
        String locationId = aLlocation.getId();
        int usageCount = LocationUsageTracker.countLocationUsages(adventureData, locationId);

        if (usageCount > 0) {
            Notification.show("Cannot delete location '" + locationId +
                              "' because it is still referenced " + usageCount +
                              " time(s). Please remove those references first.",
                              5000, Notification.Position.MIDDLE);
        } else {
            final var dialog = getConfirmDialog(aLlocation);
            dialog.addConfirmListener(event -> {
                adventureData.getLocationData().remove(locationId);
                if (locationId.equals(adventureData.getCurrentLocationId())) {
                    adventureData.setCurrentLocationId("");
                    startLocationTF.clear();
                }
                adventureService.deleteLocation(locationId);
                adventureService.saveAdventureData(adventureData);

                // Refresh the grid
                gridContainer.removeAll();
                fillGUI();
            });

            dialog.open();
        }
    }

    private static ConfirmDialog getConfirmDialog(final LocationDescriptionAdapter aLocation) {
        return ViewSupporter.getConfirmDialog("Delete Location", "location", aLocation.getId());
    }

}
