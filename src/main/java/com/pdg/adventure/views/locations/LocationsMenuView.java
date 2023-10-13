package com.pdg.adventure.views.locations;

import com.pdg.adventure.api.Describable;
import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.views.adventure.AdventureEditorView;
import com.pdg.adventure.views.support.GridProvider;
import com.pdg.adventure.views.support.ViewSupporter;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Route(value = "adventures/:adventureId/locations", layout = LocationsMainLayout.class)
@RouteAlias(value = "adventures/locations",  layout = LocationsMainLayout.class)
public class LocationsMenuView extends VerticalLayout
        implements HasDynamicTitle, BeforeLeaveObserver, BeforeEnterObserver {

    private transient final AdventureService adventureService;
    private final Binder<AdventureData> binder;

    private final Div gridContainer;

    private String targetLocationId;
    private AdventureData adventureData;
    private String pageTitle = "";

    private TextField startLocation;
    private Button create;
    private Button edit;
    private TextField searchField;
    private Button backButton;
    private IntegerField numberOfLocations;

    @Autowired
    public LocationsMenuView(AdventureService anAdventureService) {
        adventureService = anAdventureService;

        binder = new Binder<>(AdventureData.class);

        startLocation = getEntryLocation();
        startLocation.setTooltipText("This is the location where a new adventures start.");
        startLocation.setReadOnly(true);
        startLocation.setWidth(300, Unit.PIXELS);

        numberOfLocations = new IntegerField("Locations:");
        numberOfLocations.setTooltipText("This is the number of locations you have defined.");

        edit = new Button("Edit Location", e -> {
            if (binder.writeBeanIfValid(adventureData)) {
                UI.getCurrent().navigate(LocationEditorView.class,
                                     new RouteParameters(
                                             new RouteParam("locationId", targetLocationId),
                                             new RouteParam("adventureId", adventureData.getId()))
                )
//                                             .ifPresent(editor -> editor.setAdventureData(adventureData))
                ;
            }
        });
        edit.setEnabled(false);

        create = new Button("Create Location", e -> {
            UI.getCurrent().navigate(LocationEditorView.class,
                    new RouteParameters(
                            new RouteParam("locationId", "new"),
                            new RouteParam("adventureId", adventureData.getId())
                ));
        });

        backButton = new Button("Back", event -> {
            UI.getCurrent().navigate(AdventureEditorView.class,
                    new RouteParameters(
                            new RouteParam("adventureId", adventureData.getId()))
            );
        });

        VerticalLayout leftSide = new VerticalLayout(startLocation, numberOfLocations, edit, create, backButton);

        searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Find location");
        searchField.setTooltipText("Find locations by ID, noun or any text in its short description");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);

        gridContainer = new Div();
        gridContainer.setWidth("100%");
        gridContainer.setHeight("100%");

        VerticalLayout rightSide = new VerticalLayout(searchField, gridContainer);

        HorizontalLayout jumpRow = new HorizontalLayout(leftSide, rightSide);

        setMargin(true);
        setPadding(true);

        add(jumpRow);
    }

    private TextField getEntryLocation() {
        TextField field = new TextField("Entry location");
        binder.bind(field, AdventureData::getCurrentLocationId, AdventureData::setCurrentLocationId);
        return field;
    }

    class LocationDescriptionAdapter implements Describable {
        private final LocationData locationData;

        public LocationDescriptionAdapter(LocationData aLocationData) {
            this.locationData = aLocationData;
        }

        @Override
        public String getAdjective() {
            return locationData.getDescriptionData().getAdjective();
        }

        @Override
        public String getNoun() {
            return locationData.getDescriptionData().getNoun();
        }

        @Override
        public String getBasicDescription() {
            return locationData.getDescriptionData().getAdjective() + " " + locationData.getDescriptionData().getNoun();
        }

        @Override
        public String getEnrichedBasicDescription() {
            return getBasicDescription();
        }

        @Override
        public String getShortDescription() {
            return locationData.getDescriptionData().getShortDescription();
        }

        @Override
        public String getLongDescription() {
            return locationData.getDescriptionData().getLongDescription();
        }

        @Override
        public String getEnrichedShortDescription() {
            return locationData.getDescriptionData().getShortDescription();
        }

        @Override
        public String getId() {
            return locationData.getId();
        }

        @Override
        public void setId(String anId) {
            locationData.setId(anId);
        }

        public int getLumen() {
            return locationData.getLumen();
        }
    }


    private Grid<LocationDescriptionAdapter> getLocationsGrid2(List<LocationData> locations) {
        GridProvider<LocationDescriptionAdapter> gridProvider = new GridProvider<>(LocationDescriptionAdapter.class);
        gridProvider.addColumn(LocationDescriptionAdapter::getLumen, "Lumen");

        Grid<LocationDescriptionAdapter> grid = gridProvider.getGrid();
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

            if (searchTerm.isEmpty())
                return true;

            boolean matchesNoun = LocationsMenuView.this.matchesTerm(aLocationDescription.getNoun(), searchTerm);
            boolean matchesShortDescription = LocationsMenuView.this.matchesTerm(aLocationDescription.getShortDescription(), searchTerm);
            boolean matchesId = LocationsMenuView.this.matchesTerm(aLocationDescription.getId(), searchTerm);

            return matchesNoun || matchesShortDescription || matchesId;
        }, locationDescriptions, searchField);
        return grid;
    }

    private Grid<LocationData> getLocationsGrid(List<LocationData> locations) {
        Grid<LocationData> grid = new Grid<>(LocationData.class, false);
        grid.addColumn(ViewSupporter::formatId).setHeader("Id").setAutoWidth(true).setFlexGrow(0);
        grid.addColumn(ViewSupporter::formatDescription).setHeader("Short Description").setSortable(true)
            .setAutoWidth(true);
        grid.addColumn(LocationData::getLumen).setHeader("Lumen");
        //        grid.setMultiSort(true, Grid.MultiSortPriority.APPEND);

        grid.addSelectionListener(selection -> {
            Optional<LocationData> optionalLocation = selection.getFirstSelectedItem();
            if (optionalLocation.isPresent()) {
                targetLocationId = optionalLocation.get().getId();
                edit.setEnabled(true);
            } else {
                edit.setEnabled(false);
            }
        });
        grid.addItemDoubleClickListener(e -> {
            targetLocationId = e.getItem().getId();
            navigateToLocationEditor(targetLocationId);
        });

        createDataView(locations, grid);

        grid.setWidth("500px");
        grid.setHeight("500px");
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

        LocationDataContextMenu contextMenu = new LocationDataContextMenu(grid);

        return grid;
    }

    private void createDataView(List<LocationData> locations, Grid<LocationData> grid) {
        GridListDataView<LocationData> dataView = grid.setItems(locations);
        searchField.addValueChangeListener(e -> dataView.refreshAll());

        dataView.addFilter(new SerializablePredicate<LocationData>() {
            @Override
            public boolean test(LocationData aLocationData) {
                String searchTerm = searchField.getValue().trim();

                if (searchTerm.isEmpty())
                    return true;

                boolean matchesFullName = LocationsMenuView.this.matchesTerm(aLocationData.getDescriptionData().getNoun(), searchTerm);
                boolean matchesEmail = LocationsMenuView.this.matchesTerm(aLocationData.getDescriptionData().getShortDescription(), searchTerm);
                boolean matchesProfession = LocationsMenuView.this.matchesTerm(aLocationData.getId(), searchTerm);

                return matchesFullName || matchesEmail || matchesProfession;
            }
        });
    }

    private boolean matchesTerm(String value, String searchTerm) {
        return value.toLowerCase().contains(searchTerm.toLowerCase());
    }

    @Override
    public String getPageTitle() {
        return pageTitle;
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

    private void fillGUI() {
        List<LocationData> locations = List.copyOf(adventureData.getLocationData());
        numberOfLocations.setValue(locations.size());
        final LocationData locationById = adventureService.findLocationById(adventureData.getCurrentLocationId());
        startLocation.setValue(getLocationsShortedDescription(locationById));

        gridContainer.add(getLocationsGrid2(locations));

        checkIfSaveAvailable();
    }

    private void checkIfSaveAvailable() {
        if (binder.validate().isOk()) {
//            saveButton.setEnabled(!binder.getBean().getTitle().isEmpty());
        }
    }


    private void setUpNewEdit() {
        final AdventureData adventureData = new AdventureData();
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

    private class LocationDataContextMenu extends GridContextMenu<LocationData> {
        public LocationDataContextMenu(Grid<LocationData> target) {
            super(target);

            addItem("Edit", e -> e.getItem().ifPresent(location -> {
                navigateToLocationEditor(location.getId());
            }));

            addItem("Select as start", e -> e.getItem().ifPresent(location -> {
                String shortDescription = getLocationsShortedDescription(location);
                startLocation.setValue(location.getId().substring(0,8) + " " + shortDescription);
                adventureData.setCurrentLocationId(location.getId());
                adventureService.saveAdventureData(adventureData);
            }));

            add(new Hr());

            GridMenuItem<LocationData> locationDetailItem =
                    addItem("LocationId", e -> e.getItem().ifPresent(location -> {
                        // System.out.printf("Email: %s%n", location.getXYZ());
                    }));

            setDynamicContentHandler(location -> {
                // Do not show context menu when header is clicked
                if (location == null) {
                    return false;
                }
                locationDetailItem.scrollIntoView();
                locationDetailItem.setText(location.getDescriptionData().getLongDescription());
                return true;
            });

            add(new Hr());

            addItem("Delete", e -> e.getItem().ifPresent(location -> {
                // TODO: check that no directions exist, pointing to this location
                ListDataProvider<LocationData> dataProvider =
                        (ListDataProvider<LocationData>) target.getDataProvider();
                dataProvider.getItems().remove(location);
                dataProvider.refreshAll();
                adventureService.deleteLocation(location.getId());
            }));
        }
    }

    private void navigateToLocationEditor(String aLocationId) {
        if (binder.writeBeanIfValid(adventureData)) {
            UI.getCurrent().navigate(LocationEditorView.class,
                                 new RouteParameters(
                                         new RouteParam("locationId", targetLocationId),
                                         new RouteParam("adventureId", adventureData.getId()))
            ).ifPresent(e -> {
                e.setAdventureData(adventureData);
            });
        }
    }

    private static String getLocationsShortedDescription(LocationData location) {
        String noun;
        if (location.getDescriptionData().getNoun() == null || location.getDescriptionData().getNoun().isEmpty()) {
            return "";
        }
        noun =  location.getDescriptionData().getNoun();

        if (location.getDescriptionData().getAdjective() == null || location.getDescriptionData().getAdjective().isEmpty()) {
            return noun;
        }
        String adjective = location.getDescriptionData().getAdjective();

        String shortDescription = noun + " / " + adjective;
        if (shortDescription.length() > 20) {
            shortDescription = shortDescription.substring(0, 20) + "...";
        }
        return shortDescription;
    }

    public void setAdventureData(AdventureData anAdventureData) {
        adventureData = anAdventureData;
        fillGUI();
    }
}
