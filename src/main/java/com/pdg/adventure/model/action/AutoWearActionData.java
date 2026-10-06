package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** AutoWear: wears whichever item the player named; has no parameters of its own. */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AutoWearActionData extends ActionData {
}
