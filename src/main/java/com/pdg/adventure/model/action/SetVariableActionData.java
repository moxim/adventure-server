package com.pdg.adventure.model.action;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SetVariableActionData extends ActionData {
    private final String variableName;
    private final String variableValue;
}
