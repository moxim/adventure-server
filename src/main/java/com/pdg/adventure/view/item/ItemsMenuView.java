package com.pdg.adventure.view.item;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;

import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.ItemService;
import com.pdg.adventure.view.adventure.AdventuresMenuView;
import com.pdg.adventure.view.location.LocationEditorView;
import com.pdg.adventure.view.location.LocationsMenuView;
import com.pdg.adventure.view.support.AdventureRouteResolver;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

@Route(value = "author/adventures/:adventureId/locations/:locationId/items", layout = ItemsMainLayout.class)
@PageTitle("Items")
@RolesAllowed("ROLE_AUTHOR")
public class ItemsMenuView extends VerticalLayout implements BeforeEnterObserver {

    private final transient AdventureService adventureService;
    private final transient AdventureAccessService accessService;
    private final Div gridContainer;
    private final Button create;
    private final Button edit;
    private final Button backButton;
    private transient AdventureData adventureData;
    private transient LocationData locationData;
    private transient ItemViewSupporter itemViewSupporter;
    private transient String selectedItemId;

    public ItemsMenuView(AdventureService anAdventureService, ItemService anItemService,
                         AdventureAccessService anAccessService) {

        setSizeFull();

        adventureService = anAdventureService;
        accessService = anAccessService;

        edit = new Button("Edit Item", _ -> {
            UI.getCurrent().navigate(ItemEditorView.class,
                                     new RouteParameters(new RouteParam(RouteIds.ITEM_ID.getValue(),
                                                                        itemViewSupporter.getTargetItemId()),
                                                         new RouteParam(RouteIds.LOCATION_ID.getValue(),
                                                                        locationData.getId()),
                                                         new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                        adventureData.getId())));
        });
        edit.setEnabled(false);

        create = new Button("Create Item", _ -> {
            UI.getCurrent().navigate(ItemEditorView.class, new RouteParameters(
                      new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                      new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())));
        });

        backButton = new Button("Back to Location", _ -> {
            UI.getCurrent().navigate(LocationEditorView.class, new RouteParameters(
//                                  new RouteParam(RouteIds.ITEM_ID.getValue(), selectedItemId),
                                  new RouteParam(RouteIds.LOCATION_ID.getValue(), locationData.getId()),
                                  new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())));
        });
        backButton.addClickShortcut(Key.ESCAPE);

        VerticalLayout leftSide = new VerticalLayout(edit, create, backButton);
        leftSide.setMaxWidth("15%");

        gridContainer = new Div();
        gridContainer.setSizeFull();

        VerticalLayout rightSide = new VerticalLayout(ViewSupporter.doubleClickEditHint(), gridContainer);
        rightSide.setSizeFull();

        HorizontalLayout jumpRow = new HorizontalLayout(leftSide, rightSide);
        jumpRow.setSizeFull();

        setMargin(true);
        setPadding(true);

        add(jumpRow);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<AdventureData> resolvedAdventure = AdventureRouteResolver.resolveAdventure(event, accessService);
        if (resolvedAdventure.isEmpty()) {
            event.forwardTo(AdventuresMenuView.class);
            return;
        }
        Optional<LocationData> resolvedLocation = AdventureRouteResolver.resolveLocation(resolvedAdventure.get(), event);
        if (resolvedLocation.isEmpty()) {
            event.forwardTo(LocationsMenuView.class, new RouteParameters(
                    new RouteParam(RouteIds.ADVENTURE_ID.getValue(), resolvedAdventure.get().getId())));
            return;
        }
        final Optional<String> optionalItemId = event.getRouteParameters().get(RouteIds.ITEM_ID.getValue());
        if (optionalItemId.isPresent()) {
            selectedItemId = optionalItemId.get();
        } else {
            selectedItemId = null;
        }
        setData(resolvedAdventure.get(), resolvedLocation.get());
    }

    private void setData(AdventureData anAdventureData, LocationData aLocationData) {
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
