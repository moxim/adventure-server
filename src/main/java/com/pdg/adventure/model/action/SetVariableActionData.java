package com.pdg.adventure.model.action;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SetVariableActionData extends ActionData {
    private String variableName;
    private String variableValue;
}
