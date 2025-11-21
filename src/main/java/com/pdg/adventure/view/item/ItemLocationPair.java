package com.pdg.adventure.view.item;

import com.pdg.adventure.model.ItemData;
import com.pdg.adventure.model.LocationData;

/**
 * Helper class to pair an item with its location for display purposes.
 */
record ItemLocationPair(ItemData item, LocationData location) {
}
