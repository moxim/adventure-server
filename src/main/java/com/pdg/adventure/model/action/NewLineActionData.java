package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** Ends the current line of text (PAW's NEWLINE). Has nothing to configure. */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class NewLineActionData extends ActionData {
}
