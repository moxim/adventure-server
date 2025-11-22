package com.pdg.adventure.view.item;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;
import com.pdg.adventure.model.VocabularyData;
import com.pdg.adventure.server.storage.AdventureService;
import com.pdg.adventure.view.support.RouteIds;
import com.pdg.adventure.view.support.ViewSupporter;

public class ItemViewSupporter {

    private final AdventureService adventureService;
    private final AdventureData adventureData;
    private final LocationData locationData;
    private final Div gridContainer;
    private final Button edit;

    @Getter
    private String targetItemId;

    public ItemViewSupporter(final AdventureService aAdventureService, final AdventureData aAdventureData,
                             final LocationData aLocationData, final Div aGridContainer, final Button aEdit) {
        adventureService = aAdventureService;
        adventureData = aAdventureData;
        locationData = aLocationData;
        gridContainer = aGridContainer;
        edit = aEdit;
    }

    private static ConfirmDialog getConfirmDialog(final ItemData anItem) {
        return ViewSupporter.getConfirmDialog("Delete Item", "item", anItem.getId());
    }

    public void confirmDeleteItem(LocationData aLocationData, ItemData anItem) {
        String itemId = anItem.getId();
        // Only count usages in commands and other non-container references
        List<ItemUsageTracker.ItemUsage> blockingUsages = ItemUsageTracker.findItemUsagesExcludingLocationContainers(
                adventureData, itemId);
        int usageCount = blockingUsages.size();

        if (usageCount > 0) {
            Notification.show("Cannot delete item '" + itemId + "' because it is referenced in " + usageCount +
                              " command(s). Please remove those references first.", 5000, Notification.Position.MIDDLE);
        } else {
            final var dialog = getConfirmDialog(anItem);

            dialog.addConfirmListener(event -> {
                // Remove item from location's ItemContainer
                if (aLocationData.getItemContainerData() != null) {
                    aLocationData.getItemContainerData().getItems()
                                 .removeIf(i -> i != null && itemId.equals(i.getId()));
                }
                // Save adventure to update @DBRef references
                adventureService.saveAdventureData(adventureData);

                // Refresh the grid
                gridContainer.removeAll();
                fillGUI(aLocationData, gridContainer);
            });

            dialog.open();
        }
    }

    Grid<ItemData> fillGUI(final LocationData aLocationData, final Div gridContainer) {
        // Use items already loaded in memory from ItemContainerData
        List<ItemData> allItems = aLocationData.getItemContainerData() != null ?
                                  aLocationData.getItemContainerData().getItems() : new ArrayList<>();

        // Filter out null items (can occur if @DBRef fails to resolve)
        List<ItemData> items = allItems.stream().filter(Objects::nonNull).toList();

        gridContainer.removeAll();
        Grid<ItemData> grid = getItemsGrid(items);
        gridContainer.add(grid);

        return grid;
    }

    private Grid<ItemData> getItemsGrid(List<ItemData> items) {
        Grid<ItemData> grid = new Grid<>(ItemData.class, false);

        grid.addColumn(ItemData::getId).setHeader(VocabularyData.ID_TEXT).setSortable(true).setAutoWidth(true)
            .setFlexGrow(0);
        grid.addColumn(item -> item.getDescriptionData().getSafeAdjective()).setHeader(VocabularyData.ADJECTIVE_TEXT);
        grid.addColumn(item -> item.getDescriptionData().getSafeNoun()).setHeader(VocabularyData.NOUN_TEXT)
            .setSortable(true);
        grid.addColumn(ViewSupporter::formatDescription).setHeader(VocabularyData.SHORT_TEXT).setSortable(true)
            .setAutoWidth(true);

        grid.addColumn(item -> item.isContainable() ? VocabularyData.YES_TEXT : VocabularyData.NO_TEXT)
            .setHeader(VocabularyData.CONTAINABLE_TEXT);
        grid.addColumn(item -> item.isWearable() ? VocabularyData.YES_TEXT : VocabularyData.NO_TEXT)
            .setHeader(VocabularyData.WEARABLE_TEXT);
        grid.addColumn(item -> item.isWorn() ? VocabularyData.YES_TEXT : VocabularyData.NO_TEXT)
            .setHeader(VocabularyData.WORN_TEXT);


        ViewSupporter.setSize(grid);
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

        // Add context menu
        createContextMenu(grid);

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

    private void showItemUsage(ItemData item) {
        List<ItemUsageTracker.ItemUsage> usages = ItemUsageTracker.findItemUsages(adventureData, item.getId());
        ViewSupporter.showUsages("Item Usage", "item", item.getId(), usages);
    }

    private void createContextMenu(Grid<ItemData> grid) {
        GridContextMenu<ItemData> contextMenu = grid.addContextMenu();

        contextMenu.addItem("Edit", e -> e.getItem().ifPresent(item -> navigateToItemEditor(item.getId())));

        contextMenu.addItem("Find Usage", e -> e.getItem().ifPresent(this::showItemUsage));

        contextMenu.addComponent(new Hr());

        contextMenu.addItem("Delete", e -> {
            final Optional<ItemData> item = e.getItem();
            item.ifPresent(aItemData -> confirmDeleteItem(locationData, aItemData));
        });
    }
}
