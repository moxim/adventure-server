package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PlayActionData extends ActionData {
    private String soundId;
    /** How many times the sound plays in a row (1 to 99); 0 stops every copy of it. */
    private Integer times;
}
