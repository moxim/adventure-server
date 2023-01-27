package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class LocationData extends ThingData {
    private ContainerData containerData;
    private ContainerData directionsData;
    private boolean hasBeenVisited;
}
