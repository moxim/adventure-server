package com.pdg.adventure.view.command.condition;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.ItemContainerData;
import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;

class ConditionEditorSupport {
    private ConditionEditorSupport() {}

    static List<ItemData> allItems(AdventureData data) {
        List<ItemData> items = new ArrayList<>();
        data.getLocationData().values().forEach(loc -> {
            ItemContainerData container = loc.getItemContainerData();
            if (container != null && container.getItems() != null) {
                items.addAll(container.getItems());
            }
        });
        if (data.getPlayerPocket() != null && data.getPlayerPocket().getItems() != null) {
            items.addAll(data.getPlayerPocket().getItems());
        }
        return items;
    }

    static List<ItemData> wearableItems(AdventureData data) {
        return allItems(data).stream().filter(ItemData::isWearable).toList();
    }

    static List<LocationData> allLocations(AdventureData data) {
        return new ArrayList<>(data.getLocationData().values());
    }
}
