package com.pdg.adventure.view.item;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.view.adventure.AdventureEditorView;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

/**
 * View that shows all items across all locations in an adventure.
 * This provides an adventure-level view of items, similar to how LocationsMenuView shows all locations.
 */
@Route(value = "adventures/:adventureId/items", layout = ItemsMainLayout.class)
@PageTitle("All Items")
public class AllItemsMenuView extends VerticalLayout implements BeforeEnterObserver {

    private final transient AdventureService adventureService;
    private final Div gridContainer;
    private final TextField searchField;
    private final Button backButton;
    private final Button createButton;
    private final ComboBox<LocationData> locationSelector;
    private final IntegerField numberOfItems;
    private transient AdventureData adventureData;
    private transient ListDataProvider<ItemLocationPair> dataProvider;

    @Autowired
    public AllItemsMenuView(AdventureService anAdventureService) {
        setSizeFull();

        adventureService = anAdventureService;

        numberOfItems = new IntegerField("Total Items:");
        numberOfItems.setReadOnly(true);
        numberOfItems.setTooltipText("Total number of items across all locations");

        // Location selector for creating new items
        locationSelector = new ComboBox<>("Create in Location");
        locationSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        locationSelector.setPlaceholder("Select location");
        locationSelector.setTooltipText("Select which location to create the new item in");
        locationSelector.setWidthFull();

        // Create item button
        createButton = new Button("Create Item", event -> {
            LocationData selectedLocation = locationSelector.getValue();
            if (selectedLocation != null) {
                navigateToCreateItem(selectedLocation.getId());
            }
        });
        createButton.setIcon(new Icon(VaadinIcon.PLUS));
        createButton.setEnabled(false);
        locationSelector.addValueChangeListener(event ->
            createButton.setEnabled(event.getValue() != null)
        );

        backButton = new Button("Back to Adventure", event -> {
            UI.getCurrent().navigate(AdventureEditorView.class,
                    new RouteParameters(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())));
        });
        backButton.addClickShortcut(Key.ESCAPE);

        VerticalLayout leftSide = new VerticalLayout(numberOfItems, locationSelector, createButton, backButton);

        searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Find item");
        searchField.setTooltipText("Find items by ID, noun, adjective, or description");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> filterItems(e.getValue()));

        gridContainer = new Div();
        gridContainer.setSizeFull();

        VerticalLayout rightSide = new VerticalLayout(searchField, gridContainer);
        rightSide.setSizeFull();

        HorizontalLayout mainRow = new HorizontalLayout(leftSide, rightSide);
        mainRow.setSizeFull();

        setMargin(true);
        setPadding(true);

        add(mainRow);
    }

    private Grid<ItemLocationPair> getItemsGrid(List<ItemLocationPair> itemPairs) {
        Grid<ItemLocationPair> grid = new Grid<>(ItemLocationPair.class, false);

        grid.addColumn(pair -> pair.getItem().getDescriptionData().getSafeAdjective())
            .setHeader("Adjective")
            .setSortable(true);

        grid.addColumn(pair -> pair.getItem().getDescriptionData().getSafeNoun())
            .setHeader("Noun")
            .setSortable(true);

        grid.addColumn(pair -> pair.getItem().getDescriptionData().getShortDescription())
            .setHeader("Description")
            .setAutoWidth(true)
            .setSortable(true);

        grid.addColumn(pair -> ViewSupporter.formatDescription(pair.getLocation()))
            .setHeader("Location")
            .setAutoWidth(true)
            .setSortable(true);

        grid.addColumn(pair -> pair.getItem().isContainable() ? "Yes" : "No")
            .setHeader("Containable")
            .setAutoWidth(true);

        grid.addColumn(pair -> pair.getItem().isWearable() ? "Yes" : "No")
            .setHeader("Wearable")
            .setAutoWidth(true);

        grid.setWidth("900px");
        grid.setHeight("500px");
        grid.setEmptyStateText("No items found. Create items from location views.");

        dataProvider = new ListDataProvider<>(itemPairs);
        grid.setDataProvider(dataProvider);

        grid.addItemDoubleClickListener(e -> {
            ItemLocationPair pair = e.getItem();
            navigateToItemEditor(pair.getItem().getId(), pair.getLocation().getId());
        });

        return grid;
    }

    private void filterItems(String searchTerm) {
        if (dataProvider != null) {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                dataProvider.clearFilters();
            } else {
                String lowerCaseSearchTerm = searchTerm.toLowerCase();
                dataProvider.setFilter(pair -> {
                    ItemData item = pair.getItem();
                    String adjective = item.getDescriptionData().getSafeAdjective().toLowerCase();
                    String noun = item.getDescriptionData().getSafeNoun().toLowerCase();
                    String shortDesc = item.getDescriptionData().getShortDescription().toLowerCase();
                    String locationDesc = ViewSupporter.formatDescription(pair.getLocation()).toLowerCase();

                    return adjective.contains(lowerCaseSearchTerm) ||
                           noun.contains(lowerCaseSearchTerm) ||
                           shortDesc.contains(lowerCaseSearchTerm) ||
                           locationDesc.contains(lowerCaseSearchTerm);
                });
            }
        }
    }

    private void fillGUI() {
        // Collect all items from all locations
        List<ItemLocationPair> itemPairs = new ArrayList<>();

        for (LocationData location : adventureData.getLocationData().values()) {
            if (location.getItemContainerData() != null) {
                List<ItemData> items = location.getItemContainerData().getItems();
                if (items != null) {
                    for (ItemData item : items) {
                        // Filter out null items (can occur if @DBRef fails to resolve)
                        if (item != null) {
                            itemPairs.add(new ItemLocationPair(item, location));
                        }
                    }
                }
            }
        }

        numberOfItems.setValue(itemPairs.size());
        gridContainer.removeAll();
        gridContainer.add(getItemsGrid(itemPairs));
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // The setData method will be called from navigation
    }

    public void setData(AdventureData anAdventureData) {
        adventureData = anAdventureData;

        // Populate location selector with all locations
        List<LocationData> locations = new ArrayList<>(adventureData.getLocationData().values());
        locationSelector.setItems(locations);

        fillGUI();
    }

    private void navigateToItemEditor(String itemId, String locationId) {
        UI.getCurrent().navigate(ItemEditorView.class,
                new RouteParameters(
                        new RouteParam(RouteIds.ITEM_ID.getValue(), itemId),
                        new RouteParam(RouteIds.LOCATION_ID.getValue(), locationId),
                        new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())))
                .ifPresent(e -> {
                    LocationData location = adventureData.getLocationData().get(locationId);
                    e.setData(adventureData, location);
                });
    }

    private void navigateToCreateItem(String locationId) {
        UI.getCurrent().navigate(ItemEditorView.class,
                new RouteParameters(
                        new RouteParam(RouteIds.LOCATION_ID.getValue(), locationId),
                        new RouteParam(RouteIds.ADVENTURE_ID.getValue(), adventureData.getId())))
                .ifPresent(e -> {
                    LocationData location = adventureData.getLocationData().get(locationId);
                    e.setData(adventureData, location);
                });
    }

    /**
     * Helper class to pair an item with its location for display purposes.
     */
    private static class ItemLocationPair {
        private final ItemData item;
        private final LocationData location;

        public ItemLocationPair(ItemData item, LocationData location) {
            this.item = item;
            this.location = location;
        }

        public ItemData getItem() {
            return item;
        }

        public LocationData getLocation() {
            return location;
        }
    }
}
