package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class MoveItemActionData extends ActionData {
    private String thingId;
    private String destinationId;
}
