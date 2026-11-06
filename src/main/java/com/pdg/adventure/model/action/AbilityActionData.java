package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** What the player can carry: the most weight and the most objects, both whole numbers from 0. */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AbilityActionData extends ActionData {
    private Integer maxWeight;
    private Integer maxItems;
}
