package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** AutoDrop: drops whichever item the player named; has no parameters of its own. */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AutoDropActionData extends ActionData {
}
