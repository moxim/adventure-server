package com.pdg.adventure.view.location;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;

import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.PictureData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.view.adventure.AdventureEditorView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.RouteIds;

/**
 * Shows the picture the author chose as the adventure's world map (set in the {@link AdventureEditorView}),
 * stretched into a 16:9 frame of at most 1280 x 720 pixels with a clickable 10 x 10 grid on top.
 * Without a chosen picture it hints at where to choose one.
 */
@PageTitle("Your World")
@Route(value = "author/adventures/:adventureId/map", layout = LocationsMainLayout.class)
@RolesAllowed("ROLE_AUTHOR")
@StyleSheet("styles/world-map.css")
public class LocationMapView extends VerticalLayout implements BeforeEnterObserver {

    private static final int GRID_SIZE = 10;
    private static final String MAX_WIDTH = "1280px";
    private static final String TEXT_POSITION = "position";

    private final transient AdventureAccessService accessService;

    public LocationMapView(AdventureAccessService anAccessService) {
        accessService = anAccessService;
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
        PictureData worldMap = adventureData.getWorldMapPictureId() == null
                               ? null
                               : adventureData.getPictureData().get(adventureData.getWorldMapPictureId());
        add(worldMap == null ? createHint(adventureData) : createMap(worldMap));
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

        Div grid = new Div();
        grid.getStyle().set(TEXT_POSITION, "absolute").set("inset", "0").set("display", "grid")
            .set("grid-template-columns", "repeat(" + GRID_SIZE + ", 1fr)")
            .set("grid-template-rows", "repeat(" + GRID_SIZE + ", 1fr)");
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                grid.add(createCell(x, y));
            }
        }

        Div frame = new Div(image, grid);
        frame.addClassName("world-map");
        frame.getStyle().set(TEXT_POSITION, "relative").set("width", "100%").set("max-width", MAX_WIDTH)
             .set("aspect-ratio", "16 / 9");
        return frame;
    }

    private static Div createCell(int aColumn, int aRow) {
        Div cell = new Div();
        cell.addClassName("world-map-cell");
        cell.addClickListener(_ -> Notification.show("Location " + aColumn + " : " + aRow));
        return cell;
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
