package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class IncrementVariableActionData extends ActionData {
    private String name;
    private String value;
}
