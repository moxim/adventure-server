package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** Tells the player how many turns they have taken (the {@code TURNS} variable). Has nothing to configure. */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class TurnsActionData extends ActionData {
}
