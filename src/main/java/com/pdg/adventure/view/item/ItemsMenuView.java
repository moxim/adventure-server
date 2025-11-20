package com.pdg.adventure.view.item;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.server.storage.ItemService;
import com.pdg.adventure.view.location.LocationsMenuView;
import com.pdg.adventure.view.support.RouteIds;

@Route(value = "adventures/:adventureId/locations/:locationId/items", layout = ItemsMainLayout.class)
@PageTitle("Items")
public class ItemsMenuView extends VerticalLayout implements BeforeEnterObserver {

    private final transient AdventureService adventureService;
    private final Div gridContainer;
    private final Button create;
    private final Button edit;
    private final Button backButton;
    private transient AdventureData adventureData;
    private transient LocationData locationData;
    private transient ItemViewSupporter itemViewSupporter;

    @Autowired
    public ItemsMenuView(AdventureService anAdventureService, ItemService anItemService) {

        setSizeFull();

        adventureService = anAdventureService;

        edit = new Button("Edit Item", e -> {
            UI.getCurrent().navigate(ItemEditorView.class,
                                     new RouteParameters(new RouteParam(RouteIds.ITEM_ID.getValue(), itemViewSupporter.getTargetItemId()),
                                                         new RouteParam(RouteIds.LOCATION_ID.getValue(),
                                                                        locationData.getId()),
                                                         new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                        adventureData.getId())))
              .ifPresent(editor -> editor.setData(adventureData, locationData));
        });
        edit.setEnabled(false);

        create = new Button("Create Item", e -> {
            UI.getCurrent().navigate(ItemEditorView.class, new RouteParameters(
                      new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                      new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())))
              .ifPresent(editor -> editor.setData(adventureData, locationData));
        });

        backButton = new Button("Back to Location", event -> {
            UI.getCurrent().navigate(LocationsMenuView.class).ifPresent(view -> view.setAdventureData(adventureData));
        });
        backButton.addClickShortcut(Key.ESCAPE);

        VerticalLayout leftSide = new VerticalLayout(edit, create, backButton);

        gridContainer = new Div();
        gridContainer.setSizeFull();

        VerticalLayout rightSide = new VerticalLayout(gridContainer);

        HorizontalLayout jumpRow = new HorizontalLayout(leftSide, rightSide);

        setMargin(true);
        setPadding(true);

        add(jumpRow);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // The setData method will be called from navigation
    }

    public void setData(AdventureData anAdventureData, LocationData aLocationData) {
        adventureData = anAdventureData;
        locationData = aLocationData;
        itemViewSupporter = new ItemViewSupporter(adventureService, adventureData, locationData, gridContainer, edit);
        itemViewSupporter.fillGUI(locationData, gridContainer);
    }
}
