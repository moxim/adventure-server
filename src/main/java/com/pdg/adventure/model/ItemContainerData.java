package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

import com.pdg.adventure.server.storage.mongo.CascadeSave;

/**
 * Container data that holds references to items.
 * Items themselves are stored in the separate "items" collection and referenced by ID.
 */
@Document(collection = "containers")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ItemContainerData extends ItemData {
    /**
     * List of item IDs contained in this container.
     * The actual ItemData objects are stored separately in the items collection.
     */

    @DBRef(lazy = false)
    @CascadeSave
    private List<ItemData> items = new ArrayList<>();

    private int maxSize;
    private boolean holdingDirections;

    @Override
    public String toString() {
        return "ItemContainerData{" +
                "id=" + getId() +
                ", itemCount=" + items.size() +
                "}";
    }
}
