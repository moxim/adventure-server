package com.pdg.adventure.model.condition;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class EqualsConditionData extends PreConditionData {
    private String variableName;
    private String value;
}
