package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** The variable a RandomAction sets to a random number. */
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class RandomActionData extends ActionData {
    private String name;
}
