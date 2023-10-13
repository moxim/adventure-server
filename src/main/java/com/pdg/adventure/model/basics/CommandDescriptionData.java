package com.pdg.adventure.model.basics;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@ToString(callSuper = true)
public class CommandDescriptionData extends BasicDescriptionData {
    private String verb;
}
