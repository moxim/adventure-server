package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class LocationData extends ThingData {
    @DBRef(lazy = true)
    private AdventureData adventure;
    @DBRef
    private ItemContainerData itemContainerData = new ItemContainerData();
    @DBRef
    private Set<DirectionData> directionsData = new HashSet<>();

    private boolean hasBeenVisited;
    private int lumen= 50;
}
