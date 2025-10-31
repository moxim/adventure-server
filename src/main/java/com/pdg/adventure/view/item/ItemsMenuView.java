package com.pdg.adventure.view.item;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.storage.AdventureService;
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
    private String targetItemId;
    private transient AdventureData adventureData;
    private transient LocationData locationData;

    @Autowired
    public ItemsMenuView(AdventureService anAdventureService) {

        setSizeFull();

        adventureService = anAdventureService;

        edit = new Button("Edit Item", e -> {
            UI.getCurrent().navigate(ItemEditorView.class,
                                     new RouteParameters(new RouteParam(RouteIds.ITEM_ID.getValue(), targetItemId),
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
        fillGUI();
    }

    private void fillGUI() {
        // Use items already loaded in memory from ItemContainerData
        List<ItemData> allItems = locationData.getItemContainerData() != null
            ? locationData.getItemContainerData().getItems()
            : new ArrayList<>();

        // Filter out null items (can occur if @DBRef fails to resolve)
        List<ItemData> items = allItems.stream()
            .filter(item -> item != null)
            .toList();

        gridContainer.removeAll();
        gridContainer.add(getItemsGrid(items));
    }

    private Grid<ItemData> getItemsGrid(List<ItemData> items) {
        Grid<ItemData> grid = new Grid<>(ItemData.class, false);
        grid.addColumn(item -> item.getDescriptionData().getSafeAdjective()).setHeader("Adjective");
        grid.addColumn(item -> item.getDescriptionData().getSafeNoun()).setHeader("Noun");
        grid.addColumn(item -> item.getDescriptionData().getShortDescription()).setHeader("Short Description")
            .setAutoWidth(true);

        grid.addColumn(ItemData::isContainable).setHeader("Containable");
        grid.addColumn(ItemData::isWearable).setHeader("Wearable");
        grid.addColumn(ItemData::isWorn).setHeader("Worn");

        grid.setWidth("800px");
        grid.setHeight("500px");
        grid.setEmptyStateText("Create some items.");

        grid.setItems(items);

        grid.addSelectionListener(selection -> {
            Optional<ItemData> optionalItem = selection.getFirstSelectedItem();
            if (optionalItem.isPresent()) {
                targetItemId = optionalItem.get().getId();
                edit.setEnabled(true);
            } else {
                edit.setEnabled(false);
            }
        });

        grid.addItemDoubleClickListener(e -> {
            targetItemId = e.getItem().getId();
            navigateToItemEditor(targetItemId);
        });

        return grid;
    }

    private void navigateToItemEditor(String anItemId) {
        UI.getCurrent().navigate(ItemEditorView.class,
                                 new RouteParameters(new RouteParam(RouteIds.ITEM_ID.getValue(), anItemId),
                                                     new RouteParam(RouteIds.LOCATION_ID.getValue(),
                                                                    locationData.getId()),
                                                     new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                    adventureData.getId())))
          .ifPresent(e -> e.setData(adventureData, locationData));
    }
}
