package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** The container a ListAtAction lists: the item container of a location, or the player's pocket (by container id). */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ListAtActionData extends ActionData {
    private String containerId;
}
