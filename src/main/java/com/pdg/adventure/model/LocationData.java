package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Document
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
//@ToString(callSuper = true)
public class LocationData extends ThingData {
    @DBRef(lazy = true)
    private AdventureData adventure;
    @DBRef(lazy = false)
    private ItemContainerData itemContainerData = new ItemContainerData();
//    @DBRef
    private Set<DirectionData> directionsData = new HashSet<>();

    private boolean hasBeenVisited;
    private int lumen= 50;

    @Override
    public String toString() {
        return "LocationData{" +
                "id=" + getId() +
                "adventureId=" + (adventure != null ? adventure.getId() : "null") +
                "itemContainerDataId=" + (itemContainerData != null ? itemContainerData.getId() : "null") +
                "}";
    }
}
