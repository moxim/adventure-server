package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** LoadGame: lists the player's saved games, or restores the one in the slot the player typed; no parameters. */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class LoadGameActionData extends ActionData {
}
