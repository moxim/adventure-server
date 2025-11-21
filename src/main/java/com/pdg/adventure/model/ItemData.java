package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Item data stored in MongoDB.
 * Items are stored in their own collection for better scalability and querying.
 */
@Document(collection = "items")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@CompoundIndex(name = "adventure_location_item_idx", def = "{'adventureId': 1, 'locationId': 1}")
public class ItemData extends ThingData {

    /**
     * The adventure this item belongs to.
     * Items are scoped per adventure.
     */
    private String adventureId;

    /**
     * The location this item is in.
     * Items are scoped per location within an adventure.
     */
    private String locationId;

    private boolean isContainable; // i.e. can be picked up and put into a container (like a pocket, location, chest, etc)
    private String parentContainerId; // The container this item is in (if any)
    private boolean isWearable;
    private boolean isWorn;

//    private boolean isOpenable;
//    private boolean isOpen;
//    private boolean isLockable;
//    private boolean isLocked;
//    private String keyId;
//    private double weight;
//    private double volume;
//    private double capacity;
//    private boolean isTransparent;

    @Override
    public String toString() {
        return "ItemData{" +
                "id=" + getId() +
                "isContainable=" + isContainable +
                ", isWearable=" + isWearable +
                ", isWorn=" + isWorn +
                ", parentContainer=" + parentContainerId +
                '}';
    }
}
