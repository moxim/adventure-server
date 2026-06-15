package com.pdg.adventure.view.item;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.RolesAllowed;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.model.*;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.server.storage.service.ItemService;
import com.pdg.adventure.view.adventure.AdventureEditorView;
import com.pdg.adventure.view.support.GridProvider;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

/**
 * View that shows all items across all locations in an adventure.
 * This provides an adventure-level view of items, similar to how LocationsMenuView shows all locations.
 */
@Route(value = "author/adventures/:adventureId/items", layout = ItemsMainLayout.class)
@PageTitle("All Items")
@RolesAllowed("ROLE_AUTHOR")
public class AllItemsMenuView extends VerticalLayout implements BeforeEnterObserver {

    private final transient AdventureService adventureService;
    private final Div gridContainer;
    private final Button createButton;
    private final ComboBox<LocationData> locationSelector;
    private final Span numberOfItems;
    private transient ItemViewSupporter itemViewSupporter;
    private transient AdventureData adventureData;
    private ListDataProvider<ItemLocationPairAdapter> dataProvider;

    public AllItemsMenuView(AdventureService anAdventureService, ItemService anItemService) {
        setSizeFull();

        adventureService = anAdventureService;

        numberOfItems = new Span();

        // Location selector for creating new items
        locationSelector = new ComboBox<>("Create in Location");
        locationSelector.setItemLabelGenerator(ViewSupporter::formatDescription);
        locationSelector.setPlaceholder("Select location");
        locationSelector.setTooltipText("Select which location to create the new item in");
        locationSelector.setWidth("50%");

        // Create item button
        createButton = new Button("Create Item", _ -> {
            LocationData selectedLocation = locationSelector.getValue();
            if (selectedLocation != null) {
                navigateToCreateItem(selectedLocation.getId());
            }
        });
        createButton.setIcon(new Icon(VaadinIcon.PLUS));
        createButton.setEnabled(false);
        locationSelector.addValueChangeListener(
                event -> createButton.setEnabled(event.getValue() != null));

        Button backButton = new Button("Back",
                                       _ -> UI.getCurrent().navigate(AdventureEditorView.class, new RouteParameters(
                                               new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                              adventureData.getId()))));
        backButton.addClickShortcut(Key.ESCAPE);

        VerticalLayout leftSide = new VerticalLayout(numberOfItems, locationSelector, createButton, backButton);
        leftSide.setMaxWidth("20%");

        gridContainer = new Div();
        gridContainer.setSizeFull();

        TextField searchField = new TextField();
        searchField.setWidth("50%");
        searchField.setPlaceholder("Find item");
        searchField.setTooltipText("Find items by ID, noun, adjective, or description");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setValueChangeMode(ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> filterItems(e.getValue()));

        Div searchFieldContainer = new Div();
        searchFieldContainer.setWidthFull();
        searchFieldContainer.add(searchField);

        VerticalLayout rightSide = new VerticalLayout(searchFieldContainer, ViewSupporter.doubleClickEditHint(), gridContainer);
        rightSide.setSizeFull();

        HorizontalLayout mainRow = new HorizontalLayout(leftSide, rightSide);
        mainRow.setSizeFull();

        setMargin(true);
        setPadding(true);

        add(mainRow);
    }


    private void filterItems(String searchTerm) {
        if (dataProvider != null) {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                dataProvider.clearFilters();
            } else {
                String lower = searchTerm.toLowerCase();
                dataProvider.setFilter(adapter ->
                        adapter.getId().toLowerCase().contains(lower) ||
                        adapter.getAdjective().toLowerCase().contains(lower) ||
                        adapter.getNoun().toLowerCase().contains(lower) ||
                        adapter.getShortDescription().toLowerCase().contains(lower) ||
                        adapter.getLocationDescription().toLowerCase().contains(lower)
                );
            }
        }
    }

    private void navigateToCreateItem(String locationId) {
        UI.getCurrent().navigate(ItemEditorView.class,
                                 new RouteParameters(new RouteParam(RouteIds.LOCATION_ID.getValue(), locationId),
                                                     new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                    adventureData.getId()))).ifPresent(e -> {
            LocationData location = adventureData.getLocationData().get(locationId);
            e.setData(adventureData, location);
        });
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

        LocationData locationData = new LocationData(); // Dummy, not used in this view
        Button edit = new Button(); // Dummy, not used in this view
        itemViewSupporter = new ItemViewSupporter(adventureService, adventureData, locationData, gridContainer, edit);

        fillGUI();
    }

    private void fillGUI() {
        final var itemPairs = ViewSupporter.getItemLocationPairs(adventureData.getLocationData().values());
        numberOfItems.setText("Total Items: " + itemPairs.size());
        gridContainer.removeAll();
        gridContainer.add(getItemLocationPairGrid(itemPairs));
    }

    private Grid<ItemLocationPairAdapter> getItemLocationPairGrid(List<ItemLocationPair> itemPairs) {
        List<ItemLocationPairAdapter> adapters = itemPairs.stream()
                .map(ItemLocationPairAdapter::new)
                .toList();

        GridProvider<ItemLocationPairAdapter> gridProvider = new GridProvider<>(ItemLocationPairAdapter.class);
        gridProvider.hideIdColumn();
        gridProvider.addColumn(ItemLocationPairAdapter::getAdjective, VocabularyData.ADJECTIVE_TEXT);
        gridProvider.addColumn(ItemLocationPairAdapter::getNoun, VocabularyData.NOUN_TEXT);
        gridProvider.addColumn(ItemLocationPairAdapter::getLocationDescription, "Location");
        gridProvider.addColumn(ItemLocationPairAdapter::getContainable, VocabularyData.CONTAINABLE_TEXT);
        gridProvider.addColumn(ItemLocationPairAdapter::getWearable, VocabularyData.WEARABLE_TEXT);
        gridProvider.addColumn(ItemLocationPairAdapter::getWorn, VocabularyData.WORN_TEXT);

        gridProvider.addItemDoubleClickListener(e -> {
            ItemLocationPair pair = e.getItem().getPair();
            navigateToItemEditor(pair.item().getId(), pair.location().getId());
        });

        Grid<ItemLocationPairAdapter> grid = gridProvider.getGrid();
        ViewSupporter.setSize(grid);
        grid.setEmptyStateText("Create some items.");

        dataProvider = new ListDataProvider<>(adapters);
        grid.setDataProvider(dataProvider);

        createContextMenu(grid);
        return grid;
    }

    private void navigateToItemEditor(String itemId, String locationId) {
        UI.getCurrent().navigate(ItemEditorView.class,
                                 new RouteParameters(new RouteParam(RouteIds.ITEM_ID.getValue(), itemId),
                                                     new RouteParam(RouteIds.LOCATION_ID.getValue(), locationId),
                                                     new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                                    adventureData.getId()))).ifPresent(e -> {
            LocationData location = adventureData.getLocationData().get(locationId);
            e.setData(adventureData, location);
        });
    }

    private void createContextMenu(Grid<ItemLocationPairAdapter> grid) {
        GridContextMenu<ItemLocationPairAdapter> contextMenu = grid.addContextMenu();

        contextMenu.addItem("Edit", e -> e.getItem().ifPresent(adapter -> {
            ItemLocationPair pair = adapter.getPair();
            navigateToItemEditor(pair.item().getId(), pair.location().getId());
        }));

        contextMenu.addItem("Find Usage", e -> e.getItem().ifPresent(this::showItemUsage));

        contextMenu.addComponent(new Hr());

        contextMenu.addItem("Delete", e -> e.getItem().ifPresent(this::confirmDeleteItem));
    }

    private void showItemUsage(ItemLocationPairAdapter adapter) {
        ItemData item = adapter.getPair().item();
        List<ItemUsageTracker.ItemUsage> usages = ItemUsageTracker.findItemUsages(adventureData, item.getId());
        ViewSupporter.showUsages("Item Usage", "item", item.getId(), usages);
    }

    private void confirmDeleteItem(ItemLocationPairAdapter adapter) {
        itemViewSupporter.confirmDeleteItem(adapter.getPair().location(), adapter.getPair().item());
    }
}
