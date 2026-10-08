package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** SaveGame: saves the running game into a slot (the slot number the player typed, else the first free one); no parameters. */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SaveGameActionData extends ActionData {
}
