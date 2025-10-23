package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

import com.pdg.adventure.server.storage.mongo.CascadeSave;

@Document
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class LocationData extends ThingData {
    @DBRef(lazy = false)
    @CascadeSave
    private ItemContainerData itemContainerData = new ItemContainerData();
    private Set<DirectionData> directionsData = new HashSet<>();

    private boolean hasBeenVisited;
    private int lumen= 50;

    @Override
    public String toString() {
        return "LocationData{" +
                "id=" + getId() +
                "itemContainerDataId=" + (itemContainerData != null ? itemContainerData.getId() : "null") +
                "}";
    }
}
