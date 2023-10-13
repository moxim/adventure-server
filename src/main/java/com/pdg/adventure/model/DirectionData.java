package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class DirectionData extends ItemData {
    @DBRef
    private LocationData destinationData = new LocationData();
    private boolean destinationMustBeMentioned;
    private CommandData commandData = new CommandData();
}
