package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

import com.pdg.adventure.server.storage.mongo.CascadeDelete;
import com.pdg.adventure.server.storage.mongo.CascadeSave;

@Document(collection = "locations")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class LocationData extends ThingData {
    @DBRef(lazy = false)
    @CascadeSave
    @CascadeDelete
    private ItemContainerData itemContainerData;
    private Set<DirectionData> directionsData = new HashSet<>();

    private int timesVisited;
    private int lumen = 50;

    public LocationData() {
        timesVisited = 0;
        itemContainerData = new ItemContainerData(getId());
    }

    @Override
    public String toString() {
        return "LocationData{" +
               "id=" + getId() +
               "itemContainerDataId=" + (itemContainerData != null ? itemContainerData.getId() : "null") +
               "}";
    }
}
