package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class DirectionData extends ItemData {
    //@DBRef
    private String destinationId;
    private boolean destinationMustBeMentioned;
    private CommandData commandData = new CommandData();
}
