package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class DirectionData extends ItemData {
    private LocationData destinationData;
    private boolean destinationMustBeMentioned;
    //    private CommandDescriptionData commandDescriptionData;
}
