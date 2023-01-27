package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ItemData extends ThingData {
    private boolean isContainable;
    private ContainerData parentContainer;
    private boolean isWearable;
    private boolean isWorn;
}
