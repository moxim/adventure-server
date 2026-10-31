package com.pdg.adventure.view.location;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;

import java.util.List;
import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.PictureData;
import com.pdg.adventure.model.basic.DescriptionData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.support.DescriptionProvider;
import com.pdg.adventure.view.adventure.AdventureEditorView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.PictureGrid;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

/**
 * Shows the picture the author chose as the adventure's world map (set in the {@link AdventureEditorView}),
 * stretched into a 16:9 frame of at most 1280 x 720 pixels (and half the window height) with a clickable 10 x 10 grid on top.
 * Without a chosen picture it hints at where to choose one.
 * <p>
 * On the grid the author places the adventure's locations: a cell that holds a location is drawn darker and shows the
 * location's description when hovered. In {@link Mode#PLACE} a click on an empty cell places one of the unplaced
 * locations there and a click on an occupied cell replaces its location; in {@link Mode#CONNECT} a click on one
 * location and then on another connects them with directions.
 */
@PageTitle("Your World")
@Route(value = "author/adventures/:adventureId/map", layout = LocationsMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
@StyleSheet("styles/world-map.css")
public class LocationMapView extends VerticalLayout implements BeforeEnterObserver {

    // At most 1280 x 720 pixels, and at most half the window high (the 16:9 frame is that high at 50vh * 16 / 9 wide),
    // so a wide window that is not tall does not let the map take all of it.
    private static final String MAX_WIDTH = "min(1280px, calc(50vh * 16 / 9))";
    private static final String TEXT_POSITION = "position";

    private static final String OCCUPIED_CLASS = "world-map-cell-occupied";
    private static final String TITLE = "title";
    private static final String NO_UNPLACED_LOCATIONS = "No unplaced locations.";
    private static final String SELECTED_CLASS = "world-map-cell-selected";

    /** What a click on a cell of the map does. */
    enum Mode {
        PLACE("Place"),
        CONNECT("Connect");

        private final String label;

        Mode(String aLabel) {
            label = aLabel;
        }

        String getLabel() {
            return label;
        }
    }

    private final transient AdventureAccessService accessService;
    private final transient AdventureService adventureService;
    private final RadioButtonGroup<Mode> mode = new RadioButtonGroup<>();

    private transient AdventureData adventure;
    private Div overlay;
    private transient LocationData pendingSource;
    private transient PictureGrid.Cell pendingSourceCell;
    private transient Dialog openDialog;

    public LocationMapView(AdventureAccessService anAccessService, AdventureService anAdventureService) {
        accessService = anAccessService;
        adventureService = anAdventureService;
        mode.setItems(Mode.values());
        mode.setItemLabelGenerator(Mode::getLabel);
        mode.setValue(Mode.PLACE);
        mode.addValueChangeListener(_ -> clearPendingSource());
        setSizeFull();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> adventure = AdventureRouteResolver.resolveAdventureOrForward(event, accessService);
        if (adventure.isEmpty()) {
            return;
        }
        removeAll();
        AdventureData adventureData = adventure.get();
        this.adventure = adventureData;
        pendingSource = null;
        pendingSourceCell = null;
        PictureData worldMap = adventureData.getWorldMapPictureId() == null
                               ? null
                               : adventureData.getPictureData().get(adventureData.getWorldMapPictureId());
        if (worldMap == null) {
            add(createHint(adventureData));
        } else {
            add(mode, createMap(worldMap));
            refresh();
        }
    }

    /** What a click on a grid cell does, depending on the mode. */
    void onCellClicked(PictureGrid.Cell aCell) {
        if (openDialog != null && openDialog.isOpened()) {
            // A double-click sends a second click before the first dialog shows; it must not open another one.
            return;
        }
        if (mode.getValue() == Mode.PLACE) {
            Optional<LocationData> occupant = LocationMapPlacement.locationAt(adventure, aCell);
            if (occupant.isEmpty()) {
                openPlaceDialog(aCell);
            } else {
                openReplaceDialog(occupant.get());
            }
        } else {
            onConnectClick(aCell);
        }
    }

    /** The first click on a location picks the source, the second one the target of the new direction. */
    private void onConnectClick(PictureGrid.Cell aCell) {
        Optional<LocationData> clicked = LocationMapPlacement.locationAt(adventure, aCell);
        if (clicked.isEmpty()) {
            Notification.show("Click a cell that holds a location.");
            return;
        }
        if (pendingSource == null) {
            pendingSource = clicked.get();
            pendingSourceCell = aCell;
            PictureGrid.cellComponent(overlay, aCell).addClassName(SELECTED_CLASS);
            Notification.show("Now click the target location.");
            return;
        }
        LocationData source = pendingSource;
        clearPendingSource();
        open(new ConnectLocationsDialog(adventure, adventureService, source, clicked.get(), this::refresh));
    }

    private void open(Dialog aDialog) {
        openDialog = aDialog;
        aDialog.open();
    }

    private void clearPendingSource() {
        if (pendingSourceCell != null) {
            PictureGrid.cellComponent(overlay, pendingSourceCell).removeClassName(SELECTED_CLASS);
        }
        pendingSource = null;
        pendingSourceCell = null;
    }

    private void openPlaceDialog(PictureGrid.Cell aCell) {
        List<LocationData> unplaced = LocationMapPlacement.unplaced(adventure);
        if (unplaced.isEmpty()) {
            Notification.show(NO_UNPLACED_LOCATIONS);
            return;
        }
        open(new PlaceLocationDialog(adventureService, unplaced, aCell, this::refresh));
    }

    private void openReplaceDialog(LocationData anOccupant) {
        if (LocationMapPlacement.unplaced(adventure).isEmpty()) {
            Notification.show(NO_UNPLACED_LOCATIONS);
            return;
        }
        open(new ReplaceLocationDialog(adventure, adventureService, anOccupant, this::refresh));
    }

    /** Draws the cells that hold a location darker, with the location's description as hover text. */
    private void refresh() {
        for (int row = 0; row < PictureGrid.SIZE; row++) {
            for (int column = 0; column < PictureGrid.SIZE; column++) {
                Div cell = PictureGrid.cellComponent(overlay, new PictureGrid.Cell(column, row));
                cell.removeClassName(OCCUPIED_CLASS);
                cell.getElement().removeAttribute(TITLE);
            }
        }
        for (LocationData location : adventure.getLocationData().values()) {
            if (!isOnTheGrid(location)) {
                continue;
            }
            Div cell = PictureGrid.cellComponent(overlay, new PictureGrid.Cell(location.getMapColumn(),
                                                                               location.getMapRow()));
            // TODO: Review needed — two locations stored on one cell: the first one wins and the other is not shown
            if (!cell.hasClassName(OCCUPIED_CLASS)) {
                cell.addClassName(OCCUPIED_CLASS);
                // TODO: Review needed — native title attribute as hover text; a Vaadin Tooltip may fit the look better
                cell.getElement().setAttribute(TITLE, hoverText(location));
            }
        }
    }

    private static boolean isOnTheGrid(LocationData aLocation) {
        return aLocation.getMapColumn() != null && aLocation.getMapRow() != null
               && aLocation.getMapColumn() >= 0 && aLocation.getMapColumn() < PictureGrid.SIZE
               && aLocation.getMapRow() >= 0 && aLocation.getMapRow() < PictureGrid.SIZE;
    }

    private static String hoverText(LocationData aLocation) {
        DescriptionData description = aLocation.getDescriptionData();
        // DescriptionProvider cannot describe something without noun, adjective and short description.
        boolean describable = isFilled(description.getShortDescription())
                              || (description.getNoun() != null && isFilled(description.getNoun().getText()))
                              || (description.getAdjective() != null && isFilled(description.getAdjective().getText()));
        String text = describable ? new DescriptionProvider(description).getEnrichedBasicDescription() : "";
        if (text.isBlank()) {
            text = ViewSupporter.formatDescription(aLocation);
        }
        return text.isBlank() ? aLocation.getId() : text;
    }

    private static boolean isFilled(String aText) {
        return aText != null && !aText.isBlank();
    }

    private Div createMap(PictureData aWorldMap) {
        Image image = new Image();
        image.setSrc(event -> {
            event.inline();
            event.setContentType(aWorldMap.getContentType());
            event.getOutputStream().write(aWorldMap.getContent());
        });
        image.setAlt(aWorldMap.getName());
        image.setSizeFull();
        // Out of flow like the grid, so only the frame's aspect-ratio sets its height, never the image's own size.
        image.getStyle().set("object-fit", "fill").set(TEXT_POSITION, "absolute").set("inset", "0");

        overlay = PictureGrid.createOverlay(this::onCellClicked);
        Div frame = new Div(image, overlay);
        frame.addClassName("world-map");
        frame.getStyle().set(TEXT_POSITION, "relative").set("width", "100%").set("max-width", MAX_WIDTH)
             .set("aspect-ratio", "16 / 9");
        return frame;
    }

    private Div createHint(AdventureData anAdventureData) {
        Button openEditor = new Button("Open the Adventure Editor", _ -> UI.getCurrent().navigate(
                AdventureEditorView.class,
                new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), anAdventureData.getId()))));
        return new Div(new Paragraph("No world map has been chosen for this adventure yet. Upload a picture under "
                                     + "\"Pictures\", then select it as the World Map in the Adventure Editor."),
                       openEditor);
    }
}
