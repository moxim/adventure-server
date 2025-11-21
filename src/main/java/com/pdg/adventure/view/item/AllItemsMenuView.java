package com.pdg.adventure.view.item;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
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
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.server.storage.ItemService;
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
    private final Button createButton;
    private final ComboBox<LocationData> locationSelector;
    private final IntegerField numberOfItems;
    private transient ItemViewSupporter itemViewSupporter;
    private transient AdventureData adventureData;
    private ListDataProvider<ItemLocationPair> dataProvider;

    @Autowired
    public AllItemsMenuView(AdventureService anAdventureService, ItemService anItemService) {
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
        locationSelector.setWidth("50%");

        // Create item button
        createButton = new Button("Create Item", event -> {
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
                                       event -> UI.getCurrent().navigate(AdventureEditorView.class, new RouteParameters(
                                               new RouteParam(RouteIds.ADVENTURE_ID.getValue(),
                                                              adventureData.getId()))));
        backButton.addClickShortcut(Key.ESCAPE);

        VerticalLayout leftSide = new VerticalLayout(numberOfItems, locationSelector, createButton, backButton);
        leftSide.setMaxWidth("30%");

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

        VerticalLayout rightSide = new VerticalLayout(searchFieldContainer, gridContainer);
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
                String lowerCaseSearchTerm = searchTerm.toLowerCase();
                dataProvider.setFilter(pair -> {
                    ItemData item = pair.item();
                    String id = item.getId().toLowerCase();
                    String adjective = item.getDescriptionData().getSafeAdjective().toLowerCase();
                    String noun = item.getDescriptionData().getSafeNoun().toLowerCase();
                    String shortDesc = item.getDescriptionData().getShortDescription().toLowerCase();
                    String locationDesc = ViewSupporter.formatDescription(pair.location()).toLowerCase();

                    return id.contains(lowerCaseSearchTerm) ||
                           adjective.contains(lowerCaseSearchTerm) || noun.contains(lowerCaseSearchTerm) ||
                           shortDesc.contains(lowerCaseSearchTerm) || locationDesc.contains(lowerCaseSearchTerm);
                });
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
        gridContainer.add(getItemLocationPairGrid(itemPairs));
    }

    private Grid<ItemLocationPair> getItemLocationPairGrid(List<ItemLocationPair> itemPairs) {
        Grid<ItemLocationPair> grid = new Grid<>(ItemLocationPair.class, false);

        grid.addColumn(pair -> pair.item().getId())
            .setHeader(VocabularyData.ID_TEXT).setSortable(true).setAutoWidth(true).setFlexGrow(0);

        grid.addColumn(pair -> pair.item().getDescriptionData().getSafeAdjective())
            .setHeader(VocabularyData.ADJECTIVE_TEXT);

        grid.addColumn(pair -> pair.item().getDescriptionData().getSafeNoun()).setHeader(VocabularyData.NOUN_TEXT)
            .setSortable(true);

        grid.addColumn(pair -> pair.item().getDescriptionData().getShortDescription())
            .setHeader(VocabularyData.SHORT_TEXT).setAutoWidth(true).setSortable(true);

        grid.addColumn(pair -> ViewSupporter.formatDescription(pair.location())).setHeader("Location")
            .setAutoWidth(true).setSortable(true);

        grid.addColumn(pair -> pair.item().isContainable() ? VocabularyData.YES_TEXT : VocabularyData.NO_TEXT)
            .setHeader(VocabularyData.CONTAINABLE_TEXT).setAutoWidth(true);

        grid.addColumn(pair -> pair.item().isWearable() ? VocabularyData.YES_TEXT : VocabularyData.NO_TEXT)
            .setHeader(VocabularyData.WEARABLE_TEXT).setAutoWidth(true);

        grid.addColumn(pair -> pair.item().isWorn() ? VocabularyData.YES_TEXT : VocabularyData.NO_TEXT)
            .setHeader(VocabularyData.WORN_TEXT).setAutoWidth(true);


        ViewSupporter.setSize(grid);
        grid.setEmptyStateText("Create some items.");

        dataProvider = new ListDataProvider<>(itemPairs);
        grid.setDataProvider(dataProvider);

        grid.addItemDoubleClickListener(e -> {
            ItemLocationPair pair = e.getItem();
            navigateToItemEditor(pair.item().getId(), pair.location().getId());
        });

        // Add context menu
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

    private void createContextMenu(Grid<ItemLocationPair> grid) {
        GridContextMenu<ItemLocationPair> contextMenu = grid.addContextMenu();

        contextMenu.addItem("Edit", e -> e.getItem().ifPresent(
                pair -> navigateToItemEditor(pair.item().getId(), pair.location().getId())));

        contextMenu.addItem("Find Usage", e -> e.getItem().ifPresent(this::showItemUsage));

        contextMenu.addComponent(new Hr());

        contextMenu.addItem("Delete", e -> e.getItem().ifPresent(this::confirmDeleteItem));
    }

    private void showItemUsage(ItemLocationPair pair) {
        ItemData item = pair.item();
        List<ItemUsageTracker.ItemUsage> usages = ItemUsageTracker.findItemUsages(adventureData, item.getId());
        ViewSupporter.showUsages("Item Usage", "item", item.getId(), usages);
    }

    private void confirmDeleteItem(ItemLocationPair pair) {
        ItemData item = pair.item();
        LocationData location = pair.location();

        itemViewSupporter.confirmDeleteItem(location, item);
    }
}
