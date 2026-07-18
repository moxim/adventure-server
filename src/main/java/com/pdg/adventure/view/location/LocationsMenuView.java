package com.pdg.adventure.view.location;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
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
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.adventure.AdventureEditorView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.GridProvider;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

@Route(value = "author/adventures/:adventureId/locations", layout = LocationsMainLayout.class)
@PageTitle("Locations")
@RolesAllowed("ROLE_AUTHOR")
public class LocationsMenuView extends VerticalLayout implements BeforeLeaveObserver, BeforeEnterObserver {

    private static final String LOCATION_ID = "locationId";
    private static final String ADVENTURE_ID = "adventureId";

    private final transient AdventureService adventureService;
    private final transient AdventureAccessService accessService;
    private final Binder<AdventureData> binder;

    private final Div gridContainer;

    private String targetLocationId;
    private transient AdventureData adventureData;

    private final ComboBox<LocationData> entryLocationSelector;
    private final Button create;
    private final Button edit;
    private final TextField searchField;
    private final Button backButton;
    private final Span numberOfLocations;

    public LocationsMenuView(AdventureService anAdventureService, AdventureAccessService anAccessService) {

        setSizeFull();

        adventureService = anAdventureService;
        accessService = anAccessService;

        binder = new Binder<>(AdventureData.class);

        entryLocationSelector = getEntryLocation();
        entryLocationSelector.setTooltipText("This is the location where a new adventures start.");
        // Picking a location here sets it as the adventure's start (and persists it) -- a discoverable
        // alternative to the "Select as start" context-menu item, sharing the same setStartLocation() path.
        entryLocationSelector.addValueChangeListener(event -> {
            if (event.isFromClient() && event.getValue() != null) {
                setStartLocation(event.getValue());
            }
        });

        numberOfLocations = new Span();

        edit = new Button("Edit Location", _ -> {
                UI.getCurrent().navigate(LocationEditorView.class,
                                         new RouteParameters(
                                                 new RouteParam(RouteIds.LOCATION_ID.getValue(), targetLocationId),
                                                 new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                adventureData.getId())));
        });
        edit.setEnabled(false);

        create = new Button("Create Location", _ -> {
            UI.getCurrent().navigate(LocationEditorView.class, new RouteParameters(
//                            new RouteParam(LOCATION_ID, "new"),
                      new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())));
        });

        backButton = new Button("Back", _ -> {
            UI.getCurrent().navigate(AdventureEditorView.class,
                                     new RouteParameters(
                                             new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())));
        });
        backButton.addClickShortcut(Key.ESCAPE);

        VerticalLayout leftSide = new VerticalLayout(entryLocationSelector, numberOfLocations, edit, create, backButton);
        leftSide.setMaxWidth("25%");
        leftSide.setMinWidth("25%");
        leftSide.setWidth("25%");

        searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Find location");
        searchField.setTooltipText("Find locations by ID, noun or any text in its short description");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        gridContainer = new Div();
        gridContainer.setSizeFull();

        VerticalLayout rightSide = new VerticalLayout(searchField, ViewSupporter.doubleClickEditHint(), gridContainer);
        rightSide.setSizeFull();

        HorizontalLayout jumpRow = new HorizontalLayout(leftSide, rightSide);
        jumpRow.setSizeFull();

        setMargin(true);
        setPadding(true);

        add(jumpRow);
    }

    private ComboBox<LocationData> getEntryLocation() {
        ComboBox<LocationData> field = new ComboBox<>("Entry location");
        field.setItemLabelGenerator(ViewSupporter::getLocationsShortedDescription);
        return field;
    }

    /** Single source of truth for setting (and persisting) the adventure's start location. */
    private void setStartLocation(LocationData aLocation) {
        adventureData.setCurrentLocationId(aLocation.getId());
        adventureService.saveAdventureData(adventureData);
    }


    private Grid<LocationDescriptionAdapter> getLocationsGrid(List<LocationData> locations) {
        GridProvider<LocationDescriptionAdapter> gridProvider = new GridProvider<>(LocationDescriptionAdapter.class);
        gridProvider.hideIdColumn();

        Grid<LocationDescriptionAdapter> grid = gridProvider.getGrid();

        Span lumenHeader = new Span("Lumen");
        lumenHeader.getElement().setAttribute("title", "Whether the location is lit (1 = lit, 0 = dark)");
        grid.addColumn(LocationDescriptionAdapter::getLumen).setHeader(lumenHeader).setAutoWidth(true);

        Span usedHeader = new Span("Used");
        usedHeader.getElement().setAttribute("title", "How many exits lead to this location");
        grid.addColumn(LocationDescriptionAdapter::getUsageCount).setHeader(usedHeader).setAutoWidth(true);

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


        ViewSupporter.setSize(grid);
        grid.setEmptyStateText("No locations found. Create some to get the story going.");

        return grid;
    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value.toLowerCase().contains(searchTerm.toLowerCase());
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventureOrForward(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            return;
        }
        setAdventureData(resolvedAdventure.get());
    }

    private void setAdventureData(AdventureData anAdventureData) {
        adventureData = anAdventureData;
        fillGUI();
    }

    private void fillGUI() {
        List<LocationData> locations = new ArrayList<>(adventureData.getLocationData().values());
        numberOfLocations.setText("Locations: " + locations.size());
        entryLocationSelector.setItems(locations);
        entryLocationSelector.setValue(adventureData.getLocationData().get(adventureData.getCurrentLocationId()));
        gridContainer.add(getLocationsGrid(locations));
    }

    @Override
    public void beforeLeave(BeforeLeaveEvent event) {
        // TODO: this triggers even if nothing has changed....
//        AdventuresMainLayout.checkIfUserWantsToLeavePage(event, binder.hasChanges());
    }

    private void navigateToLocationEditor(String aLocationId) {
        UI.getCurrent().navigate(LocationEditorView.class, new RouteParameters(new RouteParam(LOCATION_ID, aLocationId),
                                                                               new RouteParam(ADVENTURE_ID,
                                                                                              adventureData.getId())));
    }

    private class LocationDataContextMenu extends GridContextMenu<LocationDescriptionAdapter> {
        public LocationDataContextMenu(Grid<LocationDescriptionAdapter> target) {
            super(target);

            addItem("Edit", e -> e.getItem().ifPresent(location -> navigateToLocationEditor(location.getId())));

            addItem("Select as start", e -> e.getItem().ifPresent(adapter -> {
                LocationData loc = adventureData.getLocationData().get(adapter.getId());
                if (loc != null) {
                    setStartLocation(loc);
                    entryLocationSelector.setValue(loc);
                }
            }));

            addItem("Find Usage", e -> e.getItem().ifPresent(LocationsMenuView.this::showLocationUsage));

            addComponent(new Hr());

            GridMenuItem<LocationDescriptionAdapter> locationDetailItem = addItem("LocationId", e -> e.getItem()
                                                                                                      .ifPresent(
                                                                                                              _ -> {
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
        List<LocationUsageTracker.LocationUsage> usages = LocationUsageTracker.findLocationUsages(adventureData,
                                                                                                  aLlocation.getId());
        ViewSupporter.showUsages("Location Usage", "location", aLlocation.getId(), usages);
    }

    private void confirmDeleteLocation(LocationDescriptionAdapter aLlocation) {
        String locationId = aLlocation.getId();
        int usageCount = LocationUsageTracker.countLocationUsages(adventureData, locationId);

        if (usageCount > 0) {
            Notification notification = Notification.show(
                    "Cannot delete location '" + locationId +
                    "' because it is still referenced " + usageCount +
                    " time(s). Please remove those references first.",
                    5000, Notification.Position.MIDDLE);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        } else {
            final var dialog = getConfirmDialog(aLlocation);
            dialog.addConfirmListener(_ -> {
                adventureData.getLocationData().remove(locationId);
                if (locationId.equals(adventureData.getCurrentLocationId())) {
                    adventureData.setCurrentLocationId("");
                    entryLocationSelector.clear();
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
