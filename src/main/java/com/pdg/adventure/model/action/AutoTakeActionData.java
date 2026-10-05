package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** AutoTake: takes whichever item the player named; has no parameters of its own. */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AutoTakeActionData extends ActionData {
}
