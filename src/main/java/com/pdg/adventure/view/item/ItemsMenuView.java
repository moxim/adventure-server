package com.pdg.adventure.view.item;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.server.storage.ItemService;
import com.pdg.adventure.view.location.LocationEditorView;
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
    private transient String selectedItemId;

    public ItemsMenuView(AdventureService anAdventureService, ItemService anItemService) {

        setSizeFull();

        adventureService = anAdventureService;

        edit = new Button("Edit Item", _ -> {
            UI.getCurrent().navigate(ItemEditorView.class,
                                     new RouteParameters(new RouteParam(RouteIds.ITEM_ID.getValue(),
                                                                        itemViewSupporter.getTargetItemId()),
                                                         new RouteParam(RouteIds.LOCATION_ID.getValue(),
                                                                        locationData.getId()),
                                                         new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                        adventureData.getId())))
              .ifPresent(editor -> editor.setData(adventureData, locationData));
        });
        edit.setEnabled(false);

        create = new Button("Create Item", _ -> {
            UI.getCurrent().navigate(ItemEditorView.class, new RouteParameters(
                      new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                      new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())))
              .ifPresent(editor -> editor.setData(adventureData, locationData));
        });

        backButton = new Button("Back to Location", _ -> {
            UI.getCurrent().navigate(LocationEditorView.class, new RouteParameters(
//                                  new RouteParam(RouteIds.ITEM_ID.getValue(), selectedItemId),
                                  new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                                  new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId()))
            ).ifPresent(view -> view.setData(adventureData));
        });
        backButton.addClickShortcut(Key.ESCAPE);

        VerticalLayout leftSide = new VerticalLayout(edit, create, backButton);
        leftSide.setMaxWidth("15%");

        gridContainer = new Div();
        gridContainer.setSizeFull();

        VerticalLayout rightSide = new VerticalLayout(gridContainer);
        rightSide.setSizeFull();

        HorizontalLayout jumpRow = new HorizontalLayout(leftSide, rightSide);
        jumpRow.setSizeFull();

        setMargin(true);
        setPadding(true);

        add(jumpRow);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        final Optional<String> optionalItemId = event.getRouteParameters().get(RouteIds.ITEM_ID.getValue());

        if (optionalItemId.isPresent()) {
            selectedItemId = optionalItemId.get();
        } else {
            selectedItemId = null;
        }
    }

    public void setData(AdventureData anAdventureData, LocationData aLocationData) {
        adventureData = anAdventureData;
        locationData = aLocationData;
        itemViewSupporter = new ItemViewSupporter(adventureService, adventureData, locationData, gridContainer, edit);
        final Grid<ItemData> itemDataGrid = itemViewSupporter.fillGUI(locationData, gridContainer);
        if (selectedItemId != null) {
            itemDataGrid.select(itemDataGrid.getDataProvider()
                                          .fetch(new com.vaadin.flow.data.provider.Query<>())
                                          .filter(itemData -> itemData.getId().equals(selectedItemId))
                                          .findFirst()
                                          .orElse(null));
        }
    }
}
